package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.linelists.RegimenLineList;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.RegimenLineListRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.RegimenLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.PHARMACY_FORM;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegimenLineListServiceImpl implements RegimenLineListService {
    private final RegimenLineListRepository regimenLineListRepository;
    private final FacilityRepository facilityRepository;
    private final ContainerRepository containerRepository;
    private final HelperFunctions helperFunctions;

    @Override
    public void processRegimenLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(),"Regimen");
        lineListTracker.setPageSize(1000);
        int currentPage = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        List<String> datimCodes = facilityRepository.findFctFacilitiesDatimCodes();
        while (true) {
            log.info("Processing Regimen Line List Page: " + currentPage);
            Pageable pageable = Pageable.ofSize(pageSize).withPage(currentPage);
            Page<IdWrapper> idWrapperPage =
                    containerRepository
                            .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                                    datimCodes, 4, pageable);
            if (idWrapperPage.hasContent()) {
                log.info("Total Containers: " + idWrapperPage.getTotalElements());
                log.info("Total Pages: " + idWrapperPage.getTotalPages());
                List<IdWrapper> idWrapperList = idWrapperPage.getContent();
                processRegimenLineList(idWrapperList);
                helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, idWrapperPage, idWrapperList);
            } else {
                lineListTracker.setStatus(PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                helperFunctions.saveLineListTracker(lineListTracker);
                break;
            }
        }
    }

    @Override
    public void processRegimenLineList(List<IdWrapper> containers) {
        List<Integer> regimenConceptIds = new ArrayList<>(Arrays.asList(164506, 164513, 165702, 164507, 164514, 165705));
//        String filePath = "C:\\Users\\innoc\\Documents\\linelist_problem_uids (1).xlsx";
        String filePath = "C:\\Users\\support\\Downloads\\UIDs without documented regimen prior to first VL.xlsx";
        int column = 0;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workBook = new XSSFWorkbook(fileInputStream)){
            Sheet sheet = workBook.getSheetAt(0);
            containers.forEach(idWrapper -> {
                Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
                if (container != null) {
                    String pepfarId = helperFunctions.returnIdentifiers(4, container).orElse(null);
                    if (pepfarId != null) {
                        for (Row row : sheet) {
                            Cell cell = row.getCell(column);
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                String cellValue = cell.getStringCellValue();
                                if (cellValue.equals(pepfarId)) {
                                    String datimCode = container.getMessageHeader().getFacilityDatimCode();
                                    Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
                                    List<ObsType> obsTypeList = container.getMessageData().getObs().stream()
                                            .filter(obs -> obs.getVoided() == 0 &&
                                                    obs.getFormId() == PHARMACY_FORM &&
                                                    regimenConceptIds.contains(obs.getConceptId()))
                                            .sorted(Comparator.comparing(obsType -> convertDate(obsType.getObsDatetime())))
                                            .collect(Collectors.toList());
                                    if (!obsTypeList.isEmpty()) {
                                        LocalDate artStartDate = null;
                                        Optional<Date> artStartDateObs = helperFunctions.getStartOfArt(container, new Date());
                                        if (artStartDateObs.isPresent()) {
                                            artStartDate = convertDate(artStartDateObs.get());
                                        }
                                        LocalDate finalArtStartDate = artStartDate;
                                        obsTypeList.forEach(obsType -> {
                                            RegimenLineList regimenLineList = new RegimenLineList();
                                            regimenLineList.setArtStartDate(finalArtStartDate);
                                            regimenLineList.setPepfarId(pepfarId);
                                            regimenLineList.setFacilityName(facility.getFacilityName());
                                            regimenLineList.setRegimen(obsType.getVariableValue());
                                            regimenLineList.setRegimenDate(convertDate(obsType.getObsDatetime()));
                                            regimenLineListRepository.save(regimenLineList);
                                        });
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error reading file: " + e.getMessage());
        }
    }
}
