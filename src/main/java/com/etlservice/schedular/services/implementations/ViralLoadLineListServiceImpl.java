package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.linelists.ViralLoadLineList;
import com.etlservice.schedular.model.*;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.ViralLoadLineListRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.ViralLoadLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.*;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViralLoadLineListServiceImpl implements ViralLoadLineListService {
    private final ContainerRepository containerRepository;
    private final HelperFunctions helperFunctions;
    private final RabbitTemplate rabbitTemplate;
    private final ViralLoadLineListRepository viralLoadLineListRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public void fetchViralLoadLineList() {
        List<String> datimCodes = facilityRepository.findFctFacilitiesDatimCodes();
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(),"ViralLoad");
        lineListTracker.setPageSize(1000);
        int currentPage = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        boolean hasMore = true;
        while (hasMore) {
            log.info("Fetching ViralLoad LineList for page: {}", currentPage + 1);
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<IdWrapper> containerPage = containerRepository
                    .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(datimCodes, 4, pageable);
            if (!containerPage.hasContent()) {
                hasMore = false;
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTracker.setStatus(PROCESSED.name());
                helperFunctions.saveLineListTracker(lineListTracker);
            } else {
                log.info("Total pages = " + containerPage.getTotalPages());
                List<IdWrapper> containers = containerPage.getContent();
                buildViralLoadLineList(containers);
//                List<List<Container>> partitions = Partition.ofSize(containers, 50);
//                partitions.forEach(partition -> rabbitTemplate.convertAndSend("daily_etl_queue", partition));
                helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, containerPage, containers);
            }
        }
    }

    @Override
    public void buildViralLoadLineList(List<IdWrapper> containers) {
        Date start = Date.from(LocalDate.of(2019, 1, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        containers.forEach(idWrapper -> {
            Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
            if (container != null) {
                String datimCode = container.getMessageHeader().getFacilityDatimCode();
                Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
                Date cutOff = new Date();
                String state = facility.getState().getStateName();
                String lga = facility.getLga().getLga();
                String patientUuid = container.getId();
                String patientUniqueId = helperFunctions.returnIdentifiers(4, container).orElse("");
                String patientHospitalNo = helperFunctions.returnIdentifiers(5, container).orElse("");
                String sex = container.getMessageData().getDemographics().getGender();
                LocalDate dateOfBirth = convertDate(container.getMessageData().getDemographics().getBirthdate());
                long ageAtStartOfArtYears = helperFunctions.getAgeAtStartOfARTYears(container, cutOff);
                Long ageAtStartOfArtMonths = null;
                if (ageAtStartOfArtYears < 1)
                    ageAtStartOfArtMonths = helperFunctions.getAgeAtStartOfARTMonths(container, cutOff);
                Integer currentAgeYears = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
                Integer currentAgeMonths = null;
                if (currentAgeYears < 5)
                    currentAgeMonths = helperFunctions.getCurrentAge(container, AGE_TYPE_MONTHS, cutOff);
                Date enrollDate = helperFunctions.getEnrollmentDate(container);
                LocalDate enrollmentDate = enrollDate != null ? convertDate(enrollDate) : null;
                LocalDate dateConfirmedPositive = helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 160554, container, cutOff)
                        .map(ObsType::getValueDatetime)
                        .map(HelperFunctions::convertDate)
                        .orElse(null);
                String careEntryPoint = helperFunctions.getMaxObsByConceptID(160540, container, cutOff)
                        .map(ObsType::getVariableValue).orElse(null);
                String kpType = helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, cutOff)
                        .map(ObsType::getVariableValue).orElse(null);
                LocalDate dateTransferredIn = helperFunctions.getMaxObsByConceptID(160534, container, cutOff)
                        .filter(obsType -> obsType.getValueDatetime() != null)
                        .map(obsType -> convertDate(obsType.getValueDatetime()))
                        .orElse(null);
                String transferInStatus = helperFunctions.getMaxObsByConceptID(165242, container, cutOff)
                        .map(ObsType::getVariableValue).orElse(null);
                Date artStartDate1 = helperFunctions.getArtStartDate(container, cutOff);
                LocalDate artStartDate = artStartDate1 != null ? convertDate(artStartDate1) : null;
                List<EncounterType> encounterTypes = container.getMessageData().getEncounters();
//                Set<EncounterType> encounterTypes = container.getMessageData().getEncounters()
//                        .stream()
//                        .filter(encounterType -> encounterType.getEncounterDatetime().after(start))
//                        .collect(Collectors.toSet());
                Long finalAgeAtStartOfArtMonths = ageAtStartOfArtMonths;
                Integer finalCurrentAgeMonths = currentAgeMonths;
                encounterTypes.forEach(encounterType -> {
                    LocalDate visitDate = convertDate(encounterType.getEncounterDatetime());
                    ViralLoadLineList viralLoadLineList1 = viralLoadLineListRepository
                            .findByPatientUuidAndDatimCodeAndVisitDate(patientUuid, datimCode, visitDate)
                            .orElse(null);
                    if (viralLoadLineList1 == null) {
                        ViralLoadLineList viralLoadLineList = new ViralLoadLineList();
                        viralLoadLineList.setPatientUuid(patientUuid);
                        viralLoadLineList.setPatientUniqueId(patientUniqueId);
                        viralLoadLineList.setPatientHospitalNo(patientHospitalNo);
                        viralLoadLineList.setState(state);
                        viralLoadLineList.setLga(lga);
                        viralLoadLineList.setDatimCode(datimCode);
                        viralLoadLineList.setFacilityName(facility.getFacilityName());
                        viralLoadLineList.setSex(sex);
                        viralLoadLineList.setDateOfBirth(dateOfBirth);
                        viralLoadLineList.setAgeAtStartOfArtYears((int) ageAtStartOfArtYears);
                        viralLoadLineList.setAgeAtStartOfArtMonths(finalAgeAtStartOfArtMonths != null ?
                                finalAgeAtStartOfArtMonths.intValue() : null);
                        viralLoadLineList.setCurrentAgeYears(currentAgeYears);
                        viralLoadLineList.setCurrentAgeMonths(finalCurrentAgeMonths);
                        viralLoadLineList.setEnrollmentDate(enrollmentDate);
                        viralLoadLineList.setDateConfirmedPositive(dateConfirmedPositive);
                        viralLoadLineList.setCareEntryPoint(careEntryPoint);
                        viralLoadLineList.setKpType(kpType);
                        Optional<ObsType> pickUpDuringVisitDateObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                        obsType.getConceptId() == 162240 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .findAny();
                        Date pickUpDuringVisitDate = null;
                        if (dateConfirmedPositive != null) {
                            viralLoadLineList.setTimeSinceDiagnosis((int) ChronoUnit.MONTHS.between(dateConfirmedPositive, visitDate));
                        }
                        if (pickUpDuringVisitDateObs.isPresent()) {
                            ObsType obsType = pickUpDuringVisitDateObs.get();
                            pickUpDuringVisitDate = obsType.getObsDatetime();
                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> viralLoadLineList.setDaysOfArvRefill(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().intValue()));
                            Optional<ObsType> pillBalanceObs = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obsType1 -> obsType1.getFormId() == PHARMACY_FORM &&
                                            obsType1.getConceptId() == 166406 &&
                                            convertDate(obsType1.getObsDatetime()).equals(visitDate) &&
                                            obsType1.getValueText() != null &&
                                            obsType1.getVoided() == 0)
                                    .findAny();
                            pillBalanceObs.ifPresent(obsType1 -> viralLoadLineList.setPillBalance(obsType1.getValueText() != null &&
                                    NumberUtils.isDigits(obsType1.getValueText()) ?
                                    Integer.parseInt(obsType1.getValueText()) : null));
                            Optional<ObsType> dispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166148, container, cutOff);
                            dispensingModality.ifPresent(obsType1 -> viralLoadLineList.setDispensingModality(obsType1.getVariableValue()));
                        }
                        Optional<ObsType> lastPickUpBeforeVisitObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, visitDate);
                        Date lastPickUpBeforeVisit = lastPickUpBeforeVisitObs.map(ObsType::getObsDatetime).orElse(null);
                        if (artStartDate != null)
                            viralLoadLineList.setMonthsOnArt((int) ChronoUnit.MONTHS.between(artStartDate, visitDate));
                        viralLoadLineList.setDateTransferredIn(dateTransferredIn);
                        viralLoadLineList.setTransferInStatus(transferInStatus);
                        viralLoadLineList.setArtStartDate(artStartDate);
                        viralLoadLineList.setVisitDate(visitDate);
                        Optional<ObsType> regimenLineObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                        obsType.getConceptId() == 165708 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVariableValue() != null &&
                                        !obsType.getVariableValue().isEmpty() &&
                                        obsType.getVoided() == 0)
                                .findAny();
                        regimenLineObs.ifPresent(obsType -> {
                            String regimenLine = obsType.getVariableValue();
                            viralLoadLineList.setRegimenLine(regimenLine);
                            Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
                            currentRegimenType.ifPresent(obsType1 -> viralLoadLineList.setRegimenPickedUp(obsType1.getVariableValue()));
                        });
                        Optional<ObsType> cd4CountObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                        obsType.getConceptId() == 5497 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getValueNumeric() != null)
                                .findAny();
                        cd4CountObs.ifPresent(obsType -> {
                            viralLoadLineList.setCd4Count(obsType.getValueNumeric().doubleValue());
                            viralLoadLineList.setCd4CountDate(convertDate(obsType.getObsDatetime()));
                        });
                        if (encounterType.getFormId() == 69)
                            viralLoadLineList.setEacDate(convertDate(encounterType.getEncounterDatetime()));
                        if (sex.equalsIgnoreCase("F") || sex.equalsIgnoreCase("Female")) {
                            Optional<ObsType> pregnancyObs = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                            obsType.getConceptId() == 165050 &&
                                            convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                            obsType.getVoided() == 0)
                                    .findAny();
                            pregnancyObs.ifPresent(obsType -> {
                                viralLoadLineList.setPregnancyStatus(obsType.getVariableValue());
                                viralLoadLineList.setPregnancyStatusDate(convertDate(obsType.getObsDatetime()));
                            });
                        }
                        Optional<ObsType> viralLoadObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == LABORATORY_ORDER_AND_RESULT_FORM &&
                                        obsType.getConceptId() == 856 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getValueNumeric() != null)
                                .findAny();
                        viralLoadObs.ifPresent(obsType -> {
                            viralLoadLineList.setCurrentViralLoad(obsType.getValueNumeric().doubleValue());
                            viralLoadLineList.setViralLoadEncounterDate(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                viralLoadLineList.setViralLoadSampleCollectionDate(obsType1.getValueDatetime() != null ?
                                        convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                viralLoadLineList.setViralLoadSampleCollectionDate(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> report = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 165414, container, cutOff);
                            report.ifPresent(obsType1 -> viralLoadLineList.setViralLoadReportedDate(obsType1.getValueDatetime() != null ?
                                    convertDate(obsType1.getValueDatetime()) : null));
                            Optional<ObsType> viralLoadIndication = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 164980, container, cutOff);
                            viralLoadIndication.ifPresent(obsType1 -> viralLoadLineList.setViralLoadIndication(obsType1.getVariableValue()));
                        });
                        Optional<ObsType> patientOutComeObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == CLIENT_TRACKING_AND_TERMINATION_FORM &&
                                        obsType.getConceptId() == 165470 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0)
                                .findAny();
                        patientOutComeObs.ifPresent(obsType -> {
                            viralLoadLineList.setPatientOutcome(obsType.getVariableValue());
                            viralLoadLineList.setPatientOutcomeDate(convertDate(obsType.getObsDatetime()));
                        });

                        AtomicLong daysOfArv = new AtomicLong();
                        if (viralLoadLineList.getDaysOfArvRefill() != null)
                            daysOfArv.set(viralLoadLineList.getDaysOfArvRefill());
                        else {
                            lastPickUpBeforeVisitObs.ifPresent(obsType -> {
                                Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                                daysOfArvObs.ifPresent(obsType1 -> daysOfArv.set(obsType1.getValueNumeric().intValue()));
                            });
                        }
                        String status = helperFunctions.getCurrentArtStatus(pickUpDuringVisitDate != null ? pickUpDuringVisitDate : lastPickUpBeforeVisit,
                                daysOfArv.get(), encounterType.getEncounterDatetime(), viralLoadLineList.getPatientOutcome());
                        viralLoadLineList.setCurrentArtStatus(status);
                        viralLoadLineList.setDateReturnedToCare(container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getConceptId() == 165775 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getValueDatetime() != null &&
                                        obsType.getVoided() == 0)
                                .map(obsType -> convertDate(obsType.getValueDatetime()))
                                .findAny()
                                .orElse(null));
                        viralLoadLineList.setDateOfTermination(container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getConceptId() == 165469 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getValueDatetime() != null &&
                                        obsType.getVoided() == 0)
                                .map(obsType -> convertDate(obsType.getValueDatetime()))
                                .findAny()
                                .orElse(null));
                        viralLoadLineList.setPharmacyNextAppointment(container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getConceptId() == 5096 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getValueDatetime() != null &&
                                        obsType.getVoided() == 0)
                                .map(obsType -> convertDate(obsType.getValueDatetime()))
                                .findAny()
                                .orElse(null));
                        Optional<ObsType> weightObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                        obsType.getConceptId() == 5089 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getValueNumeric() != null)
                                .findAny();
                        weightObs.ifPresent(obsType -> {
                            viralLoadLineList.setCurrentWeight(obsType.getValueNumeric().doubleValue());
                            viralLoadLineList.setCurrentWeightDate(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> heightObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                        obsType.getConceptId() == 5090 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getValueNumeric() != null)
                                .findAny();
                        heightObs.ifPresent(obsType -> {
                            viralLoadLineList.setHeight(obsType.getValueNumeric().doubleValue());
                            viralLoadLineList.setHeightDate(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> tbStatusObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                        obsType.getConceptId() == 1659 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getVariableValue() != null)
                                .findAny();
                        tbStatusObs.ifPresent(obsType -> {
                            viralLoadLineList.setTbStatus(obsType.getVariableValue());
                            viralLoadLineList.setTbStatusDate(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> prevNextAppointmentObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == CARE_CARD &&
                                        obsType.getConceptId() == 5096 &&
                                        convertDate(obsType.getObsDatetime()).isBefore(visitDate) &&
                                        obsType.getVoided() == 0 &&
                                        obsType.getValueDatetime() != null)
                                .max(Comparator.comparing(ObsType::getObsDatetime));
                        if (prevNextAppointmentObs.isPresent()) {
                            ObsType obsType = prevNextAppointmentObs.get();
                            LocalDate prevNextAppointmentDate = convertDate(obsType.getValueDatetime());
//                    log.info("Next appointment date {}", prevNextAppointmentDate);
//                    log.info("Visit date {}", visitDate);
                            long days = ChronoUnit.DAYS.between(prevNextAppointmentDate, visitDate);
//                    log.info("Days between visit date and next appointment date {}", days);
                            viralLoadLineList.setMissedClinicAppointmentDuration((int) days);
//                        if (days < -14 || days > 14) {
//                            viralLoadLineList.setMissedClinicAppointment("Yes");
//                        } else {
//                            viralLoadLineList.setMissedClinicAppointment("No");
//                        }
                        } else {
                            Optional<ObsType> pickUpDuringPrevVisitDateObs = container.getMessageData().getObs()
                                    .stream()
                                    .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                            obsType.getConceptId() == 162240 &&
                                            obsType.getVoided() == 0 &&
                                            convertDate(obsType.getObsDatetime()).isBefore(visitDate))
                                    .findAny();
                            pickUpDuringPrevVisitDateObs.ifPresent(obsType -> {
                                Optional<ObsType> daysOfPrevArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                                daysOfPrevArvObs.ifPresent(obsType1 -> {
                                    int days = obsType1.getValueNumeric().intValue();
                                    LocalDate visitDate1 = convertDate(obsType.getObsDatetime());
                                    LocalDate nextAppointmentDate = visitDate1.plusDays(days);
                                    long daysBetween = ChronoUnit.DAYS.between(nextAppointmentDate, visitDate);
                                    viralLoadLineList.setMissedClinicAppointmentDuration((int) daysBetween);
                                });
                            });
                        }
                        List<ObsType> oiDrugListObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                        obsType.getConceptId() == 165727 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .collect(Collectors.toList());
                        if (!oiDrugListObs.isEmpty()) {
                            StringBuilder oiDrugList = new StringBuilder();
                            oiDrugListObs.forEach(obsType -> oiDrugList.append(obsType.getVariableValue()).append(" | "));
                            oiDrugList.deleteCharAt(oiDrugList.lastIndexOf("|"));
                            viralLoadLineList.setOiDrugList(oiDrugList.toString());
                        }
                        Optional<ObsType> pickupReasonObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == PHARMACY_FORM &&
                                        obsType.getConceptId() == 165774 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .findFirst();
                        pickupReasonObs.ifPresent(obsType -> viralLoadLineList.setPickupReason(obsType.getVariableValue()));
                        List<ObsType> otherOiDrugListObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == 14 &&
                                        obsType.getConceptId() == 160170 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .collect(Collectors.toList());
                        if (!otherOiDrugListObs.isEmpty()) {
                            StringBuilder otherOiDrugList = new StringBuilder();
                            otherOiDrugListObs.forEach(obsType -> otherOiDrugList.append(obsType.getVariableValue()).append(" | "));
                            otherOiDrugList.deleteCharAt(otherOiDrugList.lastIndexOf("|"));
                            viralLoadLineList.setOtherOis(otherOiDrugList.toString());
                        }
                        Optional<ObsType> hbsAgObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == 21 &&
                                        obsType.getConceptId() == 159430 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .findFirst();
                        hbsAgObs.ifPresent(obsType -> viralLoadLineList.setHbsAg(obsType.getVariableValue()));
                        Optional<ObsType> hcvObs = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getFormId() == 21 &&
                                        obsType.getConceptId() == 1325 &&
                                        obsType.getVoided() == 0 &&
                                        convertDate(obsType.getObsDatetime()).equals(visitDate))
                                .findFirst();
                        hcvObs.ifPresent(obsType -> viralLoadLineList.setHcv(obsType.getVariableValue()));
                        try {
                            viralLoadLineListRepository.save(viralLoadLineList);
                        } catch (Exception e) {
                            System.out.println(viralLoadLineList);
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        log.info("Finished processing viral load line list {}", containers.size());
    }
}
