package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.linelists.SampleData;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.EncounterType;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.SampleDataRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.SampleDataService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSED;
import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.utils.ConstantsUtils.*;
import static com.etlservice.schedular.utils.HelperFunctions.convertDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleDataServiceImpl implements SampleDataService {
    private final HelperFunctions helperFunctions;
    private final SampleDataRepository sampleDataRepository;
    private final ContainerRepository containerRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public void fetchSampleData() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "Sample");
        lineListTracker.setPageSize(1000);
        int currentPage = lineListTracker.getCurrentPage();
        int pageSize = lineListTracker.getPageSize();
        List<String> datimCodes = new ArrayList<>(Arrays.asList("IEE3UZcwPu3", "FjX5IC6wJ1m", "Y3VlUBi4kD9"));
        while (true) {
            log.info("Fetching page: " + currentPage);
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<IdWrapper> idWrappersPage = containerRepository
                    .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                            datimCodes, 4, pageable
                    );
            if (!idWrappersPage.hasContent()) {
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTracker.setStatus(PROCESSED.name());
                helperFunctions.saveLineListTracker(lineListTracker);
                break;
            } else {
                log.info("Total elements: " + idWrappersPage.getTotalElements());
                log.info("Total pages: " + idWrappersPage.getTotalPages());
                List<IdWrapper> idWrappers = idWrappersPage.getContent();
                buildSampleData(idWrappers);
                helperFunctions.updateLineListTracker(lineListTracker, ++currentPage, idWrappersPage, idWrappers);
            }
        }
    }

    @Override
    public void buildSampleData(List<IdWrapper> idWrappers) {
        idWrappers.forEach(idWrapper -> {
            Optional<Container> optionalContainer = containerRepository.findById(idWrapper.getId());
            optionalContainer.ifPresent(container -> {
                String datimCode = container.getMessageHeader().getFacilityDatimCode();
                Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
                Date cutOff1 = new Date();
                String state = facility.getState().getStateName();
                String lga = facility.getLga().getLga();
                String patientUuid = container.getId();
                LocalDate dob = container.getMessageData().getDemographics().getBirthdate() != null ?
                        convertDate(container.getMessageData().getDemographics().getBirthdate()) : null;
                Date artStartDate1 = helperFunctions.getArtStartDate(container, new Date());
                LocalDate artStartDate = artStartDate1 != null ? convertDate(artStartDate1) : null;
                String patientUniqueId = helperFunctions.returnIdentifiers(4, container).orElse(null);
                String sex = container.getMessageData().getDemographics().getGender();
                String hospNo = helperFunctions.returnIdentifiers(5, container).orElse(null);
                Date dateOfHivDiagnosis = helperFunctions.getMaxObsByConceptID(160554, container, cutOff1).map(ObsType::getValueDatetime).orElse(null);
                LocalDate dateOfHivDiagnosis1 = dateOfHivDiagnosis != null ? convertDate(dateOfHivDiagnosis) : null;
                Date biometricCapturedDate = helperFunctions.getBiometricCaptureDate(container);
                LocalDate biometricCapturedDate1 = biometricCapturedDate != null ? convertDate(biometricCapturedDate) : null;
                Set<EncounterType> encounterTypeSet = new HashSet<>(container.getMessageData().getEncounters());
                List<EncounterType> encounterTypeList = new ArrayList<>(encounterTypeSet);
                encounterTypeList.sort(Comparator.comparing(encounterType -> convertDate(encounterType.getEncounterDatetime())));
                int lastEncounter = encounterTypeList.size() - 1;
                int start = Math.max(0, lastEncounter - 4);
                List<EncounterType> lastFiveEncounters = encounterTypeList.subList(start, lastEncounter + 1);
                SampleData sampleData = sampleDataRepository.findByPatientUuidAndDatimCode(patientUuid, datimCode)
                        .orElse(new SampleData());
                sampleData.setPatientUuid(patientUuid);
                sampleData.setState(state);
                sampleData.setLga(lga);
                sampleData.setDateOfBirth(dob);
                sampleData.setFacilityName(facility.getFacilityName());
                sampleData.setDatimCode(datimCode);
                sampleData.setArtStartDate(artStartDate);
                sampleData.setDateOfHivDiagnosis(dateOfHivDiagnosis1);
                sampleData.setPatientUniqueId(patientUniqueId);
                sampleData.setPatientHospitalNo(hospNo);
                sampleData.setSex(sex);
                sampleData.setBiometricCapturedDate(biometricCapturedDate1);
                for (int i = 0; i < lastFiveEncounters.size(); i++) {
                    if (i == 0) {
                        EncounterType encounterType = lastFiveEncounters.get(i);
                        Date cutOff = encounterType.getEncounterDatetime();
                        Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                        lastPickupDate.ifPresent(obsType -> {
                            sampleData.setLastPickupDate1(convertDate(obsType.getObsDatetime()));

                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill1(obsType1.getValueNumeric().intValue()));
                        });
                        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
                        currentViralLoad.ifPresent(obsType -> {
                            sampleData.setViralLoad1(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                            sampleData.setViralLoadEncounterDate1(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                sampleData.setViralLoadSampleCollectionDate1(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                sampleData.setViralLoadSampleCollectionDate1(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
                            resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate1(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
                        });
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
                        pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate1(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        sampleData.setVisitDate1(convertDate(encounterType.getEncounterDatetime()));
                        if (artStartDate != null)
                            sampleData.setMonthsOnArt1((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
                        Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
                        currentWeight.ifPresent(obsType -> {
                            sampleData.setWeight1((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setWeightDate1(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
                        currentHeight.ifPresent(obsType -> {
                            sampleData.setHeight1((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setHeightDate1(convertDate(obsType.getObsDatetime()));
                        });
                    } else if (i == 1) {
                        EncounterType encounterType = lastFiveEncounters.get(i);
                        Date cutOff = encounterType.getEncounterDatetime();
                        Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                        lastPickupDate.ifPresent(obsType -> {

                            sampleData.setLastPickupDate2(convertDate(obsType.getObsDatetime()));

                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill2(obsType1.getValueNumeric().intValue()));
                        });
                        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
                        currentViralLoad.ifPresent(obsType -> {
                            sampleData.setViralLoad2(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                            sampleData.setViralLoadEncounterDate2(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                sampleData.setViralLoadSampleCollectionDate2(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                sampleData.setViralLoadSampleCollectionDate2(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
                            resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate2(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
                        });
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
                        pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate2(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        sampleData.setVisitDate2(convertDate(encounterType.getEncounterDatetime()));
                        if (artStartDate != null)
                            sampleData.setMonthsOnArt2((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
                        Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
                        currentWeight.ifPresent(obsType -> {
                            sampleData.setWeight2((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setWeightDate2(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
                        currentHeight.ifPresent(obsType -> {
                            sampleData.setHeight2((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setHeightDate2(convertDate(obsType.getObsDatetime()));
                        });
                    } else if (i == 2) {
                        EncounterType encounterType = lastFiveEncounters.get(i);
                        Date cutOff = encounterType.getEncounterDatetime();
                        Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                        lastPickupDate.ifPresent(obsType -> {

                            sampleData.setLastPickupDate3(convertDate(obsType.getObsDatetime()));

                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill3(obsType1.getValueNumeric().intValue()));
                        });
                        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
                        currentViralLoad.ifPresent(obsType -> {
                            sampleData.setViralLoad3(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                            sampleData.setViralLoadEncounterDate3(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                sampleData.setViralLoadSampleCollectionDate3(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                sampleData.setViralLoadSampleCollectionDate3(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
                            resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate3(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
                        });
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
                        pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate3(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        sampleData.setVisitDate3(convertDate(encounterType.getEncounterDatetime()));
                        if (artStartDate != null)
                            sampleData.setMonthsOnArt3((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
                        Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
                        currentWeight.ifPresent(obsType -> {
                            sampleData.setWeight3((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setWeightDate3(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
                        currentHeight.ifPresent(obsType -> {
                            sampleData.setHeight3((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setHeightDate3(convertDate(obsType.getObsDatetime()));
                        });
                    } else if (i == 3) {
                        EncounterType encounterType = lastFiveEncounters.get(i);
                        Date cutOff = encounterType.getEncounterDatetime();
                        Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                        lastPickupDate.ifPresent(obsType -> {

                            sampleData.setLastPickupDate4(convertDate(obsType.getObsDatetime()));

                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill4(obsType1.getValueNumeric().intValue()));
                        });
                        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
                        currentViralLoad.ifPresent(obsType -> {
                            sampleData.setViralLoad4(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                            sampleData.setViralLoadEncounterDate4(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                sampleData.setViralLoadSampleCollectionDate4(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                sampleData.setViralLoadSampleCollectionDate4(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
                            resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate4(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
                        });
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
                        pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate4(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        sampleData.setVisitDate4(convertDate(encounterType.getEncounterDatetime()));
                        if (artStartDate != null)
                            sampleData.setMonthsOnArt4((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
                        Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
                        currentWeight.ifPresent(obsType -> {
                            sampleData.setWeight4((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setWeightDate4(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
                        currentHeight.ifPresent(obsType -> {
                            sampleData.setHeight4((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setHeightDate4(convertDate(obsType.getObsDatetime()));
                        });
                    } else {
                        EncounterType encounterType = lastFiveEncounters.get(i);
                        Date cutOff = encounterType.getEncounterDatetime();
                        Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
                        lastPickupDate.ifPresent(obsType -> {

                            sampleData.setLastPickupDate5(convertDate(obsType.getObsDatetime()));

                            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                            daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill5(obsType1.getValueNumeric().intValue()));
                        });
                        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
                        currentViralLoad.ifPresent(obsType -> {
                            sampleData.setViralLoad5(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

                            sampleData.setViralLoadEncounterDate5(convertDate(obsType.getObsDatetime()));
                            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
                            if (viralLoadSampleCollectionDate.isPresent()) {
                                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                                sampleData.setViralLoadSampleCollectionDate5(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
                            } else {
                                sampleData.setViralLoadSampleCollectionDate5(convertDate(obsType.getObsDatetime()));
                            }
                            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
                            resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate5(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
                        });
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
                        pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate5(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        sampleData.setVisitDate5(convertDate(encounterType.getEncounterDatetime()));
                        if (artStartDate != null)
                            sampleData.setMonthsOnArt5((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
                        Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
                        currentWeight.ifPresent(obsType -> {
                            sampleData.setWeight5((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setWeightDate5(convertDate(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
                        currentHeight.ifPresent(obsType -> {
                            sampleData.setHeight5((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                                    Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                                    obsType.getValueNumeric().doubleValue() : 0.0);
                            sampleData.setHeightDate5(convertDate(obsType.getObsDatetime()));
                        });
                    }
                }
//                lastFiveEncounters.forEach(encounterType -> {
//                    Date cutOff = encounterType.getEncounterDatetime();
//                    Optional<ObsType> lastPickupDate = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
//                    lastPickupDate.ifPresent(obsType -> {
//
//                        sampleData.setLastPickupDate(convertDate(obsType.getObsDatetime()));
//
//                        Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
//                        daysOfArvObs.ifPresent(obsType1 -> sampleData.setDaysOfArvRefill(obsType1.getValueNumeric().intValue()));
//                    });
//                    Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
//                    currentViralLoad.ifPresent(obsType -> {
//                        sampleData.setViralLoad(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);
//
//                        sampleData.setViralLoadEncounterDate(convertDate(obsType.getObsDatetime()));
//                        Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
//                        if (viralLoadSampleCollectionDate.isPresent()) {
//                            ObsType obsType1 = viralLoadSampleCollectionDate.get();
//                            sampleData.setViralLoadSampleCollectionDate(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null);
//                        } else {
//                            sampleData.setViralLoadSampleCollectionDate(convertDate(obsType.getObsDatetime()));
//                        }
//                        Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
//                        resultDate.ifPresent(obsType1 -> sampleData.setViralLoadResultDate(obsType1.getValueDatetime() != null ? convertDate(obsType1.getValueDatetime()) : null));
//                    });
//                    Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
//                    pharmacyNextAppointment.ifPresent(obsType -> sampleData.setPharmacyNextAppointmentDate(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
//                    sampleData.setVisitDate(convertDate(encounterType.getEncounterDatetime()));
//                    if (artStartDate != null)
//                        sampleData.setMonthsOnArt((int) ChronoUnit.MONTHS.between(artStartDate, convertDate(encounterType.getEncounterDatetime())));
//                    Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
//                    currentWeight.ifPresent(obsType -> {
//                        sampleData.setWeight((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
//                                Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
//                                obsType.getValueNumeric().doubleValue() : 0.0);
//                        sampleData.setWeightDate(convertDate(obsType.getObsDatetime()));
//                    });
//                    Optional<ObsType> currentHeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5090, container, cutOff);
//                    currentHeight.ifPresent(obsType -> {
//                        sampleData.setHeight((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
//                                Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
//                                obsType.getValueNumeric().doubleValue() : 0.0);
//                        sampleData.setHeightDate(convertDate(obsType.getObsDatetime()));
//                    });
//                });
                sampleDataRepository.save(sampleData);
            });
        });
    }
}
