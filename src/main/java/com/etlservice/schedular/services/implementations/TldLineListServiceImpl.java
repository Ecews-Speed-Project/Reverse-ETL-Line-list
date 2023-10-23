package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.linelists.TldLineList;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.EncounterType;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.TldLineListRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.TldLineListService;
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

import static com.etlservice.schedular.utils.ConstantsUtils.LABORATORY_ORDER_AND_RESULT_FORM;
import static com.etlservice.schedular.utils.ConstantsUtils.PHARMACY_FORM;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TldLineListServiceImpl implements TldLineListService {
    private final TldLineListRepository tldLineListRepository;
    private final FacilityRepository facilityRepository;
    private final ContainerRepository containerRepository;
    private final HelperFunctions helperFunctions;

    @Override
    public void createTldLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker("PROCESSING","TLD");
        lineListTracker.setPageSize(1000);
        int currentPage = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        List<String> datimCodes = facilityRepository.findFctFacilitiesDatimCodes();
        while (true) {
            log.info("Processing TLD Line List Page: " + currentPage);
            Pageable pageable = Pageable.ofSize(pageSize).withPage(currentPage);
            Page<IdWrapper> idWrapperPage =
                    containerRepository
                            .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                                    datimCodes, 4, pageable);
            if (idWrapperPage.hasContent()) {
                log.info("Total Containers: " + idWrapperPage.getTotalElements());
                log.info("Total Pages: " + idWrapperPage.getTotalPages());
                List<IdWrapper> idWrapperList = idWrapperPage.getContent();
                createTldLineList(idWrapperList);
                helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, idWrapperPage, idWrapperList);
            } else {
                log.info("No more containers to process");
                lineListTracker.setStatus("PROCESSED");
                lineListTracker.setDateCompleted(LocalDateTime.now());
                helperFunctions.saveLineListTracker(lineListTracker);
                break;
            }
        }
    }

    @Override
    public void createTldLineList(List<IdWrapper> idWrappers) {
        List<Integer> regimenConceptIds = new ArrayList<>(Arrays.asList(164506, 164513, 165702, 164507, 164514, 165705));
//        String filePath = "C:\\Users\\innoc\\Documents\\linelist_problem_uids (1).xlsx";
        String filePath = "C:\\Users\\support\\Documents\\linelist_problem_uids (1).xlsx";
//        String filePath = "C:\\Users\\innoc\\Downloads\\Patient ID.xlsx";
        int column = 3;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workBook = new XSSFWorkbook(fileInputStream)){
            Sheet sheet = workBook.getSheetAt(0);
            idWrappers.forEach(idWrapper -> {
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
                                    Optional<ObsType> tldObs = container.getMessageData().getObs().stream()
                                            .filter(obs -> (obs.getConceptId() == 164506 || obs.getConceptId() == 164507) &&
                                                    obs.getValueCoded() == 165681 &&
                                                    obs.getFormId() == PHARMACY_FORM &&
                                                    obs.getVoided() == 0)
                                            .min(Comparator.comparing(ObsType::getObsDatetime)); // Get the earliest TLD
                                    if (tldObs.isPresent()) {
                                        LocalDate tldDate = convertDate(tldObs.get().getObsDatetime());
                                        LocalDate artStartDate = null;
                                        Optional<Date> artStartDateObs = helperFunctions.getStartOfArt(container, new Date());
                                        if (artStartDateObs.isPresent()) {
                                            artStartDate = convertDate(artStartDateObs.get());
                                        }
                                        LocalDate finalArtStartDate = artStartDate;
                                        Set<EncounterType> encounterTypes = container.getMessageData().getEncounters().stream()
                                                .filter(encounter -> convertDate(encounter.getEncounterDatetime()).isEqual(tldDate) ||
                                                        convertDate(encounter.getEncounterDatetime()).isAfter(tldDate))
                                                .sorted(Comparator.comparing(EncounterType::getEncounterDatetime))
                                                .collect(Collectors.toCollection(LinkedHashSet::new));
                                        encounterTypes.forEach(encounterType -> {
                                            LocalDate visitDate = convertDate(encounterType.getEncounterDatetime());
                                            TldLineList tldLineList = new TldLineList();
                                            tldLineList.setFacilityName(facility.getFacilityName());
                                            tldLineList.setIdentifier(pepfarId);
                                            tldLineList.setArtStartDate(finalArtStartDate);
                                            tldLineList.setFirstTldDate(tldDate);
                                            tldLineList.setVisit(visitDate);
                                            Optional<ObsType> regimenObs = container.getMessageData().getObs()
                                                    .stream()
                                                    .filter(obs -> regimenConceptIds.contains(obs.getConceptId()) &&
                                                            obs.getVoided() == 0 &&
                                                            obs.getFormId() == PHARMACY_FORM &&
                                                            convertDate(obs.getObsDatetime()).isEqual(visitDate))
                                                    .findFirst();
                                            regimenObs.ifPresent(obsType -> {
                                                tldLineList.setRegimen(obsType.getVariableValue());
                                                tldLineList.setRegimenDate(convertDate(obsType.getObsDatetime()));
                                            });
                                            Optional<ObsType> viralLoadObs = container.getMessageData().getObs()
                                                    .stream()
                                                    .filter(obsType -> obsType.getConceptId() == 856 &&
                                                            obsType.getVoided() == 0 &&
                                                            obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                                            convertDate(obsType.getObsDatetime()).isEqual(visitDate))
                                                    .findFirst();
                                            viralLoadObs.ifPresent(obsType -> {
                                                tldLineList.setViralLoad(obsType.getValueNumeric().doubleValue());
                                                Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions
                                                        .getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, new Date());
                                                if (viralLoadSampleCollectionDate.isPresent()) {
                                                    ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                                    tldLineList.setViralLoadSampleCollectionDate(obsType1.getValueDatetime() != null ?
                                                            convertDate(obsType1.getValueDatetime()) : convertDate(obsType.getObsDatetime()));
                                                } else {
                                                    tldLineList.setViralLoadSampleCollectionDate(convertDate(obsType.getObsDatetime()));
                                                }
                                            });
                                            tldLineListRepository.save(tldLineList);
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
            log.error("Error: " + e.getMessage());
        }
    }
}
