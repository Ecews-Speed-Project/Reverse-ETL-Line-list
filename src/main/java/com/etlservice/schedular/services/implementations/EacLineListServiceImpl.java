package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.linelists.EacLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.State;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.EacLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.read.StateRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.EacLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.*;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EacLineListServiceImpl implements EacLineListService {
    private final FacilityRepository facilityRepository;
    private final EacLineListRepository eacLineListRepository;
    private final RabbitTemplate rabbitTemplate;
    private final StateRepository stateRepository;
    private final HelperFunctions helperFunctions;
    private final ContainerRepository containerRepository;

    @Override
    public void processEacLineList() {
        List<State> states = stateRepository.findAll();
        states.forEach(state -> {
            log.info("Processing EAC line list for {}", state.getStateName());
            boolean status = true;
            LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), state.getStateName() +"_EAC");
//            LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "EAC");
            lineListTracker.setPageSize(1000);
            int page = lineListTracker.getCurrentPage();
            int size = lineListTracker.getPageSize();
//            List<String> datimCodes = facilityRepository.findFctFacilitiesDatimCodes();
            List<String> datimCodes = facilityRepository.findStateFacilitiesDatimCodes(state.getId());
//            List<String> datimCodes = new ArrayList<>(Collections.singletonList("meYf9FxUI4c"));
//            List<String> datimCodes = new ArrayList<>(Arrays.asList("LYlPd52expn", "Ahcb9XhcWsi","xKvwaYWM2BS", "PLT2H2gBSYT","ZVyH4YGTvGM","G5maKPztL5K","Q2Jqt7sTbPg"));
            while (status) {
                log.info("page = {}", page + 1);
                Page<IdWrapper> containerPage = containerRepository.findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodes, 4, Pageable.ofSize(size).withPage(page));
                log.info("containerPage = {}", containerPage.getContent().size());
                log.info("container total = {}", containerPage.getTotalElements());
                List<IdWrapper> containerList = containerPage.getContent();
                processEacLineList(containerList);
                status = containerPage.hasNext();
                helperFunctions.updateLineListTracker(lineListTracker, ++page, containerPage, containerList);
            }
            lineListTracker.setStatus(PROCESSED.name());
            lineListTracker.setDateCompleted(LocalDateTime.now());
            helperFunctions.saveLineListTracker(lineListTracker);
            log.info("Running scheduled task to build custom line list :: done");
        });

    }

    @Override
    public void processEacLineList(List<IdWrapper> containers) {
        containers.forEach(idWrapper -> {
            Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
            if (container != null) {
//                log.info(container.getMessageHeader().getFacilityName());
//                if (container.getMessageHeader().getTouchTime() != null) {
//                    Date lastPickupDate = null;
//                    Long daysOfArvRefill = null;
//                    String patientOutcome = null;
//                    Optional<ObsType> lastPickupObs1 = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, new Date());
//                    if (lastPickupObs1.isPresent()) {
//                        ObsType obsType = lastPickupObs1.get();
//                        lastPickupDate = lastPickupObs1.get().getObsDatetime();
//                        Optional<ObsType> daysOfArvObs1 = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, new Date());
//                        if (daysOfArvObs1.isPresent()) {
//                            daysOfArvRefill = daysOfArvObs1.get().getValueNumeric() != null ? daysOfArvObs1.get().getValueNumeric().longValue() : null;
//                        }
//                    }
//                    Optional<ObsType> patientOutComeObs = helperFunctions.getMaxConceptObsIdWithFormId(
//                            CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container, new Date());
//                    if (patientOutComeObs.isPresent()) {
//                        patientOutcome = patientOutComeObs.get().getVariableValue();
//                    }
//                    String currentArtStatus = helperFunctions.getCurrentArtStatus(lastPickupDate, daysOfArvRefill, new Date(), patientOutcome);
//                    Double viralLoad = null;
//                    Optional<ObsType> currentViralLoadObs = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, new Date());
//                    if (currentViralLoadObs.isPresent()) {
//                        viralLoad = currentViralLoadObs.get().getValueNumeric() != null ? currentViralLoadObs.get().getValueNumeric().doubleValue() : null;
//                    }
//                    log.info("Status: {}", currentArtStatus);
//                    log.info("Viral load: {}", viralLoad);
//                    if (currentArtStatus.equals("Active") && viralLoad != null && viralLoad > 50) {
                        String datimCode = container.getMessageHeader().getFacilityDatimCode();
                        Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
//                        EacLineList eacLineList = eacLineListRepository.findByPatientUuidAndDatimCode(container.getId(), datimCode)
//                                .orElse(null);
                        Optional<EacLineList> optionalEacLineList = eacLineListRepository.findByPatientUuidAndDatimCode(container.getId(), datimCode);
                        optionalEacLineList.ifPresent(eacLineList -> {
                            eacLineList.setState(facility.getState().getStateName());
                            eacLineList.setLga(facility.getLga().getLga());
                            eacLineList.setDatimCode(datimCode);
                            eacLineList.setFacilityName(facility.getFacilityName());
                            eacLineList.setPatientUniqueId(helperFunctions.returnIdentifiers(4, container)
                                    .orElse(""));
                            eacLineList.setPatientHospitalNo(helperFunctions.returnIdentifiers(5, container)
                                    .orElse("")
                                    .replaceFirst("^0+(?!$)", ""));
                            eacLineList.setSex(container.getMessageData().getDemographics().getGender());
                            Optional<Date> artStartDateObs = helperFunctions.getStartOfArt(container, new Date());
                            artStartDateObs.ifPresent(artStartDate -> eacLineList.setArtStartDate(artStartDate
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                            Long ageAtStartOfARTYears = helperFunctions.getAgeAtStartOfARTYears(container, new Date());
                            if (ageAtStartOfARTYears != null) {
                                if (ageAtStartOfARTYears >= 1)
                                    eacLineList.setAgeAtStartOfArtYears(ageAtStartOfARTYears.intValue());
                                else
                                    eacLineList.setAgeAtStartOfArtMonths(helperFunctions.getAgeAtStartOfARTMonths(container, new Date()).intValue());
                            }
                            eacLineList.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, new Date())
                                    .map(ObsType::getVariableValue).orElse(null));
                            eacLineList.setKpType(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, new Date())
                                    .map(ObsType::getVariableValue).orElse(null));
                            Optional<ObsType> lastPickupObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, new Date());
                            lastPickupObs.ifPresent(obsType -> {
                                eacLineList.setLastPickupDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                                Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, new Date());
                                daysOfArvObs.ifPresent(obsType1 -> eacLineList.setDaysOfArvRefill(obsType1.getValueNumeric() != null ? obsType1.getValueNumeric().intValue() : null));

                                Optional<ObsType> dddDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166363, container, new Date());
                                dddDispensingModality.ifPresent(obsType1 -> eacLineList.setDddDispensingModality(obsType1.getVariableValue()));

                                Optional<ObsType> dispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166148, container, new Date());
                                dispensingModality.ifPresent(obsType1 -> eacLineList.setDispensingModality(obsType1.getVariableValue()));

                                Optional<ObsType> facilityDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166276, container, new Date());
                                facilityDispensingModality.ifPresent(obsType1 -> eacLineList.setFacilityDispensingModality(obsType1.getVariableValue()));

                                Optional<ObsType> mmdType = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166278, container, new Date());
                                mmdType.ifPresent(obsType1 -> eacLineList.setMmdType(obsType1.getVariableValue()));
                            });
                            eacLineList.setMonthsOnArt(helperFunctions.getMonthsOnArt(container, eacLineList.getLastPickupDate() != null ? Date.from(eacLineList.getLastPickupDate()
                                    .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null, new Date()).intValue());
                            eacLineList.setDateTransferredIn(helperFunctions.getMaxObsByConceptID(160534, container, new Date())
                                    .map(obsType -> obsType.getValueDatetime() != null ? obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null)
                                    .orElse(null));
                            eacLineList.setTransferInStatus(helperFunctions.getMaxObsByConceptID(165242, container, new Date())
                                    .map(ObsType::getVariableValue).orElse(null));
                            Optional<Date> lastVisitDate = helperFunctions.getMaxVisitDate(container, new Date());
                            lastVisitDate.ifPresent(date -> eacLineList.setLastVisitDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                            Optional<ObsType> maxObsByConceptID1 = helperFunctions.getMaxConceptObsIdWithFormId(27, 166406, container, new Date());
                            maxObsByConceptID1.ifPresent(obsType -> eacLineList.setPillBalance(NumberUtils.isDigits(obsType.getValueText()) ? Integer.parseInt(obsType.getValueText()) : null));
                            Optional<ObsType> currentRegimenLine = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 165708, container, new Date());
                            currentRegimenLine.ifPresent(obsType -> {
                                eacLineList.setCurrentRegimenLine(obsType.getVariableValue());
                                Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, new Date());
                                currentRegimenType.ifPresent(obsType1 -> eacLineList.setCurrentRegimen(obsType1.getVariableValue()));
                            });
                            if (eacLineList.getSex() != null && (eacLineList.getSex().equals("F") || eacLineList.getSex().equalsIgnoreCase("Female"))) {
                                Optional<ObsType> pregnancyStatus = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 165050, container, new Date());
                                pregnancyStatus.ifPresent(obsType -> {
                                    eacLineList.setPregnancyStatus(obsType.getVariableValue());

                                    eacLineList.setPregnancyStatusDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                                    Optional<ObsType> edd = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 5596, container, new Date());
                                    edd.ifPresent(obsType1 -> eacLineList.setEdd(obsType.getValueDatetime() != null ? obsType1.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
                                });
                            }
                            Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, new Date());
                            currentViralLoad.ifPresent(obsType -> {
                                eacLineList.setCurrentViralLoad(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);
                                eacLineList.setViralLoadEncounterDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                                Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 159951, container, new Date());
                                if (viralLoadSampleCollectionDate.isPresent()) {
                                    ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                    eacLineList.setViralLoadSampleCollectionDate(obsType1.getValueDatetime() != null ? obsType1.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
                                } else {
                                    eacLineList.setViralLoadSampleCollectionDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                                }

                                Optional<ObsType> viralLoadIndication = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 164980, container, new Date());
                                viralLoadIndication.ifPresent(obsType1 -> eacLineList.setViralLoadIndication(obsType1.getVariableValue()));
                                Optional<ObsType> reportDate3 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 165414, container, new Date());
                                eacLineList.setViralLoad3(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);
                                Date viralLoad3Date = obsType.getObsDatetime();
                                eacLineList.setViralLoad3SampleDate(eacLineList.getViralLoadSampleCollectionDate());
                                reportDate3.ifPresent(obsType1 -> eacLineList.setViralLoad3ReportDate(obsType1.getValueDatetime() != null ?
                                        convertDate(obsType1.getValueDatetime()) : null));
                                Optional<ObsType> viralLoad2 = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, viralLoad3Date);
                                viralLoad2.ifPresent(obsType1 -> {
                                    eacLineList.setViralLoad2(obsType1.getValueNumeric() != null ? obsType1.getValueNumeric().doubleValue() : null);
                                    Date viralLoad2Date = obsType1.getObsDatetime();
                                    Optional<ObsType> viralLoadSampleCollectionDate2 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType1.getEncounterId(), 159951, container, new Date());
                                    if (viralLoadSampleCollectionDate2.isPresent()) {
                                        ObsType obsType2 = viralLoadSampleCollectionDate2.get();
                                        eacLineList.setViralLoad2SampleDate(obsType2.getValueDatetime() != null ? convertDate(obsType2.getValueDatetime()) : null);
                                    } else {
                                        eacLineList.setViralLoad2SampleDate(convertDate(obsType1.getObsDatetime()));
                                    }
                                    Optional<ObsType> reportDate2 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType1.getEncounterId(), 165414, container, new Date());
                                    reportDate2.ifPresent(obsType2 -> eacLineList.setViralLoad2ReportDate(obsType2.getValueDatetime() != null ?
                                            convertDate(obsType2.getValueDatetime()) : null));
                                    Optional<ObsType> viralLoad1 = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 856, container, viralLoad2Date);
                                    viralLoad1.ifPresent(obsType2 -> {
                                        eacLineList.setViralLoad1(obsType2.getValueNumeric() != null ? obsType2.getValueNumeric().doubleValue() : null);
                                        Optional<ObsType> viralLoadSampleCollectionDate1 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType2.getEncounterId(), 159951, container, new Date());
                                        if (viralLoadSampleCollectionDate1.isPresent()) {
                                            ObsType obsType3 = viralLoadSampleCollectionDate1.get();
                                            eacLineList.setViralLoad1SampleDate(obsType3.getValueDatetime() != null ? convertDate(obsType3.getValueDatetime()) : null);
                                        } else {
                                            eacLineList.setViralLoad1SampleDate(convertDate(obsType2.getObsDatetime()));
                                        }
                                        Optional<ObsType> reportDate1 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType2.getEncounterId(), 165414, container, new Date());
                                        reportDate1.ifPresent(obsType3 -> eacLineList.setViralLoad1ReportDate(obsType3.getValueDatetime() != null ?
                                                convertDate(obsType3.getValueDatetime()) : null));
                                    });
                                });
                            });
                            Optional<ObsType> initialSecondLineRegimen = helperFunctions.getInitialRegimenLine(164513, 164514, container);
                            initialSecondLineRegimen.ifPresent(obsType -> eacLineList.setSecondLineRegimenStartDate(convertDate(obsType.getObsDatetime())));
                            Optional<ObsType> initialThirdLineRegimen = helperFunctions.getInitialRegimenLine(165702, 165703, container);
                            initialThirdLineRegimen.ifPresent(obsType -> eacLineList.setThirdLineRegimenStartDate(convertDate(obsType.getObsDatetime())));
                            Optional<ObsType> lastSampleTakenDate = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 159951, container, new Date());
                            lastSampleTakenDate.ifPresent(obsType -> eacLineList.setLastSampleTakenDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime()
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
                            Optional<ObsType> patientOutCome = helperFunctions.getMaxConceptObsIdWithFormId(
                                    CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container, new Date());
                            if (patientOutCome.isPresent()) {
                                eacLineList.setPatientOutcome(patientOutCome.get().getVariableValue());
                                eacLineList.setPatientOutcomeDate(patientOutCome.get().getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            }
                            String status = helperFunctions.getCurrentArtStatus(eacLineList.getLastPickupDate() != null ? Date.from(eacLineList.getLastPickupDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null,
                                    eacLineList.getDaysOfArvRefill() != null ? eacLineList.getDaysOfArvRefill().longValue() : null, new Date(), eacLineList.getPatientOutcome());
                            eacLineList.setCurrentArtStatus(status);
                            Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 5096, container, new Date());
                            pharmacyNextAppointment.ifPresent(obsType ->
                                    eacLineList.setPharmacyNextAppointmentDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
                            Optional<ObsType> clinicalNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 5096, container, new Date());
                            clinicalNextAppointment.ifPresent(obsType ->
                                    eacLineList.setClinicalNextAppointmentDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
                            eacLineList.setCurrentAgeYears(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, new Date()));
                            if (eacLineList.getCurrentAgeYears() < 5)
                                eacLineList.setCurrentAgeMonths(helperFunctions.getCurrentAge(container, AGE_TYPE_MONTHS, new Date()));

                            eacLineList.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate()
                                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            Date eac3Date = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obs -> obs.getConceptId() == 166097 &&
                                            obs.getFormId() == 69 &&
                                            obs.getValueCoded() == 165645 &&
                                            obs.getVoided() == 0)
                                    .max(Comparator.comparing(ObsType::getObsDatetime))
                                    .map(ObsType::getObsDatetime)
                                    .orElse(null);
                            eacLineList.setEac3Date(eac3Date != null ? convertDate(eac3Date) : null);
                            Date eac2Date = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obs -> obs.getConceptId() == 166097 &&
                                            obs.getFormId() == 69 &&
                                            obs.getValueCoded() == 165644 &&
                                            obs.getVoided() == 0)
                                    .max(Comparator.comparing(ObsType::getObsDatetime))
                                    .map(ObsType::getObsDatetime)
                                    .orElse(null);
                            eacLineList.setEac2Date(eac2Date != null ? convertDate(eac2Date) : null);
                            Date eac1Date = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obs -> obs.getConceptId() == 166097 &&
                                            obs.getFormId() == 69 &&
                                            obs.getValueCoded() == 165643 &&
                                            obs.getVoided() == 0)
                                    .max(Comparator.comparing(ObsType::getObsDatetime))
                                    .map(ObsType::getObsDatetime)
                                    .orElse(null);
                            eacLineList.setEac1Date(eac1Date != null ? convertDate(eac1Date) : null);
                            Date eac4Date = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obs -> obs.getConceptId() == 166097 &&
                                            obs.getFormId() == 69 &&
                                            obs.getValueCoded() == 5622 &&
                                            obs.getVoided() == 0)
                                    .max(Comparator.comparing(ObsType::getObsDatetime))
                                    .map(ObsType::getObsDatetime)
                                    .orElse(null);
                            eacLineList.setEac4Date(eac4Date != null ? convertDate(eac4Date) : null);
                            eacLineList.setPatientUuid(container.getId());
                            Optional<ObsType> lastEacSession = helperFunctions.getMaxConceptObsIdWithFormId(69, 166097, container, new Date());
                            lastEacSession.ifPresent(obsType -> {
                                eacLineList.setLastEacSessionType(obsType.getVariableValue());
                                eacLineList.setLastEacSessionDate(obsType.getObsDatetime() != null ? convertDate(obsType.getObsDatetime()) : null);
                            });
                            Optional<ObsType> lastEacBarriers = helperFunctions.getMaxConceptObsIdWithFormId(69, 165457, container, new Date());
                            lastEacBarriers.ifPresent(obsType -> eacLineList.setLastEacBarriersToAdherence(obsType.getVariableValue()));
                            Optional<ObsType> lastEacRegimenPlan = helperFunctions.getMaxConceptObsIdWithFormId(69, 165771, container, new Date());
                            lastEacRegimenPlan.ifPresent(obsType -> eacLineList.setLastEacRegimenPlan(obsType.getVariableValue()));
                            Optional<ObsType> lastEacFollowupDate = helperFunctions.getMaxConceptObsIdWithFormId(69, 165036, container, new Date());
                            lastEacFollowupDate.ifPresent(obsType -> eacLineList.setLastEacFollowupDate(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                            Optional<ObsType> lastEacCounsellorComments = helperFunctions.getMaxConceptObsIdWithFormId(69, 165606, container, new Date());
                            lastEacCounsellorComments.ifPresent(obsType -> eacLineList.setLastEacAdherenceCounsellorComments(obsType.getValueText()));
                            try {
                                eacLineListRepository.save(eacLineList);
                            } catch (Exception e) {
                                log.info("Error saving line list::{}", e.getMessage());
                                log.info("Line list with the issue\n{}", eacLineList);
                            }
                        });
//                    }
//                } else
//                    log.info("patient found with uuid::{}, no touch time", container.getId());
            }
        });
    }
}
