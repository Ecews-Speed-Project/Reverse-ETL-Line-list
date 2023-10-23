package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.linelists.BiometricLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.entities.State;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.model.PatientBiometricType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.BiometricLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.read.StateRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.BiometricService;
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
public class BiometricServiceImpl implements BiometricService {
    private final ContainerRepository containerRepository;
    private final BiometricLineListRepository biometricLineListRepository;
    private final HelperFunctions helperFunctions;
    private final FacilityRepository facilityRepository;
    private final StateRepository stateRepository;

    @Override
    public void processBiometricLineList() {
        List<State> states = stateRepository.findAll();
        states.forEach(state -> {
            List<String> datimCodes = facilityRepository.findStateFacilitiesDatimCodes(state.getId());
            LineListTracker lineListTracker = helperFunctions.getLineListTracker(
                    PROCESSING.name(), state.getStateName()+"_BIOMETRIC");
            lineListTracker.setPageSize(1000);
            int page = lineListTracker.getCurrentPage();
            int pageSize = lineListTracker.getPageSize();
            while (true) {
                log.info("Processing page {} of state {} with {} facilities",
                        page+1, state.getStateName(), datimCodes.size());
                Pageable pageable = PageRequest.of(page, pageSize);
                Page<IdWrapper> idWrappers = containerRepository
                        .findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
                                datimCodes, 4, pageable);
                if (!idWrappers.hasContent()) {
                    log.info("Finished processing state {} with {} facilities", state.getStateName(), datimCodes.size());
                    lineListTracker.setDateCompleted(LocalDateTime.now());
                    lineListTracker.setStatus(PROCESSED.name());
                    helperFunctions.saveLineListTracker(lineListTracker);
                    break;
                }
                List<IdWrapper> idWrapperList = idWrappers.getContent();
                processBiometricLineList(idWrapperList);
                helperFunctions.updateLineListTracker(lineListTracker, ++page, idWrappers, idWrapperList);
            }
        });
    }

    @Override
    public void processBiometricLineList(List<IdWrapper> idWrappers) {
        idWrappers.forEach(idWrapper -> {
            try {
                Container container = containerRepository.findById(idWrapper.getId()).orElse(null);
                if (container == null) {
                    log.error("Container with id {} not found", idWrapper.getId());
                } else {
                    Date lastPickupDate = null;
                    Long daysOfArvRefill = null;
                    String patientOutcome = null;
                    Date patientOutcomeDate = null;
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
                        patientOutcomeDate = patientOutComeObs.get().getObsDatetime();
                    }
                    String currentArtStatus = helperFunctions.getCurrentArtStatus(lastPickupDate, daysOfArvRefill, new Date(), patientOutcome);
//                    if (currentArtStatus.equals("Active")) {
                        List<PatientBiometricType> biometricTypes = container.getMessageData().getPatientBiometrics();
                        Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
                        BiometricLineList biometricLineList = biometricLineListRepository
                                .findByPatientUuidAndDatimCode(container.getId(), container.getMessageHeader().getFacilityDatimCode())
                                .orElse(new BiometricLineList());
                        biometricLineList.setState(facility.getState().getStateName());
                        biometricLineList.setFacilityName(facility.getFacilityName());
                        biometricLineList.setDatimCode(facility.getDatimCode());
                        biometricLineList.setPatientUuid(container.getId());
                        biometricLineList.setPatientUniqueId(helperFunctions.returnIdentifiers(4, container).orElse(null));
                        biometricLineList.setSex(container.getMessageData().getDemographics().getGender());
                        biometricLineList.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate() != null ?
                                convertDate(container.getMessageData().getDemographics().getBirthdate()) : null);
                        biometricLineList.setCurrentAge(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, new Date()));
                        Date dateCaptured = helperFunctions.getBiometricCaptureDate(container);
                        biometricLineList.setDateCaptured(dateCaptured != null ? convertDate(dateCaptured) : null);
                        biometricLineList.setNumberOfFingerPrints(biometricTypes.size());
                        Date artStartDate = helperFunctions.getArtStartDate(container, new Date());
                        biometricLineList.setArtStartDate(artStartDate != null ? convertDate(artStartDate) : null);
                        biometricLineList.setCurrentArtStatus(currentArtStatus);
                        biometricLineList.setLastPickupDate(lastPickupDate != null ? convertDate(lastPickupDate) : null);
                        Date dateTransferredIn = helperFunctions.getMaxObsByConceptID(160534, container, new Date())
                                .map(ObsType::getValueDatetime).orElse(null);
                        biometricLineList.setDateTransferredIn(dateTransferredIn != null ? convertDate(dateTransferredIn) : null);
                        biometricLineList.setTransferredInStatus(helperFunctions.getMaxObsByConceptID(165242, container, new Date())
                                .map(ObsType::getVariableValue).orElse(null));
                        if (patientOutcome != null && patientOutcome.equals("Transferred out")) {
                            biometricLineList.setTransferredOutStatus(patientOutcome);
                            biometricLineList.setDateTransferredOut(patientOutcomeDate != null ? convertDate(patientOutcomeDate) : null);
                        }
                        biometricLineList.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, new Date())
                                .map(ObsType::getVariableValue).orElse(null));
                        Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 5096, container, new Date());
                        pharmacyNextAppointment.ifPresent(obsType ->
                                biometricLineList.setPharmacyNextAppointmentDate(obsType.getValueDatetime() != null ? convertDate(obsType.getValueDatetime()) : null));
                        Optional<ObsType> dsdObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 166148, container, new Date());
                        dsdObs.ifPresent(obsType -> biometricLineList.setDsdModel(obsType.getVariableValue()));
                        Optional<ObsType> mmdObs = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 166278, container, new Date());
                        mmdObs.ifPresent(obsType -> biometricLineList.setMmdModel(obsType.getVariableValue()));
                        if (!biometricTypes.isEmpty()) {
                            biometricLineList.setSerialNumber(biometricTypes.get(0).getSerialNumber());
                            biometricTypes.forEach(patientBiometricType -> {
                                switch (patientBiometricType.getFingerPosition()) {
                                    case "LeftThumb":
                                        biometricLineList.setLeftThumbQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "LeftIndex":
                                        biometricLineList.setLeftIndexQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "LeftMiddle":
                                        biometricLineList.setLeftMiddleQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "LeftWedding":
                                        biometricLineList.setLeftWeddingQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "LeftSmall":
                                        biometricLineList.setLeftSmallQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "RightThumb":
                                        biometricLineList.setRightThumbQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "RightIndex":
                                        biometricLineList.setRightIndexQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "RightMiddle":
                                        biometricLineList.setRightMiddleQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "RightWedding":
                                        biometricLineList.setRightWeddingQuality(patientBiometricType.getImageQuality());
                                        break;
                                    case "RightSmall":
                                        biometricLineList.setRightSmallQuality(patientBiometricType.getImageQuality());
                                        break;
                                    default:
                                        break;
                                }
                            });
                        }
                        biometricLineListRepository.save(biometricLineList);
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error processing biometric line list for patient {}\n with error message {}", idWrapper.getId(), e.getMessage());
            }
        });
    }
}
