/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.entities.linelists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author MORRISON.I
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hts_linelist")
@NamedQuery(name = "HtsLinelist.findAll", query = "SELECT h FROM HtsLinelist h")
public class HtsLinelist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ip_name")
    private String ipName;
    @Column(name = "facility_state")
    private String facilityState;
    @Column(name = "facility_lga")
    private String facilityLga;
    @Column(name = "facility_name")
    private String facilityName;
    @Column(name = "datim_code")
    private String datimCode;
    @Column(name = "client_state")
    private String clientState;
    @Column(name = "client_lga")
    private String clientLga;
    @Column(name = "hts_client_code")
    private String htsClientCode;
    @Column(name = "patient_id")
    private String patientId;
    @Column(name = "pepfar_id")
    private String pepfarId;
    @Column(name = "hosp_id")
    private String hospId;
    @Column(name = "recency_id")
    private String recencyId;
    @Column(name = "sex")
    private String sex;
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    @Column(name = "current_age")
    private Integer currentAge;
    @Column(name = "age_group")
    private String ageGroup;
    @Column(name = "age_at_visit")
    private Integer ageAtVisit;
    @Column(name = "visit_date")
    private Date visitDate;
    @Column(name = "setting")
    private String setting;
    @Column(name = "visit_id")
    private Integer visitId;
    @Column(name = "kind_of_hts")
    private String kindOfHts;
    @Column(name = "setting_others_specify")
    private String settingOthersSpecify;
    @Column(name = "first_time_visit")
    private String firstTimeVisit;
    @Column(name = "type_of_session")
    private String typeOfSession;
    @Column(name = "referred_from")
    private String referredFrom;
    @Column(name = "marital_status")
    private String maritalStatus;
    @Column(name = "no_of_own_children_less_than5")
    private int noOfOwnChildrenLessThan5;
    @Column(name = "no_of_wives")
    private int noOfWives;
    @Column(name = "is_client_identified_from_an_index_client")
    private String isClientIdentifiedFromAnIndexClient;
    @Column(name = "index_type")
    private String indexType;
    @Column(name = "index_client_id")
    private String indexClientId;
    @Column(name = "is_client_retesting_for_result_verification")
    private String isClientRetestingForResultVerification;
    @Column(name = "previously_tested_hiv_negative")
    private String previouslyTestedHivNegative;
    @Column(name = "client_pregnant")
    private String clientPregnant;
    @Column(name = "client_informed_about_hiv_transmission_routes")
    private String clientInformedAboutHivTransmissionRoutes;
    @Column(name = "client_informed_about_risk_factors_for_hiv_transmission")
    private String clientInformedAboutRiskFactorsForHivTransmission;
    @Column(name = "client_informed_on_preventing_hiv_transmission_methods")
    private String clientInformedOnPreventingHivTransmissionMethods;
    @Column(name = "client_informed_about_possible_test_results")
    private String clientInformedAboutPossibleTestResults;
    @Column(name = "informed_consent_for_hiv_testing_given")
    private String informedConsentForHivTestingGiven;
    @Column(name = "ever_had_sexual_intercourse")
    private String everHadSexualIntercourse;
    @Column(name = "blood_transfusion_in_last_3months")
    private String bloodTransfusionInLast3months;
    @Column(name = "unprotected_sex_with_casual_partner_in_last_3months")
    private String unprotectedSexWithCasualPartnerInLast3months;
    @Column(name = "unprotected_sex_with_regular_partner_in_the_last_3months")
    private String unprotectedSexWithRegularPartnerInTheLast3months;
    @Column(name = "sti_in_last_3months")
    private String stiInLast3months;
    @Column(name = "more_than_1sex_partner_during_last_3months")
    private String moreThan1sexPartnerDuringLast3months;
    @Column(name = "current_cough")
    private String currentCough;
    @Column(name = "weight_loss")
    private String weightLoss;
    @Column(name = "fever")
    private String fever;
    @Column(name = "night_sweats")
    private String nightSweats;
    @Column(name = "contact_with_tb_patient")
    private String contactWithTbPatient;
    @Column(name = "tb_screening_score")
    private long tbScreeningScore;
    @Column(name = "complaints_of_vaginal_discharge_or_burning_when_urinating")
    private String complaintsOfVaginalDischargeOrBurningWhenUrinating;
    @Column(name = "complaints_of_lower_abdominal_pains_with_or_without_vaginal_dis")
    private String complaintsOfLowerAbdominalPainsWithOrWithoutVaginalDischarge;
    @Column(name = "complaints_of_genital_sore_or_swollen_inguinal_lymph_nodes")
    private String complaintsOfGenitalSoreOrSwollenInguinalLymphNodesWithOrWithoutPains;
    @Column(name = "sti_screening_score")
    private long stiScreeningScore;
    @Column(name = "risk_assessment_score")
    private long riskAssessmentScore;
    @Column(name = "screening_test_result")
    private String screeningTestResult;
    @Column(name = "screening_test_date")
    private Date screeningTestDate;
    @Column(name = "confirmatory_test_result")
    private String confirmatoryTestResult;
    @Column(name = "confirmatory_test_date")
    private Date confirmatoryTestDate;
    @Column(name = "tie_breaker")
    private String tieBreaker;
    @Column(name = "tie_breaker_date")
    private Date tieBreakerDate;
    @Column(name = "final_result")
    private String finalResult;
    @Column(name = "opt_out_of_rtri")
    private String optOutOfRtri;
    @Column(name = "recency_test_name")
    private String recencyTestName;
    @Column(name = "recency_test_date")
    private Date recencyTestDate;
    @Column(name = "control_line")
    private String controlLine;
    @Column(name = "verification_line")
    private String verificationLine;
    @Column(name = "long_term_line")
    private String longTermLine;
    @Column(name = "recency_interpretation")
    private String recencyInterpretation;
    @Column(name = "has_viral_load_been_requested")
    private String hasViralLoadBeenRequested;
    @Column(name = "date_sample_collected")
    private Date dateSampleCollected;
    @Column(name = "date_sample_sent")
    private Date dateSampleSent;
    @Column(name = "viral_load_result")
    private Double viralLoadResult;
    @Column(name = "viral_load_result_classification")
    private String viralLoadResultClassification;
    @Column(name = "date_of_viral_load_result")
    private Date dateOfViralLoadResult;
    @Column(name = "pcr_lab")
    private String pcrLab;
    @Column(name = "final_recency_result")
    private String finalRecencyResult;
    @Column(name = "have_you_been_tested_for_hiv_before_within_this_year")
    private String haveYouBeenTestedForHivBeforeWithinThisYear;
    @Column(name = "hiv_request_and_result_form_signed_by_tester")
    private String hivRequestAndResultFormSignedByTester;
    @Column(name = "client_received_hiv_test_result")
    private String clientReceivedHivTestResult;
    @Column(name = "post_test_counseling_done")
    private String postTestCounselingDone;
    @Column(name = "client_use_fp_methods_other_than_condom")
    private String clientUseFpMethodsOtherThanCondom;
    @Column(name = "client_use_condoms_as_fp_method")
    private String clientUseCondomsAsFpMethod;
    @Column(name = "syphilis_test_result")
    private String syphilisTestResult;
    @Column(name = "hepatitis_b_virus_test_result")
    private String hepatitisBVirusTestResult;
    @Column(name = "hepatitis_c_virus_test_result")
    private String hepatitisCVirusTestResult;
    @Column(name = "client_referred_to_other_services")
    private String clientReferredToOtherServices;
    @Column(name = "risk_reduction_plan_developed")
    private String riskReductionPlanDeveloped;
    @Column(name = "post_test_disclosure_plan_developed")
    private String postTestDisclosurePlanDeveloped;
    @Column(name = "will_bring_partner_for_hiv_testing")
    private String willBringPartnerForHivTesting;
    @Column(name = "will_bring_own_children_less_than_5years_for_hiv_testing")
    private String willBringOwnChildrenLessThan5yearsForHivTesting;
    @Column(name = "provided_with_information_on_fp_and_dual_contraception")
    private String providedWithInformationOnFpAndDualContraception;
    @Column(name = "correct_condom_use_demonstrated")
    private String correctCondomUseDemonstrated;
    @Column(name = "condoms_provided_to_client")
    private String condomsProvidedToClient;
    @Column(name = "key_population")
    private String keyPopulation;
    @Column(name = "kp_type")
    private String kpType;
    @Column(name = "additional_comments")
    private String additionalComments;
    @Column(name = "signature_date")
    private Date signatureDate;
    
}
