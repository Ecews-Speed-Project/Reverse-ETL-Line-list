package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.HtsLinelist;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.EncounterType;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.HtsLineListRepository;
import com.etlservice.schedular.repository.mongo_repository.ContainerRepository;
import com.etlservice.schedular.services.HtsLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static com.etlservice.schedular.utils.ConstantsUtils.AGE_TYPE_YEARS;
import static com.etlservice.schedular.utils.ConstantsUtils.CLIENT_INTAKE_FORM;

@Slf4j
@Service
@RequiredArgsConstructor
public class HtsLineListServiceImpl implements HtsLineListService {
    private final HtsLineListRepository htsLineListRepository;
    private final FacilityRepository facilityRepository;
    private final HelperFunctions helperFunctions;
    private final ContainerRepository containerRepository;

    @Override
    public void extractHtsData(List<IdWrapper> mongoContainers) {
        LocalDate currentDate = LocalDate.now();
        Date cutOff;
        System.out.println("Started");
        cutOff = Date.from(currentDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant());

        for (IdWrapper idWrapper: mongoContainers) {
            Container container = containerRepository.findById(idWrapper.getId())
                    .orElse(null);
            if (container != null) {
                Optional<HtsLinelist> htsLinelist = buildHtsLineList(container, cutOff);
                htsLinelist.ifPresent(htsLineListRepository::save);
            }
        }
    }

    private Optional<HtsLinelist> buildHtsLineList(Container container, Date cutOff) {
        LocalDate startDate = LocalDate.of(2023, 1, 1).minusDays(1);
        Date startDateCutOff = Date.from(startDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        boolean hasClientIntakeForm = container.getMessageData().getEncounters().stream()
                .anyMatch(encounterType ->
                        (encounterType.getFormId() == CLIENT_INTAKE_FORM || encounterType.getFormId() == 75) &&
                                encounterType.getEncounterDatetime().after(startDateCutOff) &&
                        encounterType.getVoided() == 0);

        if (hasClientIntakeForm) {
            try {
                HtsLinelist htsLinelist1 = htsLineListRepository
                        .findByPatientIdAndDatimCode(container.getId(), container.getMessageHeader().getFacilityDatimCode())
                        .orElse(null);
                if (htsLinelist1 == null) {
                    HtsLinelist htsLinelist = new HtsLinelist();
                    htsLinelist.setPatientId(container.getId());
                    setHtsLineListConstantVariables(container, htsLinelist);

                    setCurrentAgeAndAgeGroup(container, cutOff, htsLinelist);

                    Optional<EncounterType> optionalEncounterType = helperFunctions.getHtsVisitDate(container);
                    optionalEncounterType.ifPresent(encounterType -> {
                        htsLinelist.setVisitDate(encounterType.getEncounterDatetime());// todo confirm the visit date variable);
                        htsLinelist.setVisitId(encounterType.getVisitId());
                    });

                    if (htsLinelist.getVisitDate() != null)
                        htsLinelist.setAgeAtVisit(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, htsLinelist.getVisitDate()));

                    setHtsSettingType(container, htsLinelist);

                    setReferredFromAndTypeOfSession(container, htsLinelist);

                    setMaritalStatus(container, htsLinelist);

                    setClientIndexPatient(container, htsLinelist);

                    Optional<ObsType> isClientRetestingForResultVerificationObs = helperFunctions.getMaxConceptObs(165976, container);
                    isClientRetestingForResultVerificationObs.ifPresent(obsType -> htsLinelist.setIsClientRetestingForResultVerification(obsType.getVariableValue()));

                    Optional<ObsType> previouslyTestedHIVNegativeObs = helperFunctions.getMaxConceptObs(165799, container);
                    previouslyTestedHIVNegativeObs.ifPresent(obsType -> htsLinelist.setPreviouslyTestedHivNegative(obsType.getVariableValue()));

                    Optional<ObsType> clientPregnantObs = helperFunctions.getMaxConceptObs(1434, container);
                    clientPregnantObs.ifPresent(obsType -> htsLinelist.setClientPregnant(obsType.getValueCoded() == 1 ? "Yes" : "No"));

                    Optional<ObsType> clientInformedAboutHIVTransmissionRoutesObs = helperFunctions.getMaxConceptObs(165801, container);
                    clientInformedAboutHIVTransmissionRoutesObs.ifPresent(obsType ->
                            htsLinelist.setClientInformedAboutHivTransmissionRoutes(obsType.getVariableValue())
                    );

                    Optional<ObsType> clientInformedAboutRiskFactorsForHIVTransmissionObs = helperFunctions.getMaxConceptObs(165802, container);
                    clientInformedAboutRiskFactorsForHIVTransmissionObs.ifPresent(obsType ->
                            htsLinelist.setClientInformedAboutRiskFactorsForHivTransmission(obsType.getVariableValue())
                    );

                    Optional<ObsType> clientInformedOnPreventingHIVTransmissionMethodsObs = helperFunctions.getMaxConceptObs(165804, container);
                    clientInformedOnPreventingHIVTransmissionMethodsObs.ifPresent(obsType ->
                            htsLinelist.setClientInformedOnPreventingHivTransmissionMethods(obsType.getVariableValue())
                    );

                    Optional<ObsType> clientInformedAboutPossibleTestResultsObs = helperFunctions.getMaxConceptObs(165884, container);
                    clientInformedAboutPossibleTestResultsObs.ifPresent(obsType ->
                            htsLinelist.setClientInformedAboutPossibleTestResults(obsType.getVariableValue())
                    );

                    Optional<ObsType> informedConsentForHivTestingGivenObs = helperFunctions.getMaxConceptObs(1710, container);
                    informedConsentForHivTestingGivenObs.ifPresent(obsType -> htsLinelist.setInformedConsentForHivTestingGiven(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> everHadSexualIntercourseObs = helperFunctions.getMaxConceptObs(165800, container);
                    everHadSexualIntercourseObs.ifPresent(obsType -> htsLinelist.setEverHadSexualIntercourse(obsType.getVariableValue()));

                    Optional<ObsType> bloodTransfusionInLast3MonthsObs = helperFunctions.getMaxConceptObs(1063, container);
                    bloodTransfusionInLast3MonthsObs.ifPresent(obsType -> htsLinelist.setBloodTransfusionInLast3months(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> unprotectedSexWithCasualPartnerInLast3MonthsObs = helperFunctions.getMaxConceptObs(159218, container);
                    unprotectedSexWithCasualPartnerInLast3MonthsObs.ifPresent(obsType ->
                            htsLinelist.setUnprotectedSexWithCasualPartnerInLast3months(obsType.getVariableValue())
                    );

                    Optional<ObsType> unprotectedSexWithRegularPartnerInTheLast3MonthsObs = helperFunctions.getMaxConceptObs(165803, container);
                    unprotectedSexWithRegularPartnerInTheLast3MonthsObs.ifPresent(obsType ->
                            htsLinelist.setUnprotectedSexWithRegularPartnerInTheLast3months(obsType.getVariableValue())
                    );

                    Optional<ObsType> STIinLast3MonthsObs = helperFunctions.getMaxConceptObs(164809, container);
                    STIinLast3MonthsObs.ifPresent(obsType -> htsLinelist.setStiInLast3months(obsType.getVariableValue()));

                    Optional<ObsType> moreThan1SexPartnerDuringLast3MonthsObs = helperFunctions.getMaxConceptObs(165806, container);
                    moreThan1SexPartnerDuringLast3MonthsObs.ifPresent(obsType ->
                            htsLinelist.setMoreThan1sexPartnerDuringLast3months(obsType.getVariableValue())
                    );

                    Optional<ObsType> currentCoughObs = helperFunctions.getMaxConceptObs(143264, container);
                    currentCoughObs.ifPresent(obsType -> htsLinelist.setCurrentCough(obsType.getVariableValue()));

                    Optional<ObsType> weightLossObs = helperFunctions.getMaxConceptObs(832, container);
                    weightLossObs.ifPresent(obsType -> htsLinelist.setWeightLoss(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> feverObs = helperFunctions.getMaxConceptObs(140238, container);
                    feverObs.ifPresent(obsType -> htsLinelist.setFever(obsType.getVariableValue()));

                    Optional<ObsType> nightSweatsObs = helperFunctions.getMaxConceptObs(133027, container);
                    nightSweatsObs.ifPresent(obsType -> htsLinelist.setNightSweats(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> contactWithTBPatientObs = helperFunctions.getMaxConceptObs(124068, container);
                    contactWithTBPatientObs.ifPresent(obsType -> htsLinelist.setContactWithTbPatient(obsType.getVariableValue()));

                    List<Integer> tbScreeningConceptIds = Arrays.asList(832, 143264, 140238, 133027, 124068);
                    List<Integer> tbScreeningValueCodes = Arrays.asList(1, 1065);
                    htsLinelist.setTbScreeningScore(helperFunctions.getScreeningScore(container, tbScreeningConceptIds, tbScreeningValueCodes));

                    Optional<ObsType> complaintsOfVaginalDischargeOrBurningWhenUrinatingObs = helperFunctions.getMaxConceptObs(165809, container);
                    complaintsOfVaginalDischargeOrBurningWhenUrinatingObs.ifPresent(obsType ->
                            htsLinelist.setComplaintsOfVaginalDischargeOrBurningWhenUrinating(obsType.getVariableValue())
                    );

                    Optional<ObsType> complaintsOfLowerAbdominalPainsWithOrWithoutVaginalDischargeObs = helperFunctions.getMaxConceptObs(165810, container);
                    complaintsOfLowerAbdominalPainsWithOrWithoutVaginalDischargeObs.ifPresent(obsType ->
                            htsLinelist.setComplaintsOfLowerAbdominalPainsWithOrWithoutVaginalDischarge(obsType.getVariableValue())
                    );

                    Optional<ObsType> complaintsOfGenitalSoreOrSwollenInguinalLymphNodesWithOrWithoutPainsObs = helperFunctions.getMaxConceptObs(165813, container);
                    complaintsOfGenitalSoreOrSwollenInguinalLymphNodesWithOrWithoutPainsObs.ifPresent(obsType ->
                            htsLinelist.setComplaintsOfGenitalSoreOrSwollenInguinalLymphNodesWithOrWithoutPains(obsType.getVariableValue())
                    );

                    List<Integer> STIScreeningConceptIds = Arrays.asList(165809, 165810, 165813);
                    List<Integer> STIScreeningValueCodes = Collections.singletonList(1065);
                    htsLinelist.setStiScreeningScore(helperFunctions.getScreeningScore(container, STIScreeningConceptIds, STIScreeningValueCodes));

                    List<Integer> riskAssessmentConceptIds = Arrays.asList(165800, 1063, 159218, 165803, 164809, 165806);
                    List<Integer> riskAssessmentValueCodes = Arrays.asList(1, 1065);
                    htsLinelist.setRiskAssessmentScore(helperFunctions.getScreeningScore(container, riskAssessmentConceptIds, riskAssessmentValueCodes));

                    Optional<ObsType> screeningTestResultObs = helperFunctions.getMaxConceptObs(165840, container);
                    screeningTestResultObs.ifPresent(obsType -> htsLinelist.setScreeningTestResult(obsType.getVariableValue()));

                    Optional<ObsType> screeningTestDateObs = helperFunctions.getMaxConceptObs(165844, container);
                    screeningTestDateObs.ifPresent(obsType -> htsLinelist.setScreeningTestDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> confirmatoryTestResultObs = helperFunctions.getMaxConceptObs(165841, container);
                    confirmatoryTestResultObs.ifPresent(obsType -> htsLinelist.setConfirmatoryTestResult(obsType.getVariableValue()));

                    Optional<ObsType> confirmatoryTestDateObs = helperFunctions.getMaxConceptObs(165845, container);
                    confirmatoryTestDateObs.ifPresent(obsType -> htsLinelist.setConfirmatoryTestDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> tieBreakerObs = helperFunctions.getMaxConceptObs(165842, container);
                    tieBreakerObs.ifPresent(obsType -> htsLinelist.setTieBreaker(obsType.getVariableValue()));

                    Optional<ObsType> tieBreakerDateObs = helperFunctions.getMaxConceptObs(165846, container);
                    tieBreakerDateObs.ifPresent(obsType -> htsLinelist.setTieBreakerDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> finalResultObs = helperFunctions.getMaxConceptObs(165843, container);
                    finalResultObs.ifPresent(obsType -> htsLinelist.setFinalResult(obsType.getVariableValue()));

                    Optional<ObsType> optOutRtriObs = helperFunctions.getMaxConceptObs(165805, container);
                    optOutRtriObs.ifPresent(obsType -> htsLinelist.setOptOutOfRtri(
                            obsType.getValueCoded() == 1066 ? "No" : "Yes"
                    ));

                    Optional<ObsType> recencyTestNameObs = helperFunctions.getMaxConceptObs(166216, container);
                    recencyTestNameObs.ifPresent(obsType -> htsLinelist.setRecencyTestName(obsType.getVariableValue()));

                    Optional<ObsType> recencyTestDateObs = helperFunctions.getMaxConceptObs(165850, container);
                    recencyTestDateObs.ifPresent(obsType -> htsLinelist.setRecencyTestDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> controlLineObs = helperFunctions.getMaxConceptObs(166212, container);
                    controlLineObs.ifPresent(obsType -> htsLinelist.setControlLine(obsType.getVariableValue()));

                    Optional<ObsType> verificationLineObs = helperFunctions.getMaxConceptObs(166243, container);
                    verificationLineObs.ifPresent(obsType -> htsLinelist.setVerificationLine(obsType.getVariableValue()));

                    Optional<ObsType> longTermLineObs = helperFunctions.getMaxConceptObs(166211, container);
                    longTermLineObs.ifPresent(obsType -> htsLinelist.setLongTermLine(obsType.getVariableValue()));

                    Optional<ObsType> recencyInterpretationObs = helperFunctions.getMaxConceptObs(166213, container);
                    recencyInterpretationObs.ifPresent(obsType -> htsLinelist.setRecencyInterpretation(obsType.getVariableValue()));

                    Optional<ObsType> hasViralLoadBeenRequestedObs = helperFunctions.getMaxConceptObs(166244, container);
                    hasViralLoadBeenRequestedObs.ifPresent(obsType -> htsLinelist.setHasViralLoadBeenRequested(obsType.getVariableValue()));

                    Optional<ObsType> dateSampleCollectedObs = helperFunctions.getMaxConceptObs(159951, container);
                    dateSampleCollectedObs.ifPresent(obsType -> htsLinelist.setDateSampleCollected(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> dateSampleSentObs = helperFunctions.getMaxConceptObs(165988, container);
                    dateSampleSentObs.ifPresent(obsType -> htsLinelist.setDateSampleSent(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));

                    Optional<ObsType> viralLoadResultObs = helperFunctions.getMaxConceptObsIdWithFormId(CLIENT_INTAKE_FORM, 856, container, cutOff);
                    viralLoadResultObs.ifPresent(obsType -> {
                        htsLinelist.setViralLoadResult(obsType.getValueNumeric().doubleValue());
                        htsLinelist.setDateOfViralLoadResult(obsType.getObsDatetime());
                    });

                    Optional<ObsType> viralLoadResultClassificationObs = helperFunctions.getMaxConceptObs(166241, container);
                    viralLoadResultClassificationObs.ifPresent(obsType -> htsLinelist.setViralLoadResultClassification(obsType.getVariableValue()));

                    Optional<ObsType> pcrLabObs = helperFunctions.getMaxConceptObs(166233, container);
                    pcrLabObs.ifPresent(obsType -> htsLinelist.setPcrLab(obsType.getVariableValue()));

                    Optional<ObsType> finalRecencyResultObs = helperFunctions.getMaxConceptObs(166214, container);
                    finalRecencyResultObs.ifPresent(obsType -> htsLinelist.setFinalRecencyResult(obsType.getVariableValue()));

                    Optional<ObsType> haveYouBeenTestedForHivBeforeWithinThisYearObs = helperFunctions.getMaxConceptObs(165881, container);
                    haveYouBeenTestedForHivBeforeWithinThisYearObs.ifPresent(obsType ->
                            htsLinelist.setHaveYouBeenTestedForHivBeforeWithinThisYear(obsType.getVariableValue())
                    );

                    Optional<ObsType> HIVRequestAndResultFormSignedByTesterObs = helperFunctions.getMaxConceptObs(165818, container);
                    HIVRequestAndResultFormSignedByTesterObs.ifPresent(obsType ->
                            htsLinelist.setHivRequestAndResultFormSignedByTester(obsType.getVariableValue())
                    );

                    Optional<ObsType> clientReceivedHIVTestResultObs = helperFunctions.getMaxConceptObs(164848, container);
                    clientReceivedHIVTestResultObs.ifPresent(obsType -> htsLinelist.setClientReceivedHivTestResult(obsType.getVariableValue()));

                    Optional<ObsType> postTestCounselingDoneObs = helperFunctions.getMaxConceptObs(159382, container);
                    postTestCounselingDoneObs.ifPresent(obsType -> htsLinelist.setPostTestCounselingDone(obsType.getVariableValue()));

                    Optional<ObsType> clientUseFPMethodsOtherThanCondomObs = helperFunctions.getMaxConceptObs(165883, container);
                    clientUseFPMethodsOtherThanCondomObs.ifPresent(obsType -> htsLinelist.setClientUseFpMethodsOtherThanCondom(obsType.getVariableValue()));

                    Optional<ObsType> clientUseCondomsAsFPMethodObs = helperFunctions.getMaxConceptObs(5571, container);
                    clientUseCondomsAsFPMethodObs.ifPresent(obsType -> htsLinelist.setClientUseCondomsAsFpMethod(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> syphilisTestResultObs = helperFunctions.getMaxConceptObs(299, container);
                    syphilisTestResultObs.ifPresent(obsType -> htsLinelist.setSyphilisTestResult(obsType.getVariableValue()));

                    Optional<ObsType> hepatitisBVirusTestResultObs = helperFunctions.getMaxConceptObs(159430, container);
                    hepatitisBVirusTestResultObs.ifPresent(obsType -> htsLinelist.setHepatitisBVirusTestResult(obsType.getVariableValue()));

                    Optional<ObsType> hepatitisCVirusTestResultObs = helperFunctions.getMaxConceptObs(161471, container);
                    hepatitisCVirusTestResultObs.ifPresent(obsType -> htsLinelist.setHepatitisCVirusTestResult(obsType.getVariableValue()));

                    Optional<ObsType> clientReferredToOtherServicesObs = helperFunctions.getMaxConceptObs(1648, container);
                    clientReferredToOtherServicesObs.ifPresent(obsType -> htsLinelist.setClientReferredToOtherServices(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> riskReductionPlanDevelopedObs = helperFunctions.getMaxConceptObs(165820, container);
                    riskReductionPlanDevelopedObs.ifPresent(obsType -> htsLinelist.setRiskReductionPlanDeveloped(obsType.getVariableValue()));

                    Optional<ObsType> postTestDisclosurePlanDevelopedObs = helperFunctions.getMaxConceptObs(165821, container);
                    postTestDisclosurePlanDevelopedObs.ifPresent(obsType -> htsLinelist.setPostTestDisclosurePlanDeveloped(obsType.getVariableValue()));

                    Optional<ObsType> willBringPartnerForHivTestingObs = helperFunctions.getMaxConceptObs(165822, container);
                    willBringPartnerForHivTestingObs.ifPresent(obsType -> htsLinelist.setWillBringPartnerForHivTesting(obsType.getVariableValue()));

                    Optional<ObsType> willBringOwnChildrenLessThan5YearsForHIVTestingObs = helperFunctions.getMaxConceptObs(165823, container);
                    willBringOwnChildrenLessThan5YearsForHIVTestingObs.ifPresent(obsType ->
                            htsLinelist.setWillBringOwnChildrenLessThan5yearsForHivTesting(obsType.getVariableValue())
                    );

                    Optional<ObsType> providedWithInformationOnFPAndDualContraceptionObs = helperFunctions.getMaxConceptObs(1382, container);
                    providedWithInformationOnFPAndDualContraceptionObs.ifPresent(obsType -> {
                        htsLinelist.setProvidedWithInformationOnFpAndDualContraception(obsType.getVariableValue());
                        htsLinelist.setCorrectCondomUseDemonstrated(obsType.getVariableValue());
                    });

                    Optional<ObsType> condomsProvidedToClientObs = helperFunctions.getMaxConceptObs(159777, container);
                    condomsProvidedToClientObs.ifPresent(obsType -> htsLinelist.setCondomsProvidedToClient(obsType.getVariableValue()));

                    Optional<ObsType> keyPopulationObs = helperFunctions.getMaxConceptObs(166284, container);
                    keyPopulationObs.ifPresent(obsType -> htsLinelist.setKeyPopulation(
                            obsType.getValueCoded() == 1 ? "Yes" : "No"
                    ));

                    Optional<ObsType> kpTypeObs = helperFunctions.getMaxConceptObs(166369, container);
                    kpTypeObs.ifPresent(obsType -> htsLinelist.setKpType(obsType.getVariableValue()));

                    Optional<ObsType> additionalCommentsObs = helperFunctions.getMaxConceptObs(165045, container);
                    additionalCommentsObs.ifPresent(obsType -> htsLinelist.setAdditionalComments(obsType.getValueText()));

                    Optional<ObsType> signatureDateObs = helperFunctions.getMaxConceptObs(166242, container);
                    signatureDateObs.ifPresent(obsType -> htsLinelist.setSignatureDate(obsType.getValueDatetime() != null ? obsType.getValueDatetime() : null));
                    return Optional.of(htsLinelist);
                } else {
                    log.info("Patient with id " + container.getId() + " already exists in the database");
                    return Optional.empty();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else
            return Optional.empty();
    }

    private void setClientIndexPatient(Container container, HtsLinelist htsLinelist) {
        Optional<ObsType> isClientIdentifiedFromAnIndexClientObs = helperFunctions.getMaxConceptObs(165794, container);
        isClientIdentifiedFromAnIndexClientObs.ifPresent(obsType -> htsLinelist.setIsClientIdentifiedFromAnIndexClient(obsType.getVariableValue()));

        Optional<ObsType> indexTypeObs = helperFunctions.getMaxConceptObs(165798, container);
        indexTypeObs.ifPresent(obsType -> htsLinelist.setIndexType(obsType.getVariableValue()));

        Optional<ObsType> indexClientIdObs = helperFunctions.getMaxConceptObs(165859, container);
        indexClientIdObs.ifPresent(obsType -> htsLinelist.setIndexClientId(obsType.getValueText()));
    }

    private void setMaritalStatus(Container container, HtsLinelist htsLinelist) {
        Optional<ObsType> maritalStatusObs = helperFunctions.getMaxConceptObs(1054, container);
        maritalStatusObs.ifPresent(obsType -> htsLinelist.setMaritalStatus(obsType.getVariableValue()));

        Optional<ObsType> noOfOwnChildrenLessThan5Obs = helperFunctions.getMaxConceptObs(160312, container);
        noOfOwnChildrenLessThan5Obs.ifPresent(obsType -> htsLinelist.setNoOfOwnChildrenLessThan5(obsType.getValueNumeric().intValue()));

        Optional<ObsType> noOfWivesObs = helperFunctions.getMaxConceptObs(5557, container);
        noOfWivesObs.ifPresent(obsType -> htsLinelist.setNoOfWives(obsType.getValueNumeric().intValue()));
    }

    private void setReferredFromAndTypeOfSession(Container container, HtsLinelist htsLinelist) {
        Optional<ObsType> firstTimeVisitObs = helperFunctions.getMaxConceptObs(165790, container);
        firstTimeVisitObs.ifPresent(obsType -> htsLinelist.setFirstTimeVisit(obsType.getVariableValue()));

        Optional<ObsType> typeOfSessionObs = helperFunctions.getMaxConceptObs(165793, container);
        typeOfSessionObs.ifPresent(obsType -> htsLinelist.setTypeOfSession(obsType.getVariableValue()));

        Optional<ObsType> referredFromObs = helperFunctions.getMaxConceptObs(165480, container);
        referredFromObs.ifPresent(obsType -> htsLinelist.setReferredFrom(obsType.getVariableValue()));
    }

    private void setHtsSettingType(Container container, HtsLinelist htsLinelist) {
        Optional<ObsType> settingObsType = helperFunctions.getMaxConceptObs(165839, container);
        settingObsType.ifPresent(obsType -> htsLinelist.setSetting(obsType.getVariableValue()));

        Optional<ObsType> kindOfHtsObsType = helperFunctions.getMaxConceptObs(166136, container);
        kindOfHtsObsType.ifPresent(obsType -> htsLinelist.setKindOfHts(obsType.getVariableValue()));

        Optional<ObsType> settingOthersSpecifyObsType = helperFunctions.getMaxConceptObs(165966, container);
        settingOthersSpecifyObsType.ifPresent(obsType -> htsLinelist.setSettingOthersSpecify(obsType.getValueText()));
    }

    private void setHtsLineListConstantVariables(Container container, HtsLinelist htsLinelist) {
        String facilityDatimCode = container.getMessageHeader().getFacilityDatimCode();
        Facility facility = facilityRepository.findFacilityByDatimCode(facilityDatimCode);
        htsLinelist.setIpName(facility.getPartner());
        htsLinelist.setFacilityState(facility.getState().getStateName());
        htsLinelist.setFacilityLga(facility.getLga().getLga());
        htsLinelist.setFacilityName(facility.getFacilityName());
        htsLinelist.setDatimCode(facilityDatimCode);
        htsLinelist.setClientState(container.getMessageData().getDemographics().getStateProvince());
        htsLinelist.setClientLga(container.getMessageData().getDemographics().getCityVillage());
        htsLinelist.setHtsClientCode(helperFunctions.returnIdentifiers(8, container).orElse(null));
        htsLinelist.setPepfarId(helperFunctions.returnIdentifiers(4, container).orElse(null));
        htsLinelist.setHospId(helperFunctions.returnIdentifiers(5, container).orElse(null));
        htsLinelist.setRecencyId(helperFunctions.returnIdentifiers(10, container).orElse(null));
        htsLinelist.setSex(container.getMessageData().getDemographics().getGender());
        htsLinelist.setDateOfBirth(container.getMessageData().getDemographics().getBirthdate());
    }

    private void setCurrentAgeAndAgeGroup(Container container, Date cutOff, HtsLinelist htsLinelist) {
        htsLinelist.setCurrentAge(helperFunctions.getCurrentAge(container, AGE_TYPE_YEARS, cutOff));
        int currentAge = htsLinelist.getCurrentAge();
        String ageGroup;
        if (currentAge < 1)
            ageGroup = "<1";
        else if (currentAge < 5)
            ageGroup = "1 - 4";
        else if (currentAge < 10)
            ageGroup = "5 - 9";
        else if (currentAge < 15)
            ageGroup = "10 - 14";
        else if (currentAge < 20)
            ageGroup = "15 - 19";
        else if (currentAge < 25)
            ageGroup = "20 - 24";
        else if (currentAge < 30)
            ageGroup = "25 - 29";
        else if (currentAge < 35)
            ageGroup = "30 - 34";
        else if (currentAge < 40)
            ageGroup = "35 - 39";
        else if (currentAge < 45)
            ageGroup = "40 - 44";
        else if (currentAge < 50)
            ageGroup = "45 - 49";
        else if (currentAge < 55)
            ageGroup = "50 - 54";
        else if (currentAge < 60)
            ageGroup = "55 - 59";
        else if (currentAge < 65)
            ageGroup = "60 - 64";
        else
            ageGroup = "65+";
        htsLinelist.setAgeGroup(ageGroup);
    }
}
