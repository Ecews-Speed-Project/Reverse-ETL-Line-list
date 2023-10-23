/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.etlservice.schedular.messaging;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.State;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.read.StateRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.*;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.enums.LineListType.CUSTOM;

/**
 *
 * @author MORRISON.I
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ETLConsumer implements CommandLineRunner {
    private final ARTLineListETL artLineListETL;
    private final CustomLineListService customLineListService;
    private final RefreshLineListService refreshLineListService;
    private final FacilityRepository facilityRepository;
    private final ContainerRepository containerRepository;
    private final HelperFunctions helperFunctions;
    private final RabbitTemplate rabbitTemplate;
    private final EacLineListService eacLineListService;
    private final EnhancedArtLineListService enhancedArtLineListService;
    private final StateRepository stateRepository;
    private final ExecutorService executorService;
    private final HtsLineListService htsLineListService;
    private final SampleDataService sampleDataService;
    private final ViralLoadLineListService viralLoadLineListService;
    private final AhdLineListService ahdLineListService;
    private final RegimenLineListService regimenLineListService;
    private final TldLineListService tldLineListService;
    private final BiometricService biometricService;
    private final BaselineViralLoadLineListService baselineViralLoadLineListService;
    private final TargetedLineListService targetedLineListService;

//    @RabbitListener(queues = "${etl.queue}")
    public void receive(List<Container> containerList) {
        log.info("Receiver 1 running {}", containerList.size());
        artLineListETL.buildDailyArtLineList(containerList);
    }

    @RabbitListener(queues = "${etl.queue}")
    public void receive2(List<IdWrapper> containerList) {
        log.info("Receiver 2 running {}", containerList.size());
        artLineListETL.buildDailyArtLineList(containerList, LocalDate.now());
    }

//    @RabbitListener(queues = "${daily.etl.queue}")
    public void receive3(List<IdWrapper> containerList) {
        log.info("Receiver 3 running {}", containerList.size());
//        customLineListService.processMortalityLineList(containerList);
//        customLineListService.processFctDataSetLineList(containerList);
        eacLineListService.processEacLineList(containerList);
//        enhancedArtLineListService.buildEnhancedArtLineList(containerList);
//        htsLineListService.extractHtsData(containerList);
//        viralLoadLineListService.buildViralLoadLineList(containerList);
    }

//    @RabbitListener(queues = "${daily.etl.queue}")
    public void receive4(List<IdWrapper> containerList) {
        log.info("Receiver 4 running {}", containerList.size());
//        customLineListService.processFctDataSetLineList(containerList);
//        customLineListService.processMortalityLineList(containerList);
        eacLineListService.processEacLineList(containerList);
//        enhancedArtLineListService.buildEnhancedArtLineList(containerList);
//        htsLineListService.extractHtsData(containerList);
//        viralLoadLineListService.buildViralLoadLineList(containerList);
    }

//    @Scheduled(cron = "0/10 * * * * *")
    public void buildCustomLineList () {
        log.info("Running scheduled task to build custom line list :: start" );
//        customLineListService.processLineList();
        eacLineListService.processEacLineList();
    }

//    @Scheduled(cron = "0 0 0 1 */3 ?") // to run every 3 months
    public void refreshQuarterlyArtLineList() {
        log.info("Refreshing the line list for the new quarter");
        String string = refreshLineListService.refreshQuarterlyArtLineList();
        log.info(string);
    }

//    @Scheduled(cron = "0 0 0 2-31 * ?") // to run every day
    public void refreshDailyArtLineList() {
        log.info("Refreshing the line list for the new day");
        String resp = refreshLineListService.refreshDailyArtLineList();
        log.info("Response = {}", resp);
    }

//    @Scheduled(cron = "0/10 * * * * *")
    public void getContainers () {
        log.info("Running scheduled task to get containers :: start" );
        String result = artLineListETL.getContainersByIdentifierType(4);
        log.info("Result = {}", result);
    }

    public void generateCustomLineList() {
        List<String> datimCodes = new ArrayList<>(Arrays.asList("KVdLBmuVcrZ", "KFbRZKvXpb3"));
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), CUSTOM.name()+"_ART");
        lineListTracker.setPageSize(1000);
        int page = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        boolean hasMore = true;
        while (hasMore) {
            log.info("Page = {}", page+1);
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<IdWrapper> idWrapperPage = containerRepository.findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodes, 4, pageable);
            if (idWrapperPage.hasContent()) {
                List<IdWrapper> idWrapperList = idWrapperPage.getContent();
                log.info("idWrapperList size = {}", idWrapperList.size());
                LocalDate currentDate = LocalDate.of(2023, 6, 30);
                artLineListETL.buildCustomArtLineList(idWrapperList, currentDate);
                lineListTracker = helperFunctions.updateLineListTracker(lineListTracker, ++page, idWrapperPage, idWrapperList);
                log.info("Line list tracker updated");
            } else {
                hasMore = false;
                log.info("No more content");
                lineListTracker.setStatus(PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTracker = helperFunctions.saveLineListTracker(lineListTracker);
                log.info("Line list tracker saved");
            }
        }


//        List<State> stateList = stateRepository.findAll();
//        CountDownLatch latch = new CountDownLatch(stateList.size());
//        stateList.forEach(state -> executorService.execute(() -> {
//            log.info("Processing state {}", state.getStateName());
//            List<String> datimCodes = facilityRepository.findStateFacilitiesDatimCodes(state.getId());
//            log.info("datimCodes size = {}", datimCodes.size());
//            LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), state.getStateName()+"_"+CUSTOM.name());
//            lineListTracker.setPageSize(1000);
//            int page = lineListTracker.getCurrentPage();
//            int pageSize = lineListTracker.getPageSize();
//            boolean hasMore = true;
//            while (hasMore) {
//                log.info("Page = {}", page+1);
//                Pageable pageable = PageRequest.of(page, pageSize);
//                Page<IdWrapper> idWrapperPage = containerRepository.findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodes, 4, pageable);
//                if (idWrapperPage.hasContent()) {
//                    List<IdWrapper> idWrapperList = idWrapperPage.getContent();
//                    log.info("idWrapperList size = {}", idWrapperList.size());
//                    artLineListETL.buildCustomArtLineList(idWrapperList);
//                    lineListTracker = helperFunctions.updateLineListTracker(lineListTracker, ++page, idWrapperPage, idWrapperList);
//                    log.info("Line list tracker updated");
//                } else {
//                    hasMore = false;
//                    log.info("No more content");
//                    lineListTracker.setStatus(PROCESSED.name());
//                    lineListTracker.setDateCompleted(LocalDateTime.now());
//                    lineListTracker = helperFunctions.saveLineListTracker(lineListTracker);
//                    log.info("Line list tracker saved");
//                }
//            }
//            latch.countDown();
//        }));
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

//    @Scheduled(cron = "0/10 * * * * *")
    public void getContainers2 () {
        log.info("Running scheduled task to get containers :: start" );
//        List<String> datimCodeList = new ArrayList<>(Arrays.asList("yutnuUG0Y6L", "gBbkJ5wBQqq", "Ts6CTTaA2Ug", "Y3VlUBi4kD9"));
//        log.info("datimCodeList size = {}", datimCodeList.size());
//        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), CUSTOM.name());
//        lineListTracker.setPageSize(1000);
//        int page = lineListTracker.getCurrentPage();
//        int pageSize = lineListTracker.getPageSize();
//        boolean hasMore = true;
//        while (hasMore) {
//            log.info("Page = {}", page+1);
//            Pageable pageable = PageRequest.of(page, pageSize);
//            Page<Container> containerPage = containerRepository.findContainersByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodeList, 4, pageable);
//            if (containerPage.hasContent()) {
//                List<Container> containerList = containerPage.getContent();
//                log.info("Container list size = {}", containerList.size());
//                log.info("totalElements = {}", containerPage.getTotalElements());
//                log.info("totalPages = {}", containerPage.getTotalPages());
//                List<List<Container>> partitionList = Partition.ofSize(containerList, 100);
//                partitionList.forEach(partition -> {
//                    log.info("Partition size = {}", partition.size());
//                    rabbitTemplate.convertAndSend("daily_etl_queue", partition);
//                });
//                helperFunctions.updateLineListTracker(lineListTracker, ++page, containerPage, containerPage.getContent());
//            } else {
//                hasMore = false;
//                lineListTracker.setStatus(PROCESSED.name());
//                lineListTracker.setDateCompleted(LocalDateTime.now());
//                helperFunctions.saveLineListTracker(lineListTracker);
//            }
//        }
        List<State> stateList = stateRepository.findAll();
        CountDownLatch latch = new CountDownLatch(stateList.size());
        log.info("State list size = {}", stateList.size());
        stateList.forEach(state -> executorService.execute(() -> {
            List<String> stateDatimCodeList = facilityRepository.findStateFacilitiesDatimCodes(state.getId());
            log.info("State = {}, stateDatimCodeList size = {}", state.getStateName(), stateDatimCodeList.size());
            LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), state.getStateName()+"_ART");
//            lineListTracker.setPageSize(500);
            int page = lineListTracker.getCurrentPage();
            int pageSize = lineListTracker.getPageSize();
            boolean status = true;
            while (status) {
                log.info("Page = {}", page + 1);
                Pageable pageable = PageRequest.of(page, pageSize);
                Page<IdWrapper> containerPage = containerRepository
                        .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                                stateDatimCodeList, 4, pageable
                        );
                if (containerPage == null || containerPage.getContent().isEmpty()) {
                    status = false;
                    continue;
                }
                log.info("Total pages = {}", containerPage.getTotalPages());
                log.info("Container page size = {}", containerPage.getContent().size());
                List<IdWrapper> idWrapperList = containerPage.getContent();
                artLineListETL.buildNewQuarterArtLineList(idWrapperList);
                status = containerPage.hasNext();
                helperFunctions.updateLineListTracker(lineListTracker, ++page, containerPage, containerPage.getContent());
            }
            lineListTracker.setStatus(PROCESSED.name());
            lineListTracker.setDateCompleted(LocalDateTime.now());
            helperFunctions.saveLineListTracker(lineListTracker);
            latch.countDown();
            log.info("Done processing state = {}", state.getStateName());
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Running scheduled task to get containers :: end" );
    }

    public void extractHtsData() {
        List<Integer> formIds = new ArrayList<>(Arrays.asList(10, 75));
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "HTS");
        lineListTracker.setPageSize(1000);
        int page = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        boolean hasMore = true;
        while (hasMore) {
            log.info("Page = {}", page + 1);
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<IdWrapper> containerPage = containerRepository.findContainerIdsByMessageDataEncountersFormIdIn(formIds, pageable);
            if (containerPage.hasContent()) {
                log.info("Total pages = {}", containerPage.getTotalPages());
                log.info("Total elements = {}", containerPage.getTotalElements());
                List<IdWrapper> containerList = containerPage.getContent();
                htsLineListService.extractHtsData(containerList);
//                List<List<Container>> partitionList = Partition.ofSize(containerList, 100);
//                partitionList.forEach(partition -> rabbitTemplate.convertAndSend("daily_etl_queue", partition));
                helperFunctions.updateLineListTracker(lineListTracker, ++page, containerPage, containerPage.getContent());
            } else {
                hasMore = false;
                lineListTracker.setStatus(PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                helperFunctions.saveLineListTracker(lineListTracker);
            }
        }
    }

    @Override
    public void run(String... args) {
        log.info("Running scheduled task to get containers :: start" );
//        getContainers2();
//        buildCustomLineList();
//        extractHtsData();
//        viralLoadLineListService.fetchViralLoadLineList();
        refreshQuarterlyArtLineList();
//        ahdLineListService.processAhdLineList();
//        generateCustomLineList();
//        sampleDataService.fetchSampleData();
//        refreshLineListService.cleanArtLineList();
//        regimenLineListService.processRegimenLineList();
//        tldLineListService.createTldLineList();
//        eacLineListService.processEacLineList();
//        biometricService.processBiometricLineList();
//        baselineViralLoadLineListService.processBaselineViralLoadLineList();
//        String filePath = "C:\\Users\\innoc\\Downloads\\Patient ID.xlsx";
//        String filePath = "C:\\Users\\support\\Documents\\Patient ID.xlsx";
//        targetedLineListService.processTargetedLineList(filePath);
        log.info("Running scheduled task to get containers :: end" );
    }
}
