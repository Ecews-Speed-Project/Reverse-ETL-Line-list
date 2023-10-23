package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.linelists.BaselineViralLoadLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.State;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.BaselineViralLoadLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.read.StateRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.BaselineViralLoadLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.*;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaselineViralLoadLineListServiceImpl implements BaselineViralLoadLineListService {
    private final ContainerRepository containerRepository;
    private final BaselineViralLoadLineListRepository baselineViralLoadLineListRepository;
    private final HelperFunctions helperFunctions;
    private final FacilityRepository facilityRepository;
    private final StateRepository stateRepository;

    @Override
    public void processBaselineViralLoadLineList() {
        List<State> stateList = stateRepository.findAll();
        stateList.forEach(state -> {
            List<String> stateDatimCodes = facilityRepository.findStateFacilitiesDatimCodes(state.getId());
            LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "BASELINE_VL");
            lineListTracker.setPageSize(1000);
            int currentPage = lineListTracker.getCurrentPage();
            int pageSize = lineListTracker.getPageSize();
            while (true) {
                log.info("Processing page {} of {}", currentPage + 1, lineListTracker.getTotalPages());
                Pageable pageable = PageRequest.of(currentPage, pageSize);
                Page<IdWrapper> idWrapperPage = containerRepository
                        .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                            stateDatimCodes, 4, pageable
                        );
                if (!idWrapperPage.hasContent()) {
                    lineListTracker.setStatus(PROCESSED.name());
                    lineListTracker.setDateCompleted(LocalDateTime.now());
                    helperFunctions.saveLineListTracker(lineListTracker);
                    break;
                }
                List<IdWrapper> idWrapperList = idWrapperPage.getContent();
                processBaselineViralLoadLineList(idWrapperList);
                lineListTracker = helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, idWrapperPage, idWrapperList);
            }
        });
    }

    @Override
    public void processBaselineViralLoadLineList(List<IdWrapper> idWrappers) {
//        Date cutOff = Date.from(LocalDate.of(2023, 4, 30).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        Date cutOff = new Date();
        idWrappers.forEach(idWrapper -> {
            Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
            if (container != null) {
                BaselineViralLoadLineList baselineViralLoadLineList = baselineViralLoadLineListRepository
                        .findByPatientUuidAndDatimCode(container.getId(), container.getMessageHeader().getFacilityDatimCode())
                        .orElse(new BaselineViralLoadLineList());
                Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                baselineViralLoadLineList.setState(facility.getState().getStateName());
                baselineViralLoadLineList.setLga(facility.getLga().getLga());
                baselineViralLoadLineList.setDatimCode(facility.getDatimCode());
                baselineViralLoadLineList.setFacilityName(facility.getFacilityName());
                baselineViralLoadLineList.setPatientUniqueId(helperFunctions.returnIdentifiers(4, container).orElse(null));
                baselineViralLoadLineList.setPatientUuid(container.getId());
                Date artStartDate = helperFunctions.getArtStartDate(container, cutOff);
                baselineViralLoadLineList.setArtStartDate(artStartDate != null ? convertDate(artStartDate) : null);
                Date lastPickupDate = null;
                Long daysOfArvRefill = null;
                String patientOutcome = null;
                Optional<ObsType> lastPickupObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, new Date());
                if (lastPickupObs.isPresent()) {
                    ObsType obsType = lastPickupObs.get();
                    lastPickupDate = lastPickupObs.get().getObsDatetime();
                    Optional<ObsType> daysOfArvObs1 = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, new Date());
                    if (daysOfArvObs1.isPresent()) {
                        daysOfArvRefill = daysOfArvObs1.get().getValueNumeric() != null ? daysOfArvObs1.get().getValueNumeric().longValue() : null;
                    }
                }
                Optional<ObsType> patientOutComeObs = helperFunctions.getMaxConceptObsIdWithFormId(
                        CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container, new Date());
                if (patientOutComeObs.isPresent()) {
                    patientOutcome = patientOutComeObs.get().getVariableValue();
                }
                String currentArtStatus = helperFunctions.getCurrentArtStatus(lastPickupDate, daysOfArvRefill, new Date(), patientOutcome);
                baselineViralLoadLineList.setCurrentArtStatus(currentArtStatus);
                baselineViralLoadLineList.setAgeAtStartOfArt(Math.toIntExact(helperFunctions.getAgeAtStartOfARTYears(container, cutOff)));
                Date dob = container.getMessageData().getDemographics().getBirthdate();
                baselineViralLoadLineList.setDateOfBirth(dob != null ? convertDate(dob) : null);
                Optional<ObsType> viralLoadObs = helperFunctions.getMinConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, cutOff);
                viralLoadObs.ifPresent(obsType -> {
                    baselineViralLoadLineList.setViralLoadResult(obsType.getValueNumeric() != null ?
                            obsType.getValueNumeric().doubleValue() : null);
                    Optional<ObsType> sampleCollectionObs = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 159951, container, cutOff);
                    if (sampleCollectionObs.isPresent()) {
                        ObsType obsType1 = sampleCollectionObs.get();
                        baselineViralLoadLineList.setLastViralLoadSampleCollectionDateDocumentedResult(
                                obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null
                        );
                    } else {
                        baselineViralLoadLineList.setLastViralLoadSampleCollectionDateDocumentedResult(convertDate(obsType.getObsDatetime()));
                    }
                });
                Optional<ObsType> recencyObs = helperFunctions.getMaxConceptObsIdWithFormId(75, 166213, container, cutOff);
                recencyObs.ifPresent(obsType -> {
                    baselineViralLoadLineList.setRecencyTestResult(obsType.getVariableValue());
                    Optional<ObsType> recencyDateObs = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 165850, container, cutOff);
                    recencyDateObs.ifPresent(obsType1 ->
                            baselineViralLoadLineList.setRecencyTestDate(obsType1.getValueDatetime() != null ?
                                    convertDate(obsType1.getValueDatetime()) : null)
                    );
                });
                baselineViralLoadLineList.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, cutOff)
                        .map(ObsType::getVariableValue).orElse(null));
                baselineViralLoadLineList.setKpType(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, cutOff)
                        .map(ObsType::getVariableValue).orElse(null));
                Optional<ObsType> initialCd4Opt = helperFunctions.getMinConceptObsIdWithFormId(
                        LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
                );
                initialCd4Opt.ifPresent(obsType -> {
                    baselineViralLoadLineList.setCd4Result(obsType.getValueNumeric() != null ?
                            obsType.getValueNumeric().doubleValue() : null);
                    baselineViralLoadLineList.setCd4Date(convertDate(obsType.getObsDatetime()));
                });
                baselineViralLoadLineListRepository.save(baselineViralLoadLineList);
            }
        });
    }
}
