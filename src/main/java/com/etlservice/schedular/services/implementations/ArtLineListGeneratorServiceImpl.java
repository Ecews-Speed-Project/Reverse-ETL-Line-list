package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.entities.linelists.ArtLinelist;
import com.etlservice.schedular.entities.linelists.CustomArtLineList;
import com.etlservice.schedular.entities.ErrorReport;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.ArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.CustomArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.ErrorReportRepository;
import com.etlservice.schedular.services.ArtLineListGeneratorService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static com.etlservice.schedular.utils.ConstantsUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtLineListGeneratorServiceImpl implements ArtLineListGeneratorService {
    private final HelperFunctions helperFunctions;
    private final ArtLineListRepository artLineListRepository;
    private final CustomArtLineListRepository customArtLineListRepository;
    private final ErrorReportRepository errorReportRepository;

    @Override
    public ArtLinelist mapARTLineList(Container container, Facility facility, Date cutOff, String quarter) {
        Optional<String> uniqueId = helperFunctions.returnIdentifiers(4, container);
        if (uniqueId.isPresent()) {
            String patientUniqueId = uniqueId.get();
            ErrorReport errorReport = errorReportRepository.findByPatientId(container.getMessageData().getDemographics().getPatientUuid())
                    .orElse(null);
            ArtLinelist artLinelist = artLineListRepository.findByPatientUuidAndQuarterAndDatimCode(container.getId(), quarter, facility.getDatimCode())
                    .orElse(artLineListRepository.findByPatientUuidAndQuarterAndDatimCode(container.getMessageData().getDemographics().getPatientUuid(), quarter, facility.getDatimCode())
                            .orElse(new ArtLinelist()));
            if (errorReport != null)
                artLinelist.setHasCriticalError(errorReport.isHasCriticalError());
            setConstantVariablesOnArtLineList(container, cutOff, quarter, patientUniqueId, artLinelist);

            artLinelist.setFacilityName(facility.getFacilityName());
            artLinelist.setDateTransferredIn(helperFunctions.getMaxObsByConceptID(160534, container, cutOff)
                    .map(ObsType::getValueDatetime).orElse(null));
            artLinelist.setTransferInStatus(helperFunctions.getMaxObsByConceptID(165242, container, cutOff)
                    .map(ObsType::getVariableValue).orElse(null));

            Optional<Date> firstPickupDate = helperFunctions.getMinConceptObsIdWithFormId(27, 162240, container, cutOff)
                    .map(ObsType::getObsDatetime);
            firstPickupDate.ifPresent(artLinelist::setFirstPickupDate);

            setLastPickupDateVariableOnArtLineList(container, cutOff, artLinelist);

            artLinelist.setLastVisitDate(helperFunctions.getMaxVisitDate(container, cutOff).orElse(null));
            artLinelist.setMonthsOnArt(helperFunctions.getMonthsOnArt(container, artLinelist.getLastPickupDate(), cutOff));
            Optional<ObsType> maxObsByConceptID1 = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 166406, container, cutOff);
            maxObsByConceptID1.ifPresent(obsType -> artLinelist.setPillBalance(NumberUtils.isDigits(obsType.getValueText()) ? Long.parseLong(obsType.getValueText()) : null));

            setCd4OptVariables(container, cutOff, artLinelist);

            setRegimenVariables(container, cutOff, artLinelist);

            //Update by Tayo oyelekan

            Optional<ObsType> baseLineINHStartDate = helperFunctions.getMaxConceptObsIdWithFormId(56,164852, container, cutOff);
            baseLineINHStartDate.ifPresent(obsType -> artLinelist.setBaselineInhStartDate(obsType.getValueDatetime()));

            Optional<ObsType> baseLineINHStopDate = helperFunctions.getMaxConceptObsIdWithFormId(56,166096, container, cutOff);
            baseLineINHStopDate.ifPresent(obsType -> artLinelist.setBaselineInhStopDate(obsType.getValueDatetime()));

            artLinelist.setBaselineTbTreatmentStartDate(helperFunctions.getMaxObsByConceptID(1113, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

            artLinelist.setBaselineTbTreatmentStopDate(helperFunctions.getMaxObsByConceptID(159431, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

            Optional<ObsType> clinicalNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5096, container, cutOff);
            clinicalNextAppointment.ifPresent(obsType -> artLinelist.setClinicalNextAppointment(obsType.getValueDatetime()));

            artLinelist.setCurrentAgeYrs(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff));
            if (artLinelist.getCurrentAgeYrs() < 5)
                artLinelist.setCurrentAgeMonths(helperFunctions.getCurrentAge(container, AGE_TYPE_MONTHS, cutOff));
            else
                artLinelist.setCurrentAgeMonths(0);

            setPatientOutcomeAndCurrentArtStatus(container, cutOff, artLinelist);

            Optional<ObsType> currentINHOutcome = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,166007, container, cutOff);
            currentINHOutcome.ifPresent(obsType -> artLinelist.setCurrentInhOutcome(obsType.getVariableValue()));

            Optional<ObsType> currentINHOutcomeDate = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,166008, container, cutOff);
            currentINHOutcomeDate.ifPresent(obsType -> artLinelist.setCurrentInhOutcomeDate(obsType.getValueDatetime()));

            artLinelist.setBiometricCaptured(helperFunctions.getBiometricCaptured(container));
            artLinelist.setBiometricCaptureDate(helperFunctions.getBiometricCaptureDate(container));
            Optional<ObsType> currentINHStartDate = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,165994, container, cutOff);
            currentINHStartDate.ifPresent(obsType -> artLinelist.setCurrentInhStartDate(obsType.getValueDatetime()));

            setCurrentViralLoadOnArtLineList(container, cutOff, artLinelist);

            artLinelist.setLastViralLoadSampleCollectionFormDate(helperFunctions.getMaxEncounterDateTime(SAMPLE_COLLECTION_FORM, container, cutOff));

            Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
            currentWeight.ifPresent(obsType -> {
                artLinelist.setCurrentWeight((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                        Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                        obsType.getValueNumeric().doubleValue() : 0.0);
                artLinelist.setCurrentWeightDate(obsType.getObsDatetime());
            });

            artLinelist.setEnrollmentDate(helperFunctions.getEnrollmentDate(container));

            Optional<ObsType> initialFirstLineRegimen = helperFunctions.getInitialRegimenLine(164506,164507,container);
            initialFirstLineRegimen.ifPresent(obsType -> {
                artLinelist.setInitialFirstLineRegimen(obsType.getVariableValue());
                artLinelist.setInitialFirstLineRegimenDate(obsType.getObsDatetime());
            });

            Optional<ObsType> initialSecondLineRegimen = helperFunctions.getInitialRegimenLine(164513,164514,container);
            initialSecondLineRegimen.ifPresent(obsType -> {
                artLinelist.setInitialSecondLineRegimen(obsType.getVariableValue());
                artLinelist.setInitialSecondLineRegimenDate(obsType.getObsDatetime());
            });

            artLinelist.setLastEacDate(helperFunctions.getMaxEncounterDateTime(69, container, cutOff));

            Optional<Date> lastINHDispensedDate = helperFunctions.getLastINHDispensedDate(165727,1679, PHARMACY_FORM, container, cutOff);
            lastINHDispensedDate.ifPresent(artLinelist::setLastInhDispensedDate);

            Optional<ObsType> lastSampleTakenDate = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,159951, container, cutOff);
            lastSampleTakenDate.ifPresent(obsType -> artLinelist.setLastSampleTakenDate(obsType.getValueDatetime()));

            artLinelist.setLgaName(facility.getLga().getLga());
            artLinelist.setState(facility.getState().getStateName());

            int deathMark = container.getMessageData().getDemographics().getDead();
            if (deathMark == 1) {
                artLinelist.setMarkAsDeceased("Dead");
                artLinelist.setMarkAsDeceasedDeathDate(container.getMessageData().getDemographics().getDeathDate());
            }

            Optional<ObsType> nextOfKinPhoneNo = helperFunctions.getMaxObsByConceptAndEncounterTypeID(ENROLLMENT_TYPE,159635, container, cutOff);
            nextOfKinPhoneNo.ifPresent(obsType -> artLinelist.setNextOfKinPhoneNo((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                    obsType.getVariableValue() : obsType.getValueText() != null ? obsType.getValueText() : ""));

            artLinelist.setOtzEnrollmentDate(helperFunctions.getMaxEncounterDateTime(OTZ_FORM, container, cutOff));
            Optional<ObsType> otzOutCOmeDate = helperFunctions.getMaxConceptObsIdWithFormId(OTZ_FORM,166008, container, cutOff);
            otzOutCOmeDate.ifPresent(obsType -> artLinelist.setOtzOutcomeDate(obsType.getValueDatetime()));

            Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
            pharmacyNextAppointment.ifPresent(obsType -> artLinelist.setPharmacyNextAppointment(obsType.getValueDatetime()));


            setSexAndPregnancyStatusVariablesOnArtLineList(container, cutOff, artLinelist);

            Optional<ObsType> tbStatus = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,1659, container, cutOff);
            if(tbStatus.isPresent()) {
                artLinelist.setTbStatus(tbStatus.get().getVariableValue());
                artLinelist.setTbStatusDate(tbStatus.get().getObsDatetime());
            }

            Optional<ObsType> treatmentSupporterPhoneNo = helperFunctions.getMaxObsByConceptAndEncounterTypeID(ENROLLMENT_TYPE,160642, container, cutOff);
            treatmentSupporterPhoneNo.ifPresent(obsType -> artLinelist.setTreatmentSupporterPhoneNo((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                    obsType.getVariableValue() : obsType.getValueText() != null ? obsType.getValueText() : ""));

            artLinelist.setValidCapture(helperFunctions.getValidCapture(container));

            setVariablesForPreviousQuarterOnArtLineList(container, cutOff, artLinelist);

            setPatientAgeRangeOnArtLineList(artLinelist);

            return artLinelist;
        } else
            return null;
    }

    @Override
    public CustomArtLineList mapCustomArtLineList(Container container, Facility facility, Date cutOff, String quarter) {
        Optional<String> uniqueId = helperFunctions.returnIdentifiers(4, container);
        if (uniqueId.isPresent()) {
            String patientUniqueId = uniqueId.get();
            ErrorReport errorReport = errorReportRepository.findByPatientId(container.getMessageData().getDemographics().getPatientUuid())
                    .orElse(null);
            CustomArtLineList artLinelist = customArtLineListRepository.findByPatientUuidAndDatimCodeAndQuarter(container.getId(), facility.getDatimCode(), quarter)
                    .orElse(customArtLineListRepository.findByPatientUuidAndDatimCodeAndQuarter(container.getMessageData().getDemographics().getPatientUuid(), facility.getDatimCode(), quarter)
                            .orElse(new CustomArtLineList()));
            if (errorReport != null)
                artLinelist.setHasCriticalError(errorReport.isHasCriticalError());
            setConstantVariablesOnArtLineList(container, cutOff, quarter, patientUniqueId, artLinelist);

            artLinelist.setFacilityName(facility.getFacilityName());
            artLinelist.setDateTransferredIn(helperFunctions.getMaxObsByConceptID(160534, container, cutOff)
                    .map(ObsType::getValueDatetime).orElse(null));
            artLinelist.setTransferInStatus(helperFunctions.getMaxObsByConceptID(165242, container, cutOff)
                    .map(ObsType::getVariableValue).orElse(null));

            Optional<Date> firstPickupDate = helperFunctions.getMinConceptObsIdWithFormId(27, 162240, container, cutOff)
                    .map(ObsType::getObsDatetime);
            firstPickupDate.ifPresent(artLinelist::setFirstPickupDate);

            setLastPickupDateVariableOnArtLineList(container, cutOff, artLinelist);

            artLinelist.setLastVisitDate(helperFunctions.getMaxVisitDate(container, cutOff).orElse(null));
            artLinelist.setMonthsOnArt(helperFunctions.getMonthsOnArt(container, artLinelist.getLastPickupDate(), cutOff));
            Optional<ObsType> maxObsByConceptID1 = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 166406, container, cutOff);
            maxObsByConceptID1.ifPresent(obsType -> artLinelist.setPillBalance(NumberUtils.isDigits(obsType.getValueText()) ? Long.parseLong(obsType.getValueText()) : null));

            setCd4OptVariables(container, cutOff, artLinelist);

            setRegimenVariables(container, cutOff, artLinelist);

            //Update by Tayo oyelekan

            Optional<ObsType> baseLineINHStartDate = helperFunctions.getMaxConceptObsIdWithFormId(56,164852, container, cutOff);
            baseLineINHStartDate.ifPresent(obsType -> artLinelist.setBaselineInhStartDate(obsType.getValueDatetime()));

            Optional<ObsType> baseLineINHStopDate = helperFunctions.getMaxConceptObsIdWithFormId(56,166096, container, cutOff);
            baseLineINHStopDate.ifPresent(obsType -> artLinelist.setBaselineInhStopDate(obsType.getValueDatetime()));

            artLinelist.setBaselineTbTreatmentStartDate(helperFunctions.getMaxObsByConceptID(1113, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

            artLinelist.setBaselineTbTreatmentStopDate(helperFunctions.getMaxObsByConceptID(159431, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

            Optional<ObsType> clinicalNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5096, container, cutOff);
            clinicalNextAppointment.ifPresent(obsType -> artLinelist.setClinicalNextAppointment(obsType.getValueDatetime()));

            artLinelist.setCurrentAgeYrs(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff));
            if (artLinelist.getCurrentAgeYrs() < 5)
                artLinelist.setCurrentAgeMonths(helperFunctions.getCurrentAge(container, AGE_TYPE_MONTHS, cutOff));
            else
                artLinelist.setCurrentAgeMonths(0);

            Optional<ObsType> dateConfirmedPositive = helperFunctions.getMaxObsByConceptID(160554, container, cutOff);
            dateConfirmedPositive.ifPresent(obsType -> artLinelist.setDateConfirmedPositive(obsType.getValueDatetime()));

            setPatientOutcomeAndCurrentArtStatus(container, cutOff, artLinelist);

            Optional<ObsType> currentINHOutcome = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,166007, container, cutOff);
            currentINHOutcome.ifPresent(obsType -> artLinelist.setCurrentInhOutcome(obsType.getVariableValue()));

            Optional<ObsType> currentINHOutcomeDate = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,166008, container, cutOff);
            currentINHOutcomeDate.ifPresent(obsType -> artLinelist.setCurrentInhOutcomeDate(obsType.getValueDatetime()));

            artLinelist.setBiometricCaptured(helperFunctions.getBiometricCaptured(container));
            artLinelist.setBiometricCaptureDate(helperFunctions.getBiometricCaptureDate(container));
            Optional<ObsType> currentINHStartDate = helperFunctions.getMaxConceptObsIdWithFormId(IPT_FORM,165994, container, cutOff);
            currentINHStartDate.ifPresent(obsType -> artLinelist.setCurrentInhStartDate(obsType.getValueDatetime()));

            setCurrentViralLoadOnArtLineList(container, cutOff, artLinelist);

            artLinelist.setLastViralLoadSampleCollectionFormDate(helperFunctions.getMaxEncounterDateTime(SAMPLE_COLLECTION_FORM, container, cutOff));

            Optional<ObsType> currentWeight = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,5089, container, cutOff);
            currentWeight.ifPresent(obsType -> {
                artLinelist.setCurrentWeight((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                        Double.parseDouble(obsType.getVariableValue()) : obsType.getValueNumeric() != null ?
                        obsType.getValueNumeric().doubleValue() : 0.0);
                artLinelist.setCurrentWeightDate(obsType.getObsDatetime());
            });

            artLinelist.setEnrollmentDate(helperFunctions.getEnrollmentDate(container));

            Optional<ObsType> initialFirstLineRegimen = helperFunctions.getInitialRegimenLine(164506,164507,container);
            initialFirstLineRegimen.ifPresent(obsType -> {
                artLinelist.setInitialFirstLineRegimen(obsType.getVariableValue());
                artLinelist.setInitialFirstLineRegimenDate(obsType.getObsDatetime());
            });

            Optional<ObsType> initialSecondLineRegimen = helperFunctions.getInitialRegimenLine(164513,164514,container);
            initialSecondLineRegimen.ifPresent(obsType -> {
                artLinelist.setInitialSecondLineRegimen(obsType.getVariableValue());
                artLinelist.setInitialSecondLineRegimenDate(obsType.getObsDatetime());
            });

            artLinelist.setLastEacDate(helperFunctions.getMaxEncounterDateTime(69, container, cutOff));

            Optional<Date> lastINHDispensedDate = helperFunctions.getLastINHDispensedDate(165727,1679, PHARMACY_FORM, container, cutOff);
            lastINHDispensedDate.ifPresent(artLinelist::setLastInhDispensedDate);

            Optional<ObsType> lastSampleTakenDate = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,159951, container, cutOff);
            lastSampleTakenDate.ifPresent(obsType -> artLinelist.setLastSampleTakenDate(obsType.getValueDatetime()));

            artLinelist.setLgaName(facility.getLga().getLga());
            artLinelist.setState(facility.getState().getStateName());

            int deathMark = container.getMessageData().getDemographics().getDead();
            if (deathMark == 1) {
                artLinelist.setMarkAsDeceased("Dead");
                artLinelist.setMarkAsDeceasedDeathDate(container.getMessageData().getDemographics().getDeathDate());
            }

            Optional<ObsType> nextOfKinPhoneNo = helperFunctions.getMaxObsByConceptAndEncounterTypeID(ENROLLMENT_TYPE,159635, container, cutOff);
            nextOfKinPhoneNo.ifPresent(obsType -> artLinelist.setNextOfKinPhoneNo((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                    obsType.getVariableValue() : obsType.getValueText() != null ? obsType.getValueText() : ""));

            artLinelist.setOtzEnrollmentDate(helperFunctions.getMaxEncounterDateTime(OTZ_FORM, container, cutOff));
            Optional<ObsType> otzOutCOmeDate = helperFunctions.getMaxConceptObsIdWithFormId(OTZ_FORM,166008, container, cutOff);
            otzOutCOmeDate.ifPresent(obsType -> artLinelist.setOtzOutcomeDate(obsType.getValueDatetime()));

            Optional<ObsType> pharmacyNextAppointment = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,5096, container, cutOff);
            pharmacyNextAppointment.ifPresent(obsType -> artLinelist.setPharmacyNextAppointment(obsType.getValueDatetime()));


            setSexAndPregnancyStatusVariablesOnArtLineList(container, cutOff, artLinelist);

            Optional<ObsType> tbStatus = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD,1659, container, cutOff);
            if(tbStatus.isPresent()) {
                artLinelist.setTbStatus(tbStatus.get().getVariableValue());
                artLinelist.setTbStatusDate(tbStatus.get().getObsDatetime());
            }

            Optional<ObsType> treatmentSupporterPhoneNo = helperFunctions.getMaxObsByConceptAndEncounterTypeID(ENROLLMENT_TYPE,160642, container, cutOff);
            treatmentSupporterPhoneNo.ifPresent(obsType -> artLinelist.setTreatmentSupporterPhoneNo((obsType.getVariableValue() != null && !obsType.getVariableValue().isEmpty()) ?
                    obsType.getVariableValue() : obsType.getValueText() != null ? obsType.getValueText() : ""));

            artLinelist.setValidCapture(helperFunctions.getValidCapture(container));

            setVariablesForPreviousQuarterOnArtLineList(container, cutOff, artLinelist);

            setPatientAgeRangeOnArtLineList(artLinelist);

            return artLinelist;
        } else
            return null;
    }

    private void setRegimenVariables(Container container, Date cutOff, ArtLinelist artLinelist) {
        Optional<ObsType> initialRegLine = helperFunctions.getMinObsByConceptID(165708, container, cutOff);
        initialRegLine.ifPresent(obsType -> {
            artLinelist.setInitialRegimenLine(obsType.getVariableValue());

            Optional<ObsType> initialReg = helperFunctions.getInitialRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
            initialReg.ifPresent(obsType1 -> artLinelist.setInitialRegimen(obsType1.getVariableValue()));
        });

        Optional<ObsType> currentRegimenLine = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,165708, container, cutOff);
        currentRegimenLine.ifPresent(obsType -> {
            artLinelist.setCurrentRegimenLine(obsType.getVariableValue());
            Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
            currentRegimenType.ifPresent(obsType1 -> artLinelist.setCurrentRegimen(obsType1.getVariableValue()));
        });
    }

    private void setRegimenVariables(Container container, Date cutOff, CustomArtLineList artLinelist) {
        Optional<ObsType> initialRegLine = helperFunctions.getMinObsByConceptID(165708, container, cutOff);
        initialRegLine.ifPresent(obsType -> {
            artLinelist.setInitialRegimenLine(obsType.getVariableValue());

            Optional<ObsType> initialReg = helperFunctions.getInitialRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
            initialReg.ifPresent(obsType1 -> artLinelist.setInitialRegimen(obsType1.getVariableValue()));
        });

        Optional<ObsType> currentRegimenLine = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM,165708, container, cutOff);
        currentRegimenLine.ifPresent(obsType -> {
            artLinelist.setCurrentRegimenLine(obsType.getVariableValue());
            Optional<ObsType> currentRegimenType = helperFunctions.getCurrentRegimen(obsType.getEncounterId(), obsType.getValueCoded(), container, cutOff);
            currentRegimenType.ifPresent(obsType1 -> artLinelist.setCurrentRegimen(obsType1.getVariableValue()));
        });
    }

    private void setCd4OptVariables(Container container, Date cutOff, ArtLinelist artLinelist) {
        Optional<ObsType> initialCd4Opt = helperFunctions.getMinConceptObsIdWithFormId(
                LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
        );
        initialCd4Opt.ifPresent(obsType -> {
            artLinelist.setInitialCd4Count(obsType.getValueNumeric() == null ? null : obsType.getValueNumeric().longValue());
            artLinelist.setInitialCd4CountDate(obsType.getObsDatetime());
        });

        Optional<ObsType> currentCd4Opt = helperFunctions.getMaxConceptObsIdWithFormId(
                LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
        );
        currentCd4Opt.ifPresent(obsType -> {
            artLinelist.setCurrentCd4Count(obsType.getValueNumeric() == null ? null : obsType.getValueNumeric().longValue());
            artLinelist.setCurrentCd4CountDate(obsType.getObsDatetime());
        });
    }

    private void setCd4OptVariables(Container container, Date cutOff, CustomArtLineList artLinelist) {
        Optional<ObsType> initialCd4Opt = helperFunctions.getMinConceptObsIdWithFormId(
                LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
        );
        initialCd4Opt.ifPresent(obsType -> {
            artLinelist.setInitialCd4Count(obsType.getValueNumeric() == null ? null : obsType.getValueNumeric().longValue());
            artLinelist.setInitialCd4CountDate(obsType.getObsDatetime());
        });

        Optional<ObsType> currentCd4Opt = helperFunctions.getMaxConceptObsIdWithFormId(
                LABORATORY_ORDER_AND_RESULT_FORM, 5497, container, cutOff
        );
        currentCd4Opt.ifPresent(obsType -> {
            artLinelist.setCurrentCd4Count(obsType.getValueNumeric() == null ? null : obsType.getValueNumeric().longValue());
            artLinelist.setCurrentCd4CountDate(obsType.getObsDatetime());
        });
    }

    private void setPatientOutcomeAndCurrentArtStatus(Container container, Date cutOff, ArtLinelist artLinelist) {
        artLinelist.setDateOfTermination(helperFunctions.getMaxObsByConceptID(165469, container, cutOff).map(ObsType::getValueDatetime).orElse(null));
        artLinelist.setDateReturnedToCare(helperFunctions.getMaxObsByConceptID(165775, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

        Optional<ObsType> patientOutCome = helperFunctions.getMaxConceptObsIdWithFormId(
                CLIENT_TRACKING_AND_TERMINATION_FORM,165470, container, cutOff);
        if(patientOutCome.isPresent()) {
            artLinelist.setPatientOutcome(patientOutCome.get().getVariableValue());
            artLinelist.setPatientOutcomeDate(patientOutCome.get().getObsDatetime());
        }
        String status = helperFunctions.getCurrentArtStatus(artLinelist.getLastPickupDate(), artLinelist.getDaysOfArvRefil(),
                cutOff, artLinelist.getPatientOutcome());
        artLinelist.setCurrentArtStatus(status);
    }

    private void setPatientOutcomeAndCurrentArtStatus(Container container, Date cutOff, CustomArtLineList artLinelist) {
        artLinelist.setDateOfTermination(helperFunctions.getMaxObsByConceptID(165469, container, cutOff).map(ObsType::getValueDatetime).orElse(null));
        artLinelist.setDateReturnedToCare(helperFunctions.getMaxObsByConceptID(165775, container, cutOff).map(ObsType::getValueDatetime).orElse(null));

        Optional<ObsType> patientOutCome = helperFunctions.getMaxConceptObsIdWithFormId(
                CLIENT_TRACKING_AND_TERMINATION_FORM,165470, container, cutOff);
        if(patientOutCome.isPresent()) {
            artLinelist.setPatientOutcome(patientOutCome.get().getVariableValue());
            artLinelist.setPatientOutcomeDate(patientOutCome.get().getObsDatetime());
        }
        String status = helperFunctions.getCurrentArtStatus(artLinelist.getLastPickupDate(), artLinelist.getDaysOfArvRefil(),
                cutOff, artLinelist.getPatientOutcome());
        artLinelist.setCurrentArtStatus(status);
    }

    private void setSexAndPregnancyStatusVariablesOnArtLineList(Container container, Date cutOff, ArtLinelist artLinelist) {
        artLinelist.setSex(container.getMessageData().getDemographics().getGender());

        if (artLinelist.getSex() != null && artLinelist.getSex().equals("F")) {
            Optional<ObsType> pregnancyStatus = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 165050, container, cutOff);
            pregnancyStatus.ifPresent(obsType -> {
                artLinelist.setPregnancyStatus(obsType.getVariableValue());

                artLinelist.setPregnancyStatusDate(obsType.getObsDatetime());

                artLinelist.setLastDeliveryDate(obsType.getValueDatetime());

                Optional<ObsType> edd = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),5596, container, cutOff);
                edd.ifPresent(obsType1 -> artLinelist.setEdd(obsType1.getValueDatetime()));

                Optional<ObsType> lmp = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 1427, container, cutOff);
                lmp.ifPresent(obsType1 -> artLinelist.setLmp(obsType1.getValueDatetime()));

                Optional<ObsType> gestationAgeWeeks = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),1438, container, cutOff);
                gestationAgeWeeks.ifPresent(obsType1 -> artLinelist.setGestationAgeWeeks(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));
            });
        }
    }

    private void setSexAndPregnancyStatusVariablesOnArtLineList(Container container, Date cutOff, CustomArtLineList artLinelist) {
        artLinelist.setSex(container.getMessageData().getDemographics().getGender());

        if (artLinelist.getSex() != null && artLinelist.getSex().equals("F")) {
            Optional<ObsType> pregnancyStatus = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 165050, container, cutOff);
            pregnancyStatus.ifPresent(obsType -> {
                artLinelist.setPregnancyStatus(obsType.getVariableValue());

                artLinelist.setPregnancyStatusDate(obsType.getObsDatetime());

                artLinelist.setLastDeliveryDate(obsType.getValueDatetime());

                Optional<ObsType> edd = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),5596, container, cutOff);
                edd.ifPresent(obsType1 -> artLinelist.setEdd(obsType1.getValueDatetime()));

                Optional<ObsType> lmp = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 1427, container, cutOff);
                lmp.ifPresent(obsType1 -> artLinelist.setLmp(obsType1.getValueDatetime()));

                Optional<ObsType> gestationAgeWeeks = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),1438, container, cutOff);
                gestationAgeWeeks.ifPresent(obsType1 -> artLinelist.setGestationAgeWeeks(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));
            });
        }
    }

    public static void setPatientAgeRangeOnArtLineList(ArtLinelist artLinelist) {
        String ageRange;
        if (artLinelist.getCurrentAgeYrs() != null) {
            int age = artLinelist.getCurrentAgeYrs();
            if (age < 1) {
                ageRange = "<1";
            } else if (age < 5) {
                ageRange = "1-4";
            } else if (age < 10) {
                ageRange = "5-9";
            } else if (age < 15) {
                ageRange = "10-14";
            } else if (age < 20) {
                ageRange = "15-19";
            } else if (age < 25) {
                ageRange = "20-24";
            } else if (age < 30) {
                ageRange = "25-29";
            } else if (age < 35) {
                ageRange = "30-34";
            } else if (age < 40) {
                ageRange = "35-39";
            } else if (age < 45) {
                ageRange = "40-44";
            } else if (age < 50) {
                ageRange = "45-49";
            } else if (age < 55) {
                ageRange = "50-54";
            } else if (age < 60) {
                ageRange = "55-59";
            } else if (age < 65) {
                ageRange = "60-64";
            } else {
                ageRange = "65+";
            }
        } else {
            ageRange = "<1";
        }
        artLinelist.setAgeRange(ageRange);
    }

    public static void setPatientAgeRangeOnArtLineList(CustomArtLineList artLinelist) {
        String ageRange;
        if (artLinelist.getCurrentAgeYrs() != null) {
            int age = artLinelist.getCurrentAgeYrs();
            if (age < 1) {
                ageRange = "<1";
            } else if (age < 5) {
                ageRange = "1-4";
            } else if (age < 10) {
                ageRange = "5-9";
            } else if (age < 15) {
                ageRange = "10-14";
            } else if (age < 20) {
                ageRange = "15-19";
            } else if (age < 25) {
                ageRange = "20-24";
            } else if (age < 30) {
                ageRange = "25-29";
            } else if (age < 35) {
                ageRange = "30-34";
            } else if (age < 40) {
                ageRange = "35-39";
            } else if (age < 45) {
                ageRange = "40-44";
            } else if (age < 50) {
                ageRange = "45-49";
            } else if (age < 55) {
                ageRange = "50-54";
            } else if (age < 60) {
                ageRange = "55-59";
            } else if (age < 65) {
                ageRange = "60-64";
            } else {
                ageRange = "65+";
            }
        } else {
            ageRange = "<1";
        }
        artLinelist.setAgeRange(ageRange);
    }

    private void setVariablesForPreviousQuarterOnArtLineList(Container container, Date cutOff, ArtLinelist artLinelist) {
        Optional<ObsType> patientOutcomePreviousQuarter = helperFunctions.getMaxConceptObsIdWithFormId(
                CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container,
                cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );

        patientOutcomePreviousQuarter.ifPresent(obsType -> {
            artLinelist.setPatientOutcomePreviousQuarter(obsType.getVariableValue());
            artLinelist.setPatientOutcomeDatePreviousQuarter(obsType.getObsDatetime());
        });

        final Long[] daysOfRefilPreviousQuarter = new Long[1];

        Optional<ObsType> lastPickupPreviousQuarter = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        lastPickupPreviousQuarter.ifPresent(obsType -> {
            artLinelist.setLastPickupDatePreviousQuarter(obsType.getObsDatetime());
//            Optional<ObsType> drugDurationPreviousQuarter = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
//            drugDurationPreviousQuarter.ifPresent(obsType1 -> artLinelist.setDrugDurationPreviousQuarter(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().doubleValue()));

            Optional<ObsType> daysOfArv = helperFunctions.getDaysOfArvPreviousQuarter(obsType.getObsId(), 159368, container, cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            daysOfArv.ifPresent(obsType1 -> {
                daysOfRefilPreviousQuarter[0] = obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue();
                artLinelist.setDrugDurationPreviousQuarter(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().doubleValue());
            });
        });

        artLinelist.setArtStatusPreviousQuarter(
                helperFunctions.getCurrentArtStatus(
                        artLinelist.getLastPickupDatePreviousQuarter(),
                        daysOfRefilPreviousQuarter[0],
                        helperFunctions.getCutOffDate(cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        artLinelist.getPatientOutcomePreviousQuarter()
                )
        );
    }

    private void setVariablesForPreviousQuarterOnArtLineList(Container container, Date cutOff, CustomArtLineList artLinelist) {
        Optional<ObsType> patientOutcomePreviousQuarter = helperFunctions.getMaxConceptObsIdWithFormId(
                CLIENT_TRACKING_AND_TERMINATION_FORM, 165470, container,
                cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );

        patientOutcomePreviousQuarter.ifPresent(obsType -> {
            artLinelist.setPatientOutcomePreviousQuarter(obsType.getVariableValue());
            artLinelist.setPatientOutcomeDatePreviousQuarter(obsType.getObsDatetime());
        });

        final Long[] daysOfRefilPreviousQuarter = new Long[1];

        Optional<ObsType> lastPickupPreviousQuarter = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        lastPickupPreviousQuarter.ifPresent(obsType -> {
            artLinelist.setLastPickupDatePreviousQuarter(obsType.getObsDatetime());
//            Optional<ObsType> drugDurationPreviousQuarter = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
//            drugDurationPreviousQuarter.ifPresent(obsType1 -> artLinelist.setDrugDurationPreviousQuarter(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().doubleValue()));

            Optional<ObsType> daysOfArv = helperFunctions.getDaysOfArvPreviousQuarter(obsType.getObsId(), 159368, container, cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            daysOfArv.ifPresent(obsType1 -> {
                daysOfRefilPreviousQuarter[0] = obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue();
                artLinelist.setDrugDurationPreviousQuarter(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().doubleValue());
            });
        });

        artLinelist.setArtStatusPreviousQuarter(
                helperFunctions.getCurrentArtStatus(
                        artLinelist.getLastPickupDatePreviousQuarter(),
                        daysOfRefilPreviousQuarter[0],
                        helperFunctions.getCutOffDate(cutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                        artLinelist.getPatientOutcomePreviousQuarter()
                )
        );
    }

    private void setCurrentViralLoadOnArtLineList(Container container, Date cutOff, ArtLinelist artLinelist) {
        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
        currentViralLoad.ifPresent(obsType -> {
            artLinelist.setCurrentViralLoad(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

            artLinelist.setViralLoadEncounterDate(obsType.getObsDatetime());

            Optional<ObsType> report = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 165414, container, cutOff);
            report.ifPresent(obsType1 -> artLinelist.setViralLoadReportedDate(obsType1.getValueDatetime()));

            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
            if (viralLoadSampleCollectionDate.isPresent()) {
                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                artLinelist.setViralLoadSampleCollectionDate(obsType1.getValueDatetime());
            } else {
                artLinelist.setViralLoadSampleCollectionDate(obsType.getObsDatetime());
            }

            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
            resultDate.ifPresent(obsType1 -> artLinelist.setResultDate(obsType1.getValueDatetime()));

            Optional<ObsType> assayDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166424, container, cutOff);
            assayDate.ifPresent(obsType1 -> artLinelist.setAssayDate(obsType1.getValueDatetime()));

            Optional<ObsType> approvalDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166425, container, cutOff);
            approvalDate.ifPresent(obsType1 -> artLinelist.setApprovalDate(obsType1.getValueDatetime()));

            Optional<ObsType> viralLoadIndication = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),164980, container, cutOff);
            viralLoadIndication.ifPresent(obsType1 -> artLinelist.setViralLoadIndication(obsType1.getVariableValue()));
        });
    }

    private void setCurrentViralLoadOnArtLineList(Container container, Date cutOff, CustomArtLineList artLinelist) {
        Optional<ObsType> currentViralLoad = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM,856, container, cutOff);
        currentViralLoad.ifPresent(obsType -> {
            artLinelist.setCurrentViralLoad(obsType.getValueNumeric() != null ? obsType.getValueNumeric().doubleValue() : null);

            artLinelist.setViralLoadEncounterDate(obsType.getObsDatetime());

            Optional<ObsType> report = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 165414, container, cutOff);
            report.ifPresent(obsType1 -> artLinelist.setViralLoadReportedDate(obsType1.getValueDatetime()));

            Optional<ObsType> viralLoadSampleCollectionDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),159951, container, cutOff);
            if (viralLoadSampleCollectionDate.isPresent()) {
                ObsType obsType1 = viralLoadSampleCollectionDate.get();
                artLinelist.setViralLoadSampleCollectionDate(obsType1.getValueDatetime());
            } else {
                artLinelist.setViralLoadSampleCollectionDate(obsType.getObsDatetime());
            }

            Optional<ObsType> resultDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166423, container, cutOff);
            resultDate.ifPresent(obsType1 -> artLinelist.setResultDate(obsType1.getValueDatetime()));

            Optional<ObsType> assayDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166424, container, cutOff);
            assayDate.ifPresent(obsType1 -> artLinelist.setAssayDate(obsType1.getValueDatetime()));

            Optional<ObsType> approvalDate = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),166425, container, cutOff);
            approvalDate.ifPresent(obsType1 -> artLinelist.setApprovalDate(obsType1.getValueDatetime()));

            Optional<ObsType> viralLoadIndication = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(),164980, container, cutOff);
            viralLoadIndication.ifPresent(obsType1 -> artLinelist.setViralLoadIndication(obsType1.getVariableValue()));
        });
    }

    private void setLastPickupDateVariableOnArtLineList(Container container, Date cutOff, ArtLinelist artLinelist) {
        Optional<ObsType> maxObsByConceptID = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
        maxObsByConceptID.ifPresent(obsType -> {
            container.getMessageData().getEncounters().stream()
                    .filter(encounterType -> encounterType.getEncounterId() == obsType.getEncounterId() &&
                            encounterType.getVoided() == 0)
                    .findFirst()
                    .ifPresent(encounterType -> artLinelist.setLastPickupDate(encounterType.getEncounterDatetime()));

//            artLinelist.setLastPickupDate(obsType.getObsDatetime());

            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
            daysOfArvObs.ifPresent(obsType1 -> artLinelist.setDaysOfArvRefil(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));

            Optional<ObsType> lastQtyOfARVRefil = helperFunctions.getDaysOfArv(obsType.getObsId(),1443, container, cutOff);
            lastQtyOfARVRefil.ifPresent(obsType1 -> artLinelist.setLastQtyOfArvRefill(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));

            Optional<ObsType> dddDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166363, container, cutOff);
            dddDispensingModality.ifPresent(obsType1 -> artLinelist.setDddDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> dispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166148, container, cutOff);
            dispensingModality.ifPresent(obsType1 -> artLinelist.setDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> facilityDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166276, container, cutOff);
            facilityDispensingModality.ifPresent(obsType1 -> artLinelist.setFacilityDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> mmdType = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166278, container, cutOff);
            mmdType.ifPresent(obsType1 -> artLinelist.setMmdType(obsType1.getVariableValue()));
        });
    }

    private void setLastPickupDateVariableOnArtLineList(Container container, Date cutOff, CustomArtLineList artLinelist) {
        Optional<ObsType> maxObsByConceptID = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 162240, container, cutOff);
        maxObsByConceptID.ifPresent(obsType -> {
            container.getMessageData().getEncounters().stream()
                    .filter(encounterType -> encounterType.getEncounterId() == obsType.getEncounterId())
                    .findFirst()
                    .ifPresent(encounterType -> artLinelist.setLastPickupDate(encounterType.getEncounterDatetime()));
//            artLinelist.setLastPickupDate(obsType.getObsDatetime());

            Optional<ObsType> daysOfArvObs = helperFunctions.getDaysOfArv(obsType.getObsId(), 159368, container, cutOff);
            daysOfArvObs.ifPresent(obsType1 -> artLinelist.setDaysOfArvRefil(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));

            Optional<ObsType> lastQtyOfARVRefil = helperFunctions.getDaysOfArv(obsType.getObsId(),1443, container, cutOff);
            lastQtyOfARVRefil.ifPresent(obsType1 -> artLinelist.setLastQtyOfArvRefill(obsType1.getValueNumeric() == null ? null : obsType1.getValueNumeric().longValue()));

            Optional<ObsType> dddDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166363, container, cutOff);
            dddDispensingModality.ifPresent(obsType1 -> artLinelist.setDddDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> dispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166148, container, cutOff);
            dispensingModality.ifPresent(obsType1 -> artLinelist.setDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> facilityDispensingModality = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166276, container, cutOff);
            facilityDispensingModality.ifPresent(obsType1 -> artLinelist.setFacilityDispensingModality(obsType1.getVariableValue()));

            Optional<ObsType> mmdType = helperFunctions.getMaxObsByEncounterIdAndConceptId(obsType.getEncounterId(), 166278, container, cutOff);
            mmdType.ifPresent(obsType1 -> artLinelist.setMmdType(obsType1.getVariableValue()));
        });
    }

    private void setConstantVariablesOnArtLineList(Container container, Date cutOff, String quarter, String uniqueId, ArtLinelist artLinelist) {
        artLinelist.setQuarter(quarter);
        artLinelist.setDatimCode(container.getMessageHeader().getFacilityDatimCode());
        artLinelist.setPatientId((long)container.getMessageData().getDemographics().getPatientId());
        artLinelist.setPatientUuid(container.getId());
        artLinelist.setPatientUniqueId(uniqueId);
        artLinelist.setPatientHospitalNo(helperFunctions.returnIdentifiers(5, container).orElse("").replaceFirst("^0+(?!$)", ""));
        artLinelist.setAncNoIdentifier(helperFunctions.returnIdentifiers(6, container).orElse(null));
        Optional<ObsType> ancNoConceptID = helperFunctions.getMaxConceptObsIdWithFormId(GENERAL_ANTENATAL_CARE_FORM,165567, container, cutOff);
        ancNoConceptID.ifPresent(obsType -> artLinelist.setAncNoConceptId(obsType.getValueText()));
        artLinelist.setHtsNo(helperFunctions.returnIdentifiers(8, container).orElse(null));
        artLinelist.setSex(container.getMessageData().getDemographics().getGender());

        Long ageAtStartOfARTYears = helperFunctions.getAgeAtStartOfARTYears(container, cutOff);
        if (ageAtStartOfARTYears != null) {
            if (ageAtStartOfARTYears >= 1)
                artLinelist.setAgeAtStartOfArtYears(ageAtStartOfARTYears);
            else
                artLinelist.setAgeAtStartOfArtMonths(helperFunctions.getAgeAtStartOfARTMonths(container, cutOff));
        }
        artLinelist.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, cutOff)
                .map(ObsType::getVariableValue).orElse(null));
        artLinelist.setKpType(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, cutOff)
                .map(ObsType::getVariableValue).orElse(null));
        Optional<Date> artStartDateObs = helperFunctions.getStartOfArt(container, cutOff);
        artStartDateObs.ifPresent(artLinelist::setArtStartDate);

        artLinelist.setArtConfirmationDate(helperFunctions.getMaxObsByConceptID(160554, container, cutOff)
                .map(ObsType::getValueDatetime).orElse(null));

        Date dob = container.getMessageData().getDemographics().getBirthdate();
        artLinelist.setDateOfBirth(dob);
    }

    private void setConstantVariablesOnArtLineList(Container container, Date cutOff, String quarter, String uniqueId, CustomArtLineList artLinelist) {
        artLinelist.setQuarter(quarter);
        artLinelist.setDatimCode(container.getMessageHeader().getFacilityDatimCode());
        artLinelist.setPatientId((long)container.getMessageData().getDemographics().getPatientId());
        artLinelist.setPatientUuid(container.getId());
        artLinelist.setPatientUniqueId(uniqueId);
        artLinelist.setPatientHospitalNo(helperFunctions.returnIdentifiers(5, container).orElse("").replaceFirst("^0+(?!$)", ""));
        artLinelist.setAncNoIdentifier(helperFunctions.returnIdentifiers(6, container).orElse(null));
        Optional<ObsType> ancNoConceptID = helperFunctions.getMaxConceptObsIdWithFormId(GENERAL_ANTENATAL_CARE_FORM,165567, container, cutOff);
        ancNoConceptID.ifPresent(obsType -> artLinelist.setAncNoConceptId(obsType.getValueText()));
        artLinelist.setHtsNo(helperFunctions.returnIdentifiers(8, container).orElse(null));
        artLinelist.setSex(container.getMessageData().getDemographics().getGender());

        Long ageAtStartOfARTYears = helperFunctions.getAgeAtStartOfARTYears(container, cutOff);
        if (ageAtStartOfARTYears != null) {
            if (ageAtStartOfARTYears >= 1)
                artLinelist.setAgeAtStartOfArtYears(ageAtStartOfARTYears);
            else
                artLinelist.setAgeAtStartOfArtMonths(helperFunctions.getAgeAtStartOfARTMonths(container, cutOff));
        }
        artLinelist.setCareEntryPoint(helperFunctions.getMaxObsByConceptID(160540, container, cutOff)
                .map(ObsType::getVariableValue).orElse(null));
        artLinelist.setKpType(helperFunctions.getMaxConceptObsIdWithFormId(ENROLLMENT_FORM, 166369, container, cutOff)
                .map(ObsType::getVariableValue).orElse(null));
        Optional<Date> artStartDateObs = helperFunctions.getStartOfArt(container, cutOff);
        artStartDateObs.ifPresent(artLinelist::setArtStartDate);

        artLinelist.setArtConfirmationDate(helperFunctions.getMaxObsByConceptID(160554, container, cutOff)
                .map(ObsType::getValueDatetime).orElse(null));

        Date dob = container.getMessageData().getDemographics().getBirthdate();
        artLinelist.setDateOfBirth(dob);
    }
}
