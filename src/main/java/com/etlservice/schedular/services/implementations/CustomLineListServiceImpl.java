package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.entities.linelists.CustomLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.FctDataSet;
import com.etlservice.schedular.entities.linelists.MortalityLineList;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.EncounterType;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.CustomLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.FctDataSetRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.LineListTrackerRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.MortalityLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.CustomLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.etlservice.schedular.utils.ConstantsUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomLineListServiceImpl implements CustomLineListService {
    private final ContainerRepository containerRepository;
    private final CustomLineListRepository customLineListRepository;
    private final HelperFunctions helperFunctions;
    private final LineListTrackerRepository lineListTrackerRepository;
    private final FacilityRepository facilityRepository;
    private final MortalityLineListRepository mortalityLineListRepository;
    private final FctDataSetRepository fctDataSetRepository;
    private final MongoTemplate mongoTemplate;
//    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
//    private Lock readLock = readWriteLock.readLock();
//    private Lock writeLock = readWriteLock.writeLock();

    @Override
    public void processLineList() {
        LocalDate currentDate = LocalDate.now();
        Date cutOff = Date.from(currentDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());
        LocalDate startDate = LocalDate.of(2017, 1, 1);
        Date startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Pageable pageable = PageRequest.of(0, 1000);
        int page = 0;
        int limit = 1000;
        Date start = new Date();
//        Query query = new Query();
//        query.addCriteria(Criteria.where("messageData.encounters.encounterDatetime").gte(startDate1).lte(cutOff));
//        query.addCriteria(Criteria.where("messageData.obs.conceptId").is(165724));
//        query.limit(limit);
//        query.skip(page * limit);
//        List<Container> containerList2 = mongoTemplate.find(query, Container.class);
//        Page<Container> containerList1 = containerRepository
//                .findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(startDate1, cutOff, 165724, pageable);
        Page<Container> containerList1 = containerRepository
                .findContainersByMessageDataEncountersEncounterDatetimeGreaterThanAndMessageDataObsConceptId(startDate1, 165724, pageable);
//        log.info("Total Patients with encounters :: " + containerList1.getTotalElements());
        log.info("Total Patients with encounters :: " + containerList1.getContent().size());
        Date end = new Date();
        log.info("Time taken to fetch data from mongo :: " + (end.getTime() - start.getTime()) / 1000 % 60+ " seconds");

//        Page<Container> containerList1 = containerRepository
//                .findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(startDate1, cutOff, 165724, pageable);
//        log.info("Total Patients with encounters :: " + containerList1.getTotalElements());
//
//        List<Container> fctPatients = containerList1.stream().filter(container -> {
//            Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
//            return facility != null && facility.getState().getStateName().equals("FCT");
//        }).collect(Collectors.toList());
//        if (!fctPatients.isEmpty()) {
//            log.info("First batch of FCT Patients with encounters :: " + fctPatients.size());
//            Queue<Container> fctPatientsMoreThan15 = fctPatients.stream().filter(container -> {
//                int age = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
//                return age >= 15;
//            }).collect(Collectors.toCollection(LinkedList::new));
//            if (!fctPatientsMoreThan15.isEmpty()) {
//                log.info("First batch of FCT Patients with encounters more than 15 :: " + fctPatientsMoreThan15.size());
//                createCustomLineList(cutOff, startDate1, fctPatientsMoreThan15);
//            }
////            createCustomLineList(cutOff, startDate1, fctPatients);
//            int totalPages = containerList1.getTotalPages();
//            int currentPage = 1;
//            int totalPatients = containerList1.getContent().size();
//            while (currentPage < totalPages) {
//                Pageable pageable1 = PageRequest.of(currentPage, 1000);
//                Page<Container> containerList = containerRepository
//                        .findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(startDate1, cutOff, 165724, pageable1);
//
//                List<Container> fctPatients1 = containerList.stream().filter(container -> {
//                    Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
//                    return facility != null && facility.getState().getStateName().equals("FCT");
//                }).collect(Collectors.toList());
//
//                if (!fctPatients1.isEmpty()) {
//                    log.info(currentPage + " => FCT Patients with encounters :: " + fctPatients1.size());
//                    Queue<Container> fctPatientsMoreThan15B = fctPatients1.stream().filter(container -> {
//                        int age = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
//                        return age >= 15;
//                    }).collect(Collectors.toCollection(LinkedList::new));
//                    if (!fctPatientsMoreThan15B.isEmpty()) {
//                        log.info("FCT Patients with encounters more than 15 :: " + fctPatientsMoreThan15B.size());
//                        createCustomLineList(cutOff, startDate1, fctPatientsMoreThan15B);
//                    }
////                    createCustomLineList(cutOff, startDate1, fctPatients1);
//                }
//                currentPage++;
//                totalPatients += containerList.getContent().size();
//                log.info("Processed Patients with encounters :: " + totalPatients);
//            }
//            log.info("Custom Line List data saved successfully");
//        }


//        List<Container> containerList = containerRepository
//                .findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(startDate1, cutOff, 165724);
//        log.info("Patients with encounters :: " + containerList.size());
//        List<Container> fctPatients = containerList.stream().filter(container -> {
//            Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
//            return facility != null && facility.getState().getStateName().equals("FCT");
//        }).collect(Collectors.toList());
//        if (!fctPatients.isEmpty()) {
//            log.info("FCT Patients with encounters :: " + fctPatients.size());
////            createCustomLineList(cutOff, startDate1, fctPatients);
//            Queue<Container> fctPatientsMoreThan15 = fctPatients.stream().filter(container -> {
//                int age = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
//                return age >= 15;
//            }).collect(Collectors.toCollection(LinkedList::new));
//            if (!fctPatientsMoreThan15.isEmpty()) {
//                log.info("FCT Patients with encounters more than 15 :: " + fctPatientsMoreThan15.size());
//                createCustomLineList(cutOff, startDate1, fctPatientsMoreThan15);
//            }
//        }

    }

    @Override
    public void processMortalityLineList(List<Container> containerList) {
        containerList.forEach(container -> {
            Date cutOff = new Date();
            Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
            MortalityLineList mortalityLineList = new MortalityLineList();
            mortalityLineList.setFacilityState(facility.getState().getStateName());
            mortalityLineList.setFacilityName(facility.getFacilityName());
            mortalityLineList.setDatimCode(facility.getDatimCode());
//            mortalityLineList.setPatientUuid(container.getMessageData().getDemographics().getPatientUuid());
            mortalityLineList.setHospitalNumber(helperFunctions.returnIdentifiers(5, container).orElse(""));
            mortalityLineList.setPatientId(helperFunctions.returnIdentifiers(4, container).orElse(""));
            mortalityLineList.setStateOfResidence(container.getMessageData().getDemographics().getStateProvince());
            String cityVillage = container.getMessageData().getDemographics().getCityVillage();
            if (cityVillage != null)
                cityVillage = cityVillage.replace("NULL", "").trim();
            mortalityLineList.setLgaOfResidence(cityVillage);
            mortalityLineList.setMaritalStatus(
                    helperFunctions.getMaxConceptObsIdWithFormId(23, 1054, container, cutOff)
                            .map(ObsType::getVariableValue).orElse(""));
            String sex = container.getMessageData().getDemographics().getGender();
            if (sex != null) {
                if (sex.equalsIgnoreCase("Female"))
                    sex = "F";
                if (sex.equalsIgnoreCase("Male"))
                    sex = "M";
            }
            mortalityLineList.setSex(sex);
            mortalityLineList.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            mortalityLineList.setMarkAsDeceased(container.getMessageData().getDemographics().getDead());
            mortalityLineList.setPatientDeceasedDate(container.getMessageData().getDemographics().getDeathDate() != null ?
                    container.getMessageData().getDemographics().getDeathDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate() : null);
//            mortalityLineList.setCauseOfDeath(container.getMessageData().getDemographics().getCauseOfDeath());
            mortalityLineList.setPatientDeceasedDate(helperFunctions.getMaxConceptObsIdWithFormId(13, 165469, container, cutOff)
                    .filter(obsType -> obsType.getValueDatetime() != null)
                    .map(obsType -> obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .orElse(null));
            mortalityLineList.setDateOfDiagnosis(
                    helperFunctions.getMaxConceptObsIdWithFormId(23, 160554, container, cutOff)
                            .filter(obsType -> obsType.getValueDatetime() != null)
                            .map(obsType -> obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setWhoClinicalStageAtDiagnosis(
                    helperFunctions.getMaxConceptObsIdWithFormId(56, 5356, container, cutOff)
                            .map(ObsType::getVariableValue).orElse("")
            );
            mortalityLineList.setLastRecordedWhoClinicalStage(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5356, container, cutOff)
                            .map(ObsType::getVariableValue).orElse("")
            );
            mortalityLineList.setDateOfLastRecordedWhoClinicalStage(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5356, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setCd4CountAtDiagnosis(
                    helperFunctions.getMaxConceptObsIdWithFormId(56, 164429, container, cutOff)
                            .map(obsType -> obsType.getValueNumeric().doubleValue())
                            .orElse(null)
            );
            mortalityLineList.setDateOfBaselineCd4Count(
                    helperFunctions.getMaxConceptObsIdWithFormId(56, 164429, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setLastRecordedCd4Count(
                    helperFunctions.getMaxConceptObsIdWithFormId(21, 5497, container, cutOff)
                            .map(obsType -> obsType.getValueNumeric().doubleValue()).orElse(null)
            );
            mortalityLineList.setDateOfLastRecordedCd4Count(
                    helperFunctions.getMaxConceptObsIdWithFormId(21, 5497, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setDateOfArtInitiation(
                    helperFunctions.getMaxConceptObsIdWithFormId(56, 159599, container, cutOff)
                            .filter(obsType -> obsType.getValueDatetime() != null)
                            .map(obsType -> obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            Optional<ObsType> regimenObs = helperFunctions.getMaxConceptObsIdWithFormId(56, 165708, container, cutOff);
            regimenObs.ifPresent(obsType -> {
                mortalityLineList.setArtRegimenLineAtInitiation(obsType.getVariableValue());
                mortalityLineList.setArtRegimenAtInitiation(
                        helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff)
                        .map(ObsType::getVariableValue).orElse(""));
            });
            Optional<ObsType> lastRegimenObs = helperFunctions.getMaxConceptObsIdWithFormId(27, 165708, container, cutOff);
            lastRegimenObs.ifPresent(obsType -> {
                mortalityLineList.setLastArtRegimenLine(obsType.getVariableValue());
                mortalityLineList.setLastArtRegimen(
                        helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff)
                        .map(ObsType::getVariableValue).orElse(""));
            });
            Optional<ObsType> lastPickupObs = helperFunctions.getMaxConceptObsIdWithFormId(27, 162240, container, cutOff);
            lastPickupObs.ifPresent(obsType -> {
                mortalityLineList.setDateOfLastDrugPickup(obsType.getObsDatetime() != null ?
                        obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
                Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
                daysOfArvObs.ifPresent(obsType1 -> mortalityLineList.setDaysOfArvRefillAtLastDrugPickup(obsType1.getValueNumeric().intValue()));
            });
            mortalityLineList.setLastRecordedWeight(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5089, container, cutOff)
                            .map(obsType -> obsType.getValueNumeric().doubleValue()).orElse(null)
            );
            mortalityLineList.setDateOfLastRecordedWeight(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5089, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setLastRecordedSystolicBloodPressure(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5085, container, cutOff)
                            .map(obsType -> obsType.getValueNumeric().doubleValue()).orElse(null)
            );
            mortalityLineList.setDateOfLastRecordedSystolicBloodPressure(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5085, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setLastRecordedDiastolicBloodPressure(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5086, container, cutOff)
                            .map(obsType -> obsType.getValueNumeric().doubleValue())
                            .orElse(null)
            );
            mortalityLineList.setDateOfLastRecordedDiastolicBloodPressure(
                    helperFunctions.getMaxConceptObsIdWithFormId(14, 5086, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            Optional<ObsType> lastViralLoadObs = helperFunctions.getMaxConceptObsIdWithFormId(21, 856, container, cutOff);
            lastViralLoadObs.ifPresent(obsType -> {
                mortalityLineList.setLastAvailableViralLoadResult(obsType.getValueNumeric().doubleValue());
                Optional<ObsType> viralLoadTypeObs = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 159951, container, cutOff);
                viralLoadTypeObs.ifPresent(obsType1 ->
                        mortalityLineList.setDateSampleCollectedForLastAvailableViralLoadResult(obsType1.getObsDatetime() != null ?
                        obsType1.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
            });
            mortalityLineList.setLastViralLoadSampleCollectionDate(
                    helperFunctions.getMaxConceptObsIdWithFormId(21, 159951, container, cutOff)
                            .filter(obsType -> obsType.getObsDatetime() != null)
                            .map(obsType -> obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setNonVaCauseOfDeath(
                    helperFunctions.getMaxConceptObsIdWithFormId(13, 165889, container, cutOff)
                            .map(ObsType::getVariableValue).orElse("")
            );
            Optional<ObsType> smartVACODObs = helperFunctions.getMaxConceptObsIdWithFormId(13, 166349, container, cutOff);
            smartVACODObs.ifPresent(obsType -> {
                Optional<ObsType> smartVACODObs1 = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
                smartVACODObs1.ifPresent(obsType1 -> mortalityLineList.setSmartVACOD(obsType1.getVariableValue()));
            });
            Optional<ObsType> tbStatusObs = helperFunctions.getMaxConceptObsIdWithFormId(14, 1659, container, cutOff);
            tbStatusObs.ifPresent(obsType -> {
                mortalityLineList.setTbStatus(obsType.getVariableValue());
                mortalityLineList.setTbStatusDate(obsType.getObsDatetime() != null ?
                        obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            });
            Optional<ObsType> prophylaxisUseCPT = container.getMessageData().getObs().stream()
                    .filter(obsType -> obsType.getConceptId() == 165727 &&
                                    obsType.getFormId() == 27 &&
                                    obsType.getValueCoded() == 165257 &&
                                    obsType.getObsDatetime().before(cutOff) &&
                                    obsType.getVoided() == 0)
                    .max(Comparator.comparing(ObsType::getObsDatetime));
            prophylaxisUseCPT.ifPresent(obsType -> {
                mortalityLineList.setProphylaxisUseCPT(obsType.getVariableValue());
                mortalityLineList.setLastCptPickupDate(obsType.getObsDatetime() != null ?
                        obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            });
            Optional<ObsType> prophylaxisUseTPTMaxObs = container.getMessageData().getObs().stream()
                    .filter(obsType -> obsType.getConceptId() == 165727 &&
                                    obsType.getFormId() == 27 &&
                                    obsType.getValueCoded() == 1679 &&
                                    obsType.getObsDatetime().before(cutOff) &&
                                    obsType.getVoided() == 0)
                    .max(Comparator.comparing(ObsType::getObsDatetime));
            prophylaxisUseTPTMaxObs.ifPresent(obsType -> {
                mortalityLineList.setProphylaxisUseTPT(obsType.getVariableValue());
                mortalityLineList.setLastTptPickupDate(obsType.getObsDatetime() != null ?
                        obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            });
            Optional<ObsType> prophylaxisUseTPTMinObs = container.getMessageData().getObs().stream()
                    .filter(obsType -> obsType.getConceptId() == 165727 &&
                            obsType.getFormId() == 27 &&
                            obsType.getValueCoded() == 1679 &&
                            obsType.getObsDatetime().before(cutOff) &&
                            obsType.getVoided() == 0)
                    .min(Comparator.comparing(ObsType::getObsDatetime));
            prophylaxisUseTPTMinObs.ifPresent(obsType ->
                    mortalityLineList.setFirstTptPickupDate(obsType.getObsDatetime().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate()));
            mortalityLineList.setTptStartDate(
                    helperFunctions.getMaxConceptObsIdWithFormId(56, 164852, container, cutOff)
                            .filter(obsType -> obsType.getValueDatetime() != null)
                            .map(obsType -> obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineList.setCurrentInhOutcome(
                    helperFunctions.getMaxConceptObsIdWithFormId(53, 166007, container, cutOff)
                            .map(ObsType::getVariableValue).orElse("")
            );
            mortalityLineList.setCurrentInhOutcomeDate(
                    helperFunctions.getMaxConceptObsIdWithFormId(53, 166008, container, cutOff)
                            .filter(obsType -> obsType.getValueDatetime() != null)
                            .map(obsType -> obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                            .orElse(null)
            );
            mortalityLineListRepository.save(mortalityLineList);
        });
        log.info("Mortality Line List created successfully {}", containerList.size());
    }

    @Override
    public void processFctDataSetLineList(List<Container> containerList) {
        containerList.forEach(container -> {
            String datimCode = container.getMessageHeader().getFacilityDatimCode();
            Facility facility = facilityRepository.findFacilityByDatimCode(datimCode);
            FctDataSet fctDataSet = fctDataSetRepository.findByPatientUuidAndDatimCode(container.getId(), datimCode)
                            .orElse(new FctDataSet());
            fctDataSet.setState(facility.getState().getStateName());
            fctDataSet.setLga(facility.getLga().getLga());
            fctDataSet.setFacilityName(facility.getFacilityName());
            fctDataSet.setDatimCode(datimCode);
            fctDataSet.setPatientUuid(container.getMessageData().getDemographics().getPatientUuid());
            String sex = container.getMessageData().getDemographics().getGender();
            if (sex != null) {
                if (sex.equalsIgnoreCase("Female"))
                    sex = "F";
                if (sex.equalsIgnoreCase("Male"))
                    sex = "M";
            }
            fctDataSet.setSex(sex);
            fctDataSet.setCurrentAge(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, new Date()));
            fctDataSet.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            Long ageAtStartOfARTYears = helperFunctions.getAgeAtStartOfARTYears(container, new Date());
            if (ageAtStartOfARTYears != null)
                if (ageAtStartOfARTYears >= 1)
                    fctDataSet.setAgeAtStartOfArtYears(ageAtStartOfARTYears.intValue());
                else {
                    Long ageAtStartOfARTMonths = helperFunctions.getAgeAtStartOfARTMonths(container, new Date());
                    if (ageAtStartOfARTMonths != null)
                        fctDataSet.setAgeAtStartOfArtMonths(ageAtStartOfARTMonths.intValue());
                }
            fctDataSet.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, new Date())
                    .map(ObsType::getVariableValue).orElse(null));
            fctDataSet.setKpType(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, new Date())
                    .map(ObsType::getVariableValue).orElse(null));
            fctDataSet.setMaritalStatus(
                    helperFunctions.getMaxConceptObsIdWithFormId(23, 1054, container, new Date())
                            .map(ObsType::getVariableValue).orElse(""));
            Optional<ObsType> lastViralLoadObs = helperFunctions.getMaxConceptObsIdWithFormId(21, 856, container, new Date());
            lastViralLoadObs.ifPresent(obsType -> {
                fctDataSet.setLastViralLoad(obsType.getValueNumeric().doubleValue());
                Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, new Date());
                if (viralLoadSampleCollectionDate.isPresent()) {
                    ObsType obsType1 = viralLoadSampleCollectionDate.get();
                    fctDataSet.setLastViralLoadDate(obsType1.getValueDatetime() != null ? obsType1.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : obsType1.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    fctDataSet.setLastViralLoadDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }
            });
            Date artStartDate = helperFunctions.getMaxObsByConceptID(159599, container, new Date())
                    .map(ObsType::getValueDatetime).orElse(null);
            fctDataSet.setArtStartDate(artStartDate != null ?
                    artStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
            Optional<ObsType> currentRegimenLine = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,165708, container, new Date());
            currentRegimenLine.ifPresent(obsType -> {
                fctDataSet.setCurrentRegimenLine(obsType.getVariableValue());
                Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, new Date());
                currentRegimenType.ifPresent(obsType1 -> fctDataSet.setCurrentRegimen(obsType1.getVariableValue()));
            });
            Optional<ObsType> maxObsByConceptID = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, new Date());
            maxObsByConceptID.ifPresent(obsType -> {
                fctDataSet.setLastPickupDate(obsType.getObsDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, new Date());
                daysOfArvObs.ifPresent(obsType1 -> fctDataSet.setDrugDuration(obsType1.getValueNumeric().toBigInteger().intValue()));
            });
            Optional<ObsType> patientOutCome = helperFunctions.getMaxConceptObsIdWithFormId(
                    CLIENT_TRACKING_AND_TERMINATION_FORM,165470, container, new Date());
            patientOutCome.ifPresent(obsType -> fctDataSet.setPatientOutcome(obsType.getVariableValue()));
            Date lastPickupDate = fctDataSet.getLastPickupDate() != null ?
                    Date.from(fctDataSet.getLastPickupDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
            String status = helperFunctions.getCurrentArtStatus(lastPickupDate, fctDataSet.getDrugDuration() != null ? Long.valueOf(fctDataSet.getDrugDuration()) : null,
                    new Date(), fctDataSet.getPatientOutcome());
            fctDataSet.setCurrentStatus(status);
            Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, new Date());
            pharmacyNextAppointment.ifPresent(obsType -> fctDataSet.setNextAppointmentDate(obsType.getValueDatetime() != null ?
                    obsType.getValueDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null));
            fctDataSet.setDateConfirmedPositive(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 160554, container, new Date())
                    .map(ObsType::getValueDatetime)
                    .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .orElse(null));
            fctDataSet.setOccupationalStatus(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 1542, container, new Date())
                    .map(ObsType::getVariableValue).orElse(""));
            fctDataSet.setEducationalLevel(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 1712, container, new Date())
                    .map(ObsType::getVariableValue).orElse(""));
            fctDataSetRepository.save(fctDataSet);
        });
        log.info("FCT Data Set Line List created successfully {}", containerList.size());
    }

    private void createCustomLineList(Date cutOff, Date startDate1, Queue<Container> fctPatients) {
        while (!fctPatients.isEmpty()) {
            Container container = fctPatients.poll();
            buildPatientCustomLineList(cutOff, startDate1, container);
        }
//        fctPatients.forEach(container -> {
//            int age = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
//            if (age >= 15) {
//                Date dob = container.getMessageData().getDemographics().getBirthdate();
//                String identifier = helperFunctions.returnIdentifiers(4, container).orElse("");
//                String facilityName = container.getMessageHeader().getFacilityName();
//                String gender = container.getMessageData().getDemographics().getGender();
//                String kpIdentifier = helperFunctions.getMaxObsByConceptID(166369, container, cutOff).map(ObsType::getVariableValue).orElse("GP");
//                Date dateOfHivDiagnosis = helperFunctions.getMaxObsByConceptID(160554, container, cutOff).map(ObsType::getValueDatetime).orElse(null);
//                Date artStartDate = helperFunctions.getMaxObsByConceptID(159599, container, cutOff).map(ObsType::getValueDatetime).orElse(null);
//                Set<EncounterType> uniqueEncounters = container.getMessageData().getEncounters().stream()
//                        .filter(encounterType -> encounterType.getEncounterDatetime().after(startDate1) &&
//                                encounterType.getEncounterDatetime().before(cutOff))
//                        .collect(Collectors.toSet());
//                uniqueEncounters.forEach(encounterType -> {
//                    CustomLineList customLineList = customLineListRepository
//                            .findByIdentifierAndVisit(identifier, encounterType.getEncounterDatetime())
//                            .orElse(new CustomLineList());
//                    customLineList.setFacilityName(facilityName);
//                    customLineList.setIdentifier(identifier);
//                    customLineList.setGender(gender);
//                    customLineList.setAge(age);
//                    customLineList.setKpIdentifier(kpIdentifier);
//                    customLineList.setDateOfHivDiagnosis(dateOfHivDiagnosis);
//                    customLineList.setArtStartDate(artStartDate);
//                    customLineList.setVisit(encounterType.getEncounterDatetime());
//                    customLineList.setDob(dob);
//                    customLineList.setPregnancyStatus(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 165050 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    if (customLineList.getPregnancyStatus().equals("Pregnant")) {
//                        customLineList.setPregnancyStatusDate(container.getMessageData().getObs().stream()
//                                .filter(obsType -> obsType.getConceptId() == 165050 &&
//                                        obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                        obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
//                                .map(ObsType::getObsDatetime).findFirst().orElse(null));
//                    }
//                    customLineList.setPregnancyDueDate(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 5596 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueDatetime() != null)
//                            .map(ObsType::getValueDatetime).findFirst().orElse(null));
//                    customLineList.setPickUpReason(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 165774 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 &&
//                                    obsType.getValueCoded() != 47 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    List<Integer> regimenConcepts = new ArrayList<>(Arrays.asList(164507,164514,165703,164506,164513,165702));
//                    customLineList.setRegimen(container.getMessageData().getObs().stream()
//                            .filter(obsType -> regimenConcepts.contains(obsType.getConceptId()) &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setRegimenDate(container.getMessageData().getObs().stream()
//                            .filter(obsType -> regimenConcepts.contains(obsType.getConceptId()) &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
//                            .map(ObsType::getObsDatetime).findFirst().orElse(null));
//                    customLineList.setDispensedQuantity(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 1443 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
//                            .map(obsType -> obsType.getValueNumeric().intValue()).findFirst().orElse(0));
//                    customLineList.setDispensedDate(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 1443 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
//                            .map(ObsType::getObsDatetime).findFirst().orElse(null));
//                    customLineList.setDaysOfArv(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 159368 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
//                            .map(obsType -> obsType.getValueNumeric().intValue()).findFirst().orElse(0));
//                    customLineList.setDispenseModality(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 166148 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setDddDispensing(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 166363 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setViralLoad(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 856 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
//                            .map(obsType -> obsType.getValueNumeric().doubleValue()).findFirst().orElse(null));
//                    customLineList.setViralLoadSampleCollectionDate(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 159951 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueDatetime() != null)
//                            .map(ObsType::getValueDatetime).findFirst().orElse(null));
//                    customLineList.setTransferInStatus(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 165242 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setPatientOutcome(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 165470 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setEacDate(encounterType.getFormId() == 69 ? encounterType.getEncounterDatetime() : null);
//                    customLineList.setEacSessionType(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 166097 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineList.setEacBarrier(container.getMessageData().getObs().stream()
//                            .filter(obsType -> obsType.getConceptId() == 165457 &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    List<Integer> eacConcepts = new ArrayList<>(Arrays.asList(165501,165021));
//                    customLineList.setEacIntervention(container.getMessageData().getObs().stream()
//                            .filter(obsType -> eacConcepts.contains(obsType.getConceptId()) &&
//                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
//                                    obsType.getVoided() == 0 && obsType.getValueCoded() != 166160 &&
//                                    obsType.getVariableValue() != null)
//                            .map(ObsType::getVariableValue).findFirst().orElse(""));
//                    customLineListRepository.save(customLineList);
//                });
//            }
//        });
    }

    public void buildPatientCustomLineList(Date cutOff, Date startDate1, Container container) {
        int age = helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff);
        if (age >= 15) {
            Date dob = container.getMessageData().getDemographics().getBirthdate();
            String identifier = helperFunctions.returnIdentifiers(4, container).orElse("");
            String facilityName = container.getMessageHeader().getFacilityName();
            String gender = container.getMessageData().getDemographics().getGender();
            String kpIdentifier = helperFunctions.getMaxObsByConceptID(166369, container, cutOff).map(ObsType::getVariableValue).orElse("GP");
            Date dateOfHivDiagnosis = helperFunctions.getMaxObsByConceptID(160554, container, cutOff).map(ObsType::getValueDatetime).orElse(null);
            Date artStartDate = helperFunctions.getMaxObsByConceptID(159599, container, cutOff).map(ObsType::getValueDatetime).orElse(null);
            Set<EncounterType> uniqueEncounters = container.getMessageData().getEncounters()
                    .stream()
                    .filter(encounterType -> encounterType.getEncounterDatetime().after(startDate1) &&
                            encounterType.getEncounterDatetime().before(cutOff))
                    .collect(Collectors.toSet());
            uniqueEncounters.forEach(encounterType -> {
                CustomLineList customLineList = customLineListRepository
                        .findByIdentifierAndVisit(identifier, encounterType.getEncounterDatetime())
                        .orElse(new CustomLineList());
                customLineList.setFacilityName(facilityName);
                customLineList.setIdentifier(identifier);
                customLineList.setGender(gender);
                customLineList.setAge(age);
                customLineList.setKpIdentifier(kpIdentifier);
                customLineList.setDateOfHivDiagnosis(dateOfHivDiagnosis);
                customLineList.setArtStartDate(artStartDate);
                customLineList.setVisit(encounterType.getEncounterDatetime());
                customLineList.setDob(dob);
                customLineList.setPregnancyStatus(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 165050 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                if (customLineList.getPregnancyStatus().equals("Pregnant")) {
                    customLineList.setPregnancyStatusDate(container.getMessageData().getObs().stream()
                            .filter(obsType -> obsType.getConceptId() == 165050 &&
                                    obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                    obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
                            .map(ObsType::getObsDatetime).findFirst().orElse(null));
                }
                customLineList.setPregnancyDueDate(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 5596 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueDatetime() != null)
                        .map(ObsType::getValueDatetime).findFirst().orElse(null));
                customLineList.setPickUpReason(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 165774 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 &&
                                obsType.getValueCoded() != 47 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                List<Integer> regimenConcepts = new ArrayList<>(Arrays.asList(164507,164514,165703,164506,164513,165702));
                customLineList.setRegimen(container.getMessageData().getObs().stream()
                        .filter(obsType -> regimenConcepts.contains(obsType.getConceptId()) &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setRegimenDate(container.getMessageData().getObs().stream()
                        .filter(obsType -> regimenConcepts.contains(obsType.getConceptId()) &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
                        .map(ObsType::getObsDatetime).findFirst().orElse(null));
                customLineList.setDispensedQuantity(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 1443 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
                        .map(obsType -> obsType.getValueNumeric().intValue()).findFirst().orElse(0));
                customLineList.setDispensedDate(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 1443 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getObsDatetime() != null)
                        .map(ObsType::getObsDatetime).findFirst().orElse(null));
                customLineList.setDaysOfArv(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 159368 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
                        .map(obsType -> obsType.getValueNumeric().intValue()).findFirst().orElse(0));
                customLineList.setDispenseModality(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 166148 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setDddDispensing(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 166363 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setViralLoad(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 856 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueNumeric() != null)
                        .map(obsType -> obsType.getValueNumeric().doubleValue()).findFirst().orElse(null));
                customLineList.setViralLoadSampleCollectionDate(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 159951 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueDatetime() != null)
                        .map(ObsType::getValueDatetime).findFirst().orElse(null));
                customLineList.setTransferInStatus(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 165242 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setPatientOutcome(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 165470 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setEacDate(encounterType.getFormId() == 69 ? encounterType.getEncounterDatetime() : null);
                customLineList.setEacSessionType(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 166097 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineList.setEacBarrier(container.getMessageData().getObs().stream()
                        .filter(obsType -> obsType.getConceptId() == 165457 &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                List<Integer> eacConcepts = new ArrayList<>(Arrays.asList(165501,165021));
                customLineList.setEacIntervention(container.getMessageData().getObs().stream()
                        .filter(obsType -> eacConcepts.contains(obsType.getConceptId()) &&
                                obsType.getObsDatetime().equals(encounterType.getEncounterDatetime()) &&
                                obsType.getVoided() == 0 && obsType.getValueCoded() != 166160 &&
                                obsType.getVariableValue() != null)
                        .map(ObsType::getVariableValue).findFirst().orElse(""));
                customLineListRepository.save(customLineList);
            });
        }
    }
}
