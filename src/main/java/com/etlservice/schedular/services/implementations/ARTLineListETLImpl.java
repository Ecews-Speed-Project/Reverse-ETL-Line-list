/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.*;
import com.etlservice.schedular.entities.linelists.AhdLineList;
import com.etlservice.schedular.entities.linelists.ArtLinelist;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.enums.LineListStatus;
import com.etlservice.schedular.enums.LineListType;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.Partition;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.ArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.CustomArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.LineListTrackerRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.ARTLineListETL;
import com.etlservice.schedular.services.ArtLineListGeneratorService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;

/**
 *
 * @author MORRISON.I
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ARTLineListETLImpl implements ARTLineListETL {
    private final ArtLineListRepository artLineListRepository;
    private final FacilityRepository facilityRepository;
    private final ContainerRepository containerRepository;
    private final CustomArtLineListRepository customArtLineListRepository;
    private final LineListTrackerRepository lineListTrackerRepository;
    private final HelperFunctions helperFunctions;
    private final CustomLineListServiceImpl customLineListService;
    private final RabbitTemplate rabbitTemplate;
    private final ArtLineListGeneratorService artLineListGeneratorService;
    private final ExecutorService executorService;
    @Value("${daily.etl.queue}")
    private String dailyEtlQueue;

    @Override
    public void buildArtLineList(List<Container> mongoContainers) {
        Facility facility = facilityRepository.findFacilityByDatimCode(
                mongoContainers.get(0).getMessageHeader().getFacilityDatimCode());
        LocalDate currentDate = LocalDate.now();
        List<LocalDate> previousQuarters = helperFunctions.getCutOffDatesForEachQuarter(currentDate);
        extractArtLineListForDifferentQuarters(mongoContainers, facility, currentDate, previousQuarters);
    }

    @Override
    public void buildDailyArtLineList(List<Container> containers) {
        LocalDate currentDate = LocalDate.now();
        Date cutOff = Date.from(currentDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());
        String quarterCode = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
        log.info(quarterCode);
        generateARTLineList(containers, cutOff, quarterCode);
        log.info("ART Line List Generated Successfully {}", containers.size());
    }

    @Override
    public void buildDailyArtLineList(List<IdWrapper> containers, LocalDate cutOffDate) {
        Date cutOff = Date.from(cutOffDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        String quarterCode = helperFunctions.getQuarterCodeFromLocalDate(cutOffDate);
        log.info(quarterCode);
        containers.forEach(idWrapper -> {
            Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
            if (container != null && container.getMessageData().getDemographics().getVoided() == 0) {
                Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                if (facility != null) {
                    ArtLinelist artLinelist = artLineListGeneratorService.mapARTLineList(container, facility, cutOff, quarterCode);
                    if (artLinelist != null) {
                        artLineListRepository.save(artLinelist);
                    }
                }
            }
        });
        log.info("ART Line List Generated Successfully {}", containers.size());
//        containers.stream()
//                .map(idWrapper -> containerRepository.findById(idWrapper.getId()).orElse(null))
//                .filter(container -> container != null && container.getMessageData().getDemographics().getVoided() == 0)
//                .forEach(container -> {
//                    Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
//                    if (facility != null) {
//                        ArtLinelist artLinelist = artLineListGeneratorService.mapARTLineList(container, facility, cutOff, quarterCode);
//                        if (artLinelist != null) {
//                            artLineListRepository.save(artLinelist);
//                        }
//                    }
//                });
    }

    @Override
    public void buildCustomArtLineList(List<IdWrapper> containers, LocalDate cutOffDate) {
        Date cutOff = Date.from(cutOffDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        String quarterCode = helperFunctions.getQuarterCodeFromLocalDate(cutOffDate);
        log.info(quarterCode);
        Partition<IdWrapper> partition = Partition.ofSize(containers, 100);
        CountDownLatch latch = new CountDownLatch(partition.size());
        partition.forEach(part -> executorService.execute(() -> {
            try {
                generateCustomARTLineList(part, cutOff, quarterCode);
                log.info("ART Line List Generated Successfully {}", part.size());
            } catch (Exception e) {
                log.error("Error generating ART Line List {}", e.getMessage());
            } finally {
                latch.countDown();
            }
        }));
        try {
            latch.await();
            log.info("ART Line List Generated Successfully {}", containers.size());
        } catch (InterruptedException e) {
            log.error("Error generating ART Line List {}", e.getMessage());
        }
//        generateCustomARTLineList(containers, cutOff, quarterCode);
//        log.info("ART Line List Generated Successfully {}", containers.size());
    }

    @Override
    public void buildNewQuarterArtLineList(List<IdWrapper> containers) {
        LocalDate currentDate = LocalDate.now();
        Date cutOff = Date.from(currentDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());
        String quarterCode = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
        log.info(quarterCode);
        generateNewQuarterARTLineList(containers, cutOff, quarterCode);
        log.info("ART Line List Generated Successfully {}", containers.size());
    }

    @Override
    public Page<Container> getContainers (int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Container> containers = containerRepository.findByMessageDataPatientIdentifiersIdentifierType(4, pageable);
        if (containers != null && !containers.isEmpty()) {
            return containers;
        } else
            return null;
    }

    @Override
    public String getContainersByIdentifierType(int identifierType) {
        LineListTracker lineListTracker = lineListTrackerRepository.findByStatusAndLineListType(PROCESSING.name(), LineListType.CUSTOM.name())
                .orElse(null);
        if (lineListTracker == null) {
            lineListTracker = LineListTracker.builder()
                    .status(PROCESSING.name())
                    .currentPage(0)
                    .dateStarted(LocalDateTime.now())
                    .pageSize(500)
                    .lineListType(LineListType.CUSTOM.name())
                    .build();
            lineListTrackerRepository.save(lineListTracker);
        }
        int page = lineListTracker.getCurrentPage();
        int size = lineListTracker.getPageSize();
        boolean hasMoreResults = true;
        while (hasMoreResults) {
            log.info("Extracting Page: {}", page);
            Page<Container> containersPage = getContainers(page, size);
            if (containersPage == null || containersPage.isEmpty()) {
                log.info("No more results");
                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTrackerRepository.save(lineListTracker);
                break;
            }
            log.info("Containers page: {}", containersPage.getTotalElements());
            List<Container> containers = containersPage.getContent();
            log.info("Containers: {}", containers.size());
            List<List<Container>> partitionedContainers = Partition.ofSize(containers, 50);
            log.info("Total partitions = {}", partitionedContainers.size());
            for (List<Container> containerList : partitionedContainers) {
                rabbitTemplate.convertAndSend(dailyEtlQueue, containerList);
            }
            log.info("Total containers sent to queue = {}", containers.size());
            page++;
            helperFunctions.updateLineListTracker(lineListTracker, page, containersPage, containers);
            hasMoreResults = containers.size() == size;
            log.info("Has more results: {}", hasMoreResults);
            if (!hasMoreResults) {
                log.info("No more results");
                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTrackerRepository.save(lineListTracker);
            }
        }
        return "completed";
    }

    @Override
    public ContainerList getContainers() {
        List<Container> containers = containerRepository.findByMessageDataPatientIdentifiersIdentifierType(4);
        if (containers != null && !containers.isEmpty()) {
            ContainerList containerList = ContainerList.getInstance();
            containerList.setContainers(containers);
            containerList.setTotalPages(1);
            log.info(String.valueOf(containers.size()));
            return containerList;
        } else
            return null;
    }

    //    @Cacheable(value = "containers")
    public Page<Container> getContainerPage (Pageable pageable) {
        return containerRepository.findAll(pageable);
    }

    private void extractArtLineListForDifferentQuarters(List<Container> mongoContainers, Facility facility, LocalDate currentDate, List<LocalDate> previousQuarters) {
        String quarter;
        for (LocalDate quarterDate : previousQuarters) {
            int index = previousQuarters.indexOf(quarterDate);
            if (index == 0) {
                quarter = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
            } else {
                quarter = "FY" + String.valueOf(quarterDate.getYear()).substring(2) + "Q" + index;
            }
            Date cutOff;
            if (quarterDate.isEqual(currentDate)) {
                cutOff = Date.from(currentDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());
            } else {
                cutOff = helperFunctions.getCutOffDate(quarterDate);
            }
            generateARTLineList(mongoContainers, cutOff, quarter);
            log.info("Done with {} patients in {} for {}", mongoContainers.size(), facility.getFacilityName(), quarter);
        }
    }

    private void generateARTLineList(List<Container> mongoContainers, Date cutOff, String quarter) {
        mongoContainers.stream()
                .filter(container -> container.getMessageData().getDemographics().getVoided() == 0)
                .map(container -> {
                    Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                    return artLineListGeneratorService.mapARTLineList(container, facility, cutOff, quarter);
                })
                .filter(artLinelist -> artLinelist != null && artLinelist.getPatientUniqueId() != null)
                .forEach(artLineListRepository::save);
    }

    private void generateCustomARTLineList(List<IdWrapper> mongoContainers, Date cutOff, String quarter) {
        mongoContainers.stream()
                .map(idWrapper -> {
                    Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
                    if (container != null && container.getMessageData().getDemographics().getVoided() == 0) {
                        Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                        return artLineListGeneratorService.mapCustomArtLineList(container, facility, cutOff, quarter);
                    } else
                        return null;
                })
                .filter(artLinelist -> artLinelist != null && artLinelist.getPatientUniqueId() != null)
                .forEach(customArtLineListRepository::save);
    }

    private void generateNewQuarterARTLineList(List<IdWrapper> mongoContainers, Date cutOff, String quarter) {
        mongoContainers.stream()
                .map(idWrapper -> {
                    Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
                    if (container != null && container.getMessageData().getDemographics().getVoided() == 0) {
                        Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                        return artLineListGeneratorService.mapARTLineList(container, facility, cutOff, quarter);
                    } else {
                        log.info("Voided patient with id = {}", container != null ? container.getId() : null);
                        return null;
                    }
                })
                .filter(artLinelist -> artLinelist != null && artLinelist.getPatientUniqueId() != null)
                .forEach(artLineListRepository::save);
    }

}
