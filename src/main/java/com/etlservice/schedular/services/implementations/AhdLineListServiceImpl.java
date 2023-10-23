package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.linelists.AhdLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.AhdLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.AhdLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.*;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AhdLineListServiceImpl implements AhdLineListService {
    private final ContainerRepository containerRepository;
    private final FacilityRepository facilityRepository;
    private final AhdLineListRepository ahdLineListRepository;
    private final HelperFunctions helperFunctions;

    @Override
    public void processAhdLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "AHD");
        lineListTracker.setPageSize(2000);
//        List<String> facilityDatimCodes = facilityRepository.findFctFacilitiesDatimCodes();
//        List<String> facilityDatimCodes = Collections.singletonList("Vbs05uX6Y1i");
        List<String> facilityDatimCodes = facilityRepository.findStateFacilitiesDatimCodes(2);
        int currentPage = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        boolean isLastPage = false;
        while (!isLastPage) {
            log.info("Processing page: {}", currentPage + 1);
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<IdWrapper> containerIds = containerRepository
                    .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                            facilityDatimCodes, 4, pageable);
            if (!containerIds.hasContent()) {
                isLastPage = true;
                lineListTracker.setStatus(PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                helperFunctions.saveLineListTracker(lineListTracker);
            } else {
                log.info("Total pages: " + containerIds.getTotalPages());
                log.info("Total elements: " + containerIds.getTotalElements());
                buildAhdLineList(containerIds.getContent());
                helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, containerIds, containerIds.getContent());
            }
        }
    }

    @Override
    public void buildAhdLineList(List<IdWrapper> containerIds) {
//        LocalDate start = LocalDate.of(2022, 12, 31);
//        LocalDate marchCutOff = LocalDate.of(2023, 4, 1);
//        Date globalCutOff = Date.from(marchCutOff.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
//        LocalDate febCutOff = LocalDate.of(2023, 3, 1);
//        LocalDate janCutOff = LocalDate.of(2023, 2, 1);
//        List<LocalDate> cutOffs = new ArrayList<>(Arrays.asList(marchCutOff, febCutOff, janCutOff));
        log.info("Processing " + containerIds.size() + " containers");
//        log.info("cutOffs: " + cutOffs);
        AtomicInteger counter = new AtomicInteger(0);
        containerIds.forEach(idWrapper -> {
            String containerId = idWrapper.getId();
            Optional<Container> optionalContainer = containerRepository.findById(containerId);
            optionalContainer.ifPresent(container -> {
                if (container.getMessageData().getDemographics().getVoided() == 0) {
                    String artStatus;
                    String patientOutcome = null;
                    LocalDate patientOutcomeDate = null;
                    LocalDate artStartLocalDate;
                    LocalDate lastPickupLocalDate = null;
                    Integer daysOfArv = null;
//                for (LocalDate cutOffDate : cutOffs) {
                    Date cutOff = Date.from(LocalDate.of(2023, 6, 1)
                            .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    Date artStartDate = helperFunctions.getArtStartDate(container, cutOff);
                    Date lastPickupDate = null;
//                    if (artStartDate != null) {
                    artStartLocalDate = artStartDate == null ? null : convertDate(artStartDate);
                    Optional<ObsType> lastPickupObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                    if (lastPickupObs.isPresent()) {
                        ObsType obsType = lastPickupObs.get();
                        lastPickupDate = obsType.getObsDatetime();
                        lastPickupLocalDate = convertDate(lastPickupDate);
                        Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                        if (daysOfArvObs.isPresent()) {
                            daysOfArv = daysOfArvObs.get().getValueNumeric() == null ?
                                    null : daysOfArvObs.get().getValueNumeric().intValue();
                        }
                    }
                    Optional<ObsType> patientOutComeObs = helperFunctions.getMaxConceptObsIdWithFormId(
                            CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container, cutOff);
                    if (patientOutComeObs.isPresent()) {
                        patientOutcome = patientOutComeObs.get().getVariableValue();
                        patientOutcomeDate = convertDate(patientOutComeObs.get().getObsDatetime());
                    }
                    Long daysOfRefill = daysOfArv != null ?
                            Long.valueOf(daysOfArv) : null;
                    artStatus = helperFunctions.getCurrentArtStatus(lastPickupDate, daysOfRefill, cutOff, patientOutcome);
//                        if (artStatus.equals("Active")) {
//                            counter.getAndIncrement();
                    Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                    String patientUniqueId = helperFunctions.returnIdentifiers(4, container).orElse("");
                    AhdLineList ahdLineList = ahdLineListRepository.findByPatientUniqueIdAndDatimCode(patientUniqueId, facility.getDatimCode())
                            .orElse(new AhdLineList());
                    ahdLineList.setPatientUniqueId(patientUniqueId);
                    ahdLineList.setState(facility.getState().getStateName());
                    ahdLineList.setLga(facility.getLga().getLga());
                    ahdLineList.setFacilityName(facility.getFacilityName());
                    ahdLineList.setDatimCode(facility.getDatimCode());
                    ahdLineList.setPatientHospitalNo(helperFunctions.returnIdentifiers(5, container)
                            .orElse(""));
                    ahdLineList.setSex(container.getMessageData().getDemographics().getGender());
                    ahdLineList.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate() != null ?
                            convertDate(container.getMessageData().getDemographics().getBirthdate()) : null);
                    ahdLineList.setArtStartDate(artStartLocalDate);
                    ahdLineList.setLastPickupDate(lastPickupLocalDate);
                    ahdLineList.setDaysOfArvRefill(daysOfArv);
//                    Date lastEacDate = container.getMessageData().getEncounters().stream()
//                            .filter(encounterType -> encounterType.getFormId() == 69 &&
//                                    encounterType.getVoided() == 0 &&
//                                    encounterType.getEncounterDatetime() != null &&
//                                    convertDate(encounterType.getEncounterDatetime()).isBefore(marchCutOff) &&
//                                    convertDate(encounterType.getEncounterDatetime()).isAfter(start))
//                            .map(EncounterType::getEncounterDatetime)
//                            .max(Date::compareTo).orElse(null);//helperFunctions.getMaxEncounterDateTime(69, container, globalCutOff);
//                            ahdLineList.setLastEacDate(lastEacDate != null ? convertDate(lastEacDate) : null);
                    Optional<ObsType> currentRegimenLine = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 165708, container, cutOff);
                    currentRegimenLine.ifPresent(obsType -> {
                        ahdLineList.setCurrentRegimenLine(obsType.getVariableValue());
                        Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
                        currentRegimenType.ifPresent(obsType1 -> ahdLineList.setCurrentRegimen(obsType1.getVariableValue()));
                    });
                    Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, cutOff);
                    currentViralLoad.ifPresent(obsType -> {
                        ahdLineList.setCurrentViralLoad(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                        ahdLineList.setViralLoadEncounterDate(convertDate(obsType.getObsDatetime()));

                        Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 159951, container, cutOff);
                        if (viralLoadSampleCollectionDate.isPresent()) {
                            ObsType obsType1 = viralLoadSampleCollectionDate.get();
                            ahdLineList.setViralLoadSampleCollectionDate(obsType1.getValueDatetime() != null ?
                                    convertDate(obsType1.getValueDatetime()) : convertDate(obsType1.getObsDatetime()));
                        } else {
                            ahdLineList.setViralLoadSampleCollectionDate(convertDate(obsType.getObsDatetime()));
                        }
                    });
                    Optional<ObsType> currentPatientOutComeObs = helperFunctions.getMaxConceptObsIdWithFormId(
                            CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container, cutOff);
                    if (currentPatientOutComeObs.isPresent()) {
                        patientOutcome = currentPatientOutComeObs.get().getVariableValue();
                        patientOutcomeDate = convertDate(currentPatientOutComeObs.get().getObsDatetime());
                    }
                    ahdLineList.setPatientOutcome(patientOutcome);
                    ahdLineList.setPatientOutcomeDate(patientOutcomeDate);
                    ahdLineList.setCurrentArtStatus(helperFunctions.getCurrentArtStatus(lastPickupDate, daysOfRefill, cutOff, patientOutcome));
                    if (artStartLocalDate != null)
                        ahdLineList.setMonthsOnArt((int) ChronoUnit.MONTHS.between(
                                artStartLocalDate, ahdLineList.getCurrentArtStatus().equals("Active") || lastPickupLocalDate == null ?
                                        convertDate(cutOff) : lastPickupLocalDate));
                    Optional<ObsType> cd4LfaObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167088 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .min(Comparator.comparing(ObsType::getObsDatetime));
                    cd4LfaObs.ifPresent(obsType -> {
                        ahdLineList.setFirstCd4LfaResult(obsType.getVariableValue());
                        ahdLineList.setFirstCd4LfaResultDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> xpertObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167070 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    xpertObs.ifPresent(obsType -> {
                        ahdLineList.setXpertMtbRifRequest(obsType.getVariableValue());
                        ahdLineList.setXpertMtbRifRequestDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> tbObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                    obsType.getConceptId() == 1659 &&
                                    obsType.getValueCoded() == 1662 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    tbObs.ifPresent(obsType -> ahdLineList.setTbTreatment(obsType.getVariableValue()));
                    Optional<ObsType> tbDateObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == 56 &&
                                    obsType.getConceptId() == 1113 &&
                                    obsType.getVoided() == 0 &&
                                    obsType.getValueDatetime() != null &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    tbDateObs.ifPresent(obsType -> ahdLineList.setTbTreatmentDate(convertDate(obsType.getValueDatetime())));
                    Optional<ObsType> serologyObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167090 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                            &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    serologyObs.ifPresent(obsType -> {
                        ahdLineList.setSerologyForCrAg(obsType.getVariableValue());
                        ahdLineList.setSerologyForCrAgDate(convertDate(obsType.getObsDatetime()));
                    });

                    Optional<ObsType> csfObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167082 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    csfObs.ifPresent(obsType -> {
                        ahdLineList.setCsfForCrAg(obsType.getVariableValue());
                        ahdLineList.setCsfForCrAgDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> fluconazoleObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                    obsType.getConceptId() == 165727 &&
                                    obsType.getValueCoded() == 76488 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    fluconazoleObs.ifPresent(obsType -> {
                        ahdLineList.setFluconazoleTreatment(obsType.getVariableValue());
                        ahdLineList.setFluconazoleTreatmentDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> timeCd4CollectedObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167091 &&
                                    obsType.getVoided() == 0 &&
                                    obsType.getValueDatetime() != null &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    timeCd4CollectedObs.ifPresent(obsType ->
                            ahdLineList.setTimeCd4LfaSampleCollected(obsType.getValueDatetime()));
                    Optional<ObsType> timeCd4ReceivedObs = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167092 &&
                                    obsType.getVoided() == 0 &&
                                    obsType.getValueDatetime() != null &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    timeCd4ReceivedObs.ifPresent(obsType ->
                            ahdLineList.setTimeCd4LfaResultReceived(obsType.getValueDatetime()));
//                    Optional<ObsType> labRegobs = container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
//                                    obsType.getConceptId() == 165394 &&
//                                    obsType.getVoided() == 0 &&
//                                    obsType.getValueText() != null &&
//                                    convertDate(obsType.getObsDatetime()).isBefore(marchCutOff) &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start))
//                            .max(Comparator.comparing(ObsType::getObsDatetime));
//                    labRegobs.ifPresent(obsType -> ahdLineList.setLaboratoryRegistrationNo(obsType.getValueText()));
                    Optional<ObsType> indicationForAhd = container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                    obsType.getConceptId() == 167079 &&
                                    obsType.getVoided() == 0 &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .max(Comparator.comparing(ObsType::getObsDatetime));
                    indicationForAhd.ifPresent(obsType -> ahdLineList.setIndicationForAhd(obsType.getVariableValue()));
//                    Optional<ObsType> lastCd4Obs = container.getMessageData().getObs()
//                            .stream()
//                            .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
//                                    obsType.getConceptId() == 5497 &&
//                                    obsType.getVoided() == 0 &&
//                                    obsType.getValueNumeric() != null &&
//                                    convertDate(obsType.getObsDatetime()).isBefore(marchCutOff) &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start))
//                            .max(Comparator.comparing(ObsType::getObsDatetime));
//                    lastCd4Obs.ifPresent(obsType -> {
//                        ahdLineList.setLastCd4Count(obsType.getValueNumeric().doubleValue());
//                        ahdLineList.setLastCd4CountDate(convertDate(obsType.getObsDatetime()));
//                    });
                    Optional<ObsType> firstWhoStagingObs = container.getMessageData().getObs()
                            .stream()
                            .filter(obsType -> obsType.getFormId() == ART_COMMENCEMENT_FORM &&
                                    obsType.getConceptId() == 5356 &&
                                    obsType.getVoided() == 0 &&
                                    obsType.getVariableValue() != null &&
                                    convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
//                                    &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start)
                            )
                            .min(Comparator.comparing(ObsType::getObsDatetime));
                    firstWhoStagingObs.ifPresent(obsType -> {
                        ahdLineList.setFirstWhoStaging(obsType.getVariableValue());
                        ahdLineList.setFirstWhoStagingDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> confirmedPosObs = helperFunctions.getMaxConceptObsIdWithFormId(23, 160554, container, cutOff);
                    confirmedPosObs.ifPresent(obsType -> ahdLineList.setDateConfirmedPositive(obsType.getValueDatetime() != null ?
                            convertDate(obsType.getValueDatetime()) : null));
                    Optional<ObsType> initialCd4Opt = helperFunctions.getMinConceptObsIdWithFormId(
                            LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
                    );
                    initialCd4Opt.ifPresent(obsType -> {
                        ahdLineList.setFirstCd4CountNumeric(obsType.getValueNumeric() == null ?
                                null : obsType.getValueNumeric().doubleValue());
                        ahdLineList.setFirstCd4CountNumericDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> firstTbStatusObs = helperFunctions.getMinConceptObsIdWithFormId(CARE_CARD, 1659, container, cutOff);
                    firstTbStatusObs.ifPresent(obsType -> {
                        ahdLineList.setFirstTbStatus(obsType.getVariableValue());
                        ahdLineList.setFirstTbStatusDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> tblFlamObs = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 166697, container,cutOff);
                    tblFlamObs.ifPresent(obsType -> {
                        ahdLineList.setTblFlamResult(obsType.getVariableValue());
                        ahdLineList.setTblFlamResultDate(convertDate(obsType.getObsDatetime()));
                    });
                    Optional<ObsType> ctxInhStartObs = helperFunctions.getMinConceptObsIdWithFormId(PHARMACY_FORM, 165727, container, cutOff);
                    ctxInhStartObs.ifPresent(obsType -> {
                        int valueCoded = obsType.getValueCoded();
                        if (valueCoded == 165257)
                            ahdLineList.setCtxStartDate(convertDate(obsType.getObsDatetime()));
                        else if (valueCoded == 1679) {
                            ahdLineList.setInhStartDate(convertDate(obsType.getObsDatetime()));
                        }
                    });
//                    List<Integer> conceptIds = new ArrayList<>(Arrays.asList(164507, 164514, 165703, 164506, 164513, 165702));
                    Optional<ObsType> firstRegimenObs = container.getMessageData().getObs()
                            .stream()
                            .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                            obsType.getConceptId() == 165708 &&
                                            obsType.getVoided() == 0 &&
                                            convertDate(obsType.getObsDatetime()).isBefore(convertDate(cutOff))
                                    )
                            .min(Comparator.comparing(ObsType::getObsDatetime));
                    firstRegimenObs.ifPresent(obsType -> {
                        ahdLineList.setFirstRegimenLine(obsType.getVariableValue());
                        Optional<ObsType> initialReg = helperFunctions.getInitialRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
                        initialReg.ifPresent(obsType1 -> {
                            ahdLineList.setFirstRegimen(obsType1.getVariableValue());
                            ahdLineList.setFirstRegimenPickupDate(convertDate(obsType1.getObsDatetime()));
                        });
                    });
                    Optional<ObsType> serologyForCrAgCollectedObs = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 167093, container, cutOff);
                    serologyForCrAgCollectedObs.ifPresent(obsType -> ahdLineList.setTimeSerologyForCrAgSampleCollected(obsType.getValueDatetime() != null ?
                            obsType.getValueDatetime() : null));
                    Optional<ObsType> serologyForCrAgReceivedObs = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 167093, container, cutOff);
                    serologyForCrAgReceivedObs.ifPresent(obsType -> ahdLineList.setTimeSerologyForCrAgResultReceived(obsType.getValueDatetime() != null ?
                            obsType.getValueDatetime() : null));
//                    Optional<ObsType> lastWhoStagingObs = container.getMessageData().getObs()
//                            .stream()
//                            .filter(obsType -> obsType.getFormId() == ENROLLMENT_TYPE &&
//                                    obsType.getConceptId() == 5356 &&
//                                    obsType.getVoided() == 0 &&
//                                    obsType.getVariableValue() != null &&
//                                    convertDate(obsType.getObsDatetime()).isBefore(marchCutOff) &&
//                                    convertDate(obsType.getObsDatetime()).isAfter(start))
//                            .max(Comparator.comparing(ObsType::getObsDatetime));
//                    lastWhoStagingObs.ifPresent(obsType -> {
//                        ahdLineList.setLastWhoStaging(obsType.getVariableValue());
//                        ahdLineList.setLastWhoStagingDate(convertDate(obsType.getObsDatetime()));
//                    });
                    ahdLineListRepository.save(ahdLineList);
//                            break;
//                        }
//                    }
//                }
                }
            });
        });
        log.info("Finished processing AHD Line List ::" + counter.get() + " records");
    }
}
