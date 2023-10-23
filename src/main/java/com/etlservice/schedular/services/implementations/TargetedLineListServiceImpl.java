package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.linelists.TargetedLineList;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.model.Partition;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.TargetedLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.TargetedLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetedLineListServiceImpl implements TargetedLineListService {
    private final TargetedLineListRepository targetedLineListRepository;
    private final HelperFunctions helperFunctions;
    private final ContainerRepository containerRepository;
    private final ExecutorService executorService;
    private final FacilityRepository facilityRepository;

    @Override
    public void processTargetedLineList(String fileName) {
        List<String> datimCodes = Arrays.asList("AlHWYsy5u3m","Ro8QYYh2EVH","KLx83uYKVcC","PRLTfziBO1L","vzy7MBuPWQ0","OsIeuStyqTh","tyyZQSN5D4p","PIM6KHQXxrB","FH7LMnbnVlT","Ke1y8vsmeC4","KFbRZKvXpb3","s0SI9pIe68w","Y3VlUBi4kD9",
                "FjX5IC6wJ1m","GW1w1chZMPR","IEE3UZcwPu3","meYf9FxUI4c","SHF865XzjPJ", "wp753KYAdno");
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "Targeted");
        lineListTracker.setPageSize(1000);
        int page = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        boolean isLastPage = false;
        while (!isLastPage) {
            log.info("Processing page {} of {}", page+1, lineListTracker.getTotalPages());
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<IdWrapper> idWrapperPage = containerRepository.findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodes, 4, pageable);
            log.info("Found {} containers", idWrapperPage.getTotalElements());
            log.info("Total pages {}", idWrapperPage.getTotalPages());
            List<IdWrapper> idWrappers = idWrapperPage.getContent();
            Partition<IdWrapper> partition = Partition.ofSize(idWrappers, 100);
            CountDownLatch latch = new CountDownLatch(partition.size());
            partition.forEach(part -> executorService.execute(()-> {
                processTargetedLineList(fileName, part);
                latch.countDown();
            }));
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            helperFunctions.updateLineListTracker(lineListTracker, ++page, idWrapperPage, idWrappers);
            isLastPage = idWrapperPage.isLast();
        }
        lineListTracker.setStatus(PROCESSED.name());
        lineListTracker.setDateCompleted(LocalDateTime.now());
        helperFunctions.saveLineListTracker(lineListTracker);
        log.info("Completed processing Targeted Line List");
    }

    @Override
    public void processTargetedLineList(String filePath, List<IdWrapper> idWrappers) {
        int column = 0;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workBook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workBook.getSheetAt(0);
            idWrappers.forEach(idWrapper -> {
                Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
                if (container != null) {
                    String pepfarId = helperFunctions.returnIdentifiers(4, container).orElse(null);
                    if (pepfarId != null) {
                        Optional<TargetedLineList> optionalTargetedLineList = targetedLineListRepository.findFirstByArtUniqueIdAndDatimCode(pepfarId, container.getMessageHeader().getFacilityDatimCode());
                        if (!optionalTargetedLineList.isPresent()) {
                            for (Row row : sheet) {
                                Cell cell = row.getCell(column);
                                if (cell != null && cell.getCellType() == CellType.STRING) {
                                    String cellValue = cell.getStringCellValue();
                                    if (cellValue.equals(pepfarId)) {
                                        String datimCode = container.getMessageHeader().getFacilityDatimCode();
                                        Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
                                        List<ObsType> obsTypes = container.getMessageData().getObs();
                                        obsTypes.stream()
                                                .filter(obsType -> obsType.getVoided() == 0)
                                                .forEach(obsType -> {
                                                    TargetedLineList targetedLineList = new TargetedLineList();
                                                    targetedLineList.setArtUniqueId(pepfarId);
                                                    targetedLineList.setState(facility.getState().getStateName());
                                                    targetedLineList.setLga(facility.getLga().getLga());
                                                    targetedLineList.setFacility(facility.getFacilityName());
                                                    targetedLineList.setDatimCode(datimCode);
                                                    targetedLineList.setForm(obsType.getPmmForm());
                                                    targetedLineList.setConceptId(obsType.getConceptId());
                                                    targetedLineList.setObsGroupId(obsType.getObsGroupId());
                                                    targetedLineList.setVariableName(obsType.getVariableName());
                                                    String variableValue;
                                                    if (obsType.getVariableValue() != null) {
                                                        variableValue = obsType.getVariableValue();
                                                    } else if (obsType.getValueText() != null) {
                                                        variableValue = obsType.getValueText();
                                                    } else if (obsType.getValueDatetime() != null) {
                                                        variableValue = obsType.getValueDatetime().toString();
                                                    } else {
                                                        variableValue = null;
                                                    }
                                                    targetedLineList.setVariableValue(variableValue);
                                                    targetedLineList.setVisitDate(convertDate(obsType.getObsDatetime()));
                                                    targetedLineList.setCreator(obsType.getCreator());
                                                    targetedLineList.setDateCreated(convertDate(obsType.getDateCreated()));

                                                    targetedLineListRepository.save(targetedLineList);
                                                });
                                        row.createCell(1).setCellValue("Processed");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
