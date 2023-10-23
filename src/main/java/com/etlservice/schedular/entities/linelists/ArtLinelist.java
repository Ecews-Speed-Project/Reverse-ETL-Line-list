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
@Table(name = "art_linelist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"patient_uuid", "datim_code", "quarter"})
})
@NamedQuery(name = "ArtLinelist.findAll", query = "SELECT a FROM ArtLinelist a")
public class ArtLinelist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "state")
    private String state;
    @Column(name = "lga")
    private String lgaName;
    @Column(name = "datim_code")
    private String datimCode;
    @Column(name = "facility_name")
    private String facilityName;
    @Column(name = "patient_unique_id")
    private String patientUniqueId;
    @Column(name = "patient_hospital_no")
    private String patientHospitalNo;
    @Column(name = "anc_no_identifier")
    private String ancNoIdentifier;
    @Column(name = "anc_no_concept_id")
    private String ancNoConceptId;
    @Column(name = "sex")
    private String sex;
    @Column(name = "patient_id")
    private Long patientId;
    @Column(name = "patient_uuid")
    private String patientUuid;
    @Column(name = "age_at_start_of_art_years")
    private Long ageAtStartOfArtYears;
    @Column(name = "age_at_start_of_art_months")
    private Long ageAtStartOfArtMonths;
    @Column(name = "care_entry_point")
    private String careEntryPoint;
    @Column(name = "kp_type")
    private String kpType;
    @Column(name = "months_on_art")
    private Long monthsOnArt;
    @Column(name = "date_transferred_in")
    private Date dateTransferredIn;
    @Column(name = "transfer_in_status")
    private String transferInStatus;
    @Column(name = "art_start_date")
    private Date artStartDate;
    @Column(name = "last_pickup_date")
    private Date lastPickupDate;
    @Column(name = "last_visit_date")
    private Date lastVisitDate;
    @Column(name = "days_of_arv_refil")
    private Long daysOfArvRefil;
    @Column(name = "pill_balance")
    private Long pillBalance;
    @Column(name = "initial_regimen_line")
    private String initialRegimenLine;
    @Column(name = "initial_regimen")
    private String initialRegimen;
    @Column(name = "initial_cd4_count")
    private Long initialCd4Count;
    @Column(name = "initial_cd4_count_date")
    private Date initialCd4CountDate;
    @Column(name = "current_cd4_count")
    private Long currentCd4Count;
    @Column(name = "current_cd4_count_date")
    private Date currentCd4CountDate;
    @Column(name = "last_eac_date")
    private Date lastEacDate;
    @Column(name = "current_regimen_line")
    private String currentRegimenLine;
    @Column(name = "current_regimen")
    private String currentRegimen;
    @Column(name = "pregnancy_status")
    private String pregnancyStatus;
    @Column(name = "pregnancy_status_date")
    private Date pregnancyStatusDate;
    @Column(name = "edd")
    private Date edd;
    @Column(name = "last_delivery_date")
    private Date lastDeliveryDate;
    @Column(name = "lmp")
    private Date lmp;
    @Column(name = "gestation_age_weeks")
    private Long gestationAgeWeeks;
    @Column(name = "current_viral_load")
    private Double currentViralLoad;
    @Column(name = "viral_load_encounter_date")
    private Date viralLoadEncounterDate;
    @Column(name = "viral_load_sample_collection_date")
    private Date viralLoadSampleCollectionDate;
    @Column(name = "viral_load_reported_date")
    private Date viralLoadReportedDate;
    @Column(name = "result_date")
    private Date resultDate;
    @Column(name = "assay_date")
    private Date assayDate;
    @Column(name = "approval_date")
    private Date approvalDate;
    @Column(name = "viral_load_indication")
    private String viralLoadIndication;
    @Column(name = "patient_outcome")
    private String patientOutcome;
    @Column(name = "patient_outcome_date")
    private Date patientOutcomeDate;
    @Column(name = "current_art_status")
    private String currentArtStatus;
    @Column(name = "dispensing_modality")
    private String dispensingModality;
    @Column(name = "facility_dispensing_modality")
    private String facilityDispensingModality;
    @Column(name = "ddd_dispensing_modality")
    private String dddDispensingModality;
    @Column(name = "mmd_type")
    private String mmdType;
    @Column(name = "date_returned_to_care")
    private Date dateReturnedToCare;
    @Column(name = "date_of_termination")
    private Date dateOfTermination;
    @Column(name = "pharmacy_next_appointment")
    private Date pharmacyNextAppointment;
    @Column(name = "clinical_next_appointment")
    private Date clinicalNextAppointment;
    @Column(name = "current_age_yrs")
    private Integer currentAgeYrs;
    @Column(name = "current_age_months")
    private Integer currentAgeMonths;
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    @Column(name = "mark_as_deceased")
    private String markAsDeceased;
    @Column(name = "mark_as_deceased_death_date")
    private Date markAsDeceasedDeathDate;
    @Column(name = "registration_phone_no")
    private String registrationPhoneNo;
    @Column(name = "next_of_kin_phone_no")
    private String nextOfKinPhoneNo;
    @Column(name = "treatment_supporter_phone_no")
    private String treatmentSupporterPhoneNo;
    @Column(name = "biometric_captured")
    private String biometricCaptured;
    @Column(name = "biometric_capture_date")
    private Date biometricCaptureDate;
    @Column(name = "valid_capture")
    private String validCapture;
    @Column(name = "current_weight")
    private Double currentWeight;
    @Column(name = "current_weight_date")
    private Date currentWeightDate;
    @Column(name = "tb_status")
    private String tbStatus;
    @Column(name = "tb_status_date")
    private Date tbStatusDate;
    @Column(name = "baseline_inh_start_date")
    private Date baselineInhStartDate;
    @Column(name = "baseline_inh_stop_date")
    private Date baselineInhStopDate;
    @Column(name = "current_inh_start_date")
    private Date currentInhStartDate;
    @Column(name = "current_inh_outcome")
    private String currentInhOutcome;
    @Column(name = "current_inh_outcome_date")
    private Date currentInhOutcomeDate;
    @Column(name = "last_inh_dispensed_date")
    private Date lastInhDispensedDate;
    @Column(name = "baseline_tb_treatment_start_date")
    private Date baselineTbTreatmentStartDate;
    @Column(name = "baseline_tb_treatment_stop_date")
    private Date baselineTbTreatmentStopDate;
    @Column(name = "last_viral_load_sample_collection_form_date")
    private Date lastViralLoadSampleCollectionFormDate;
    @Column(name = "last_sample_taken_date")
    private Date lastSampleTakenDate;
    @Column(name = "otz_enrollment_date")
    private Date otzEnrollmentDate;
    @Column(name = "otz_outcome_date")
    private Date otzOutcomeDate;
    @Column(name = "enrollment_date")
    private Date enrollmentDate;
    @Column(name = "initial_first_line_regimen")
    private String initialFirstLineRegimen;
    @Column(name = "initial_first_line_regimen_date")
    private Date initialFirstLineRegimenDate;
    @Column(name = "initial_second_line_regimen")
    private String initialSecondLineRegimen;
    @Column(name = "initial_second_line_regimen_date")
    private Date initialSecondLineRegimenDate;
    @Column(name = "last_pickup_date_previous_quarter")
    private Date lastPickupDatePreviousQuarter;
    @Column(name = "drug_duration_previous_quarter")
    private Double drugDurationPreviousQuarter;
    @Column(name = "patient_outcome_previous_quarter")
    private String patientOutcomePreviousQuarter;
    @Column(name = "patient_outcome_date_previous_quarter")
    private Date patientOutcomeDatePreviousQuarter;
    @Column(name = "art_status_previous_quarter")
    private String artStatusPreviousQuarter;

    @Column(name = "art_confirmation_date")
    private Date artConfirmationDate;
    @Column(name = "first_pickup_date")
    private Date firstPickupDate;
    @Column(name = "hts_no")
    private String htsNo;
    @Column(name = "last_qty_of_arv_refill")
    private Long lastQtyOfArvRefill;
    private String quarter;
    @Column(name = "age_range")
    private String ageRange;
    @Column(name = "has_critical_error")
    private boolean hasCriticalError;

    public ArtLinelist(ArtLinelist artLinelist) {
        this.state = artLinelist.getState();
        this.lgaName = artLinelist.getLgaName();
        this.datimCode = artLinelist.getDatimCode();
        this.facilityName = artLinelist.getFacilityName();
        this.patientUniqueId = artLinelist.getPatientUniqueId();
        this.patientHospitalNo = artLinelist.getPatientHospitalNo();
        this.ancNoConceptId = artLinelist.getAncNoConceptId();
        this.ancNoIdentifier = artLinelist.getAncNoIdentifier();
        this.sex = artLinelist.getSex();
        this.patientId = artLinelist.getPatientId();
        this.patientUuid = artLinelist.getPatientUuid();
        this.ageAtStartOfArtYears = artLinelist.getAgeAtStartOfArtYears();
        this.ageAtStartOfArtMonths = artLinelist.getAgeAtStartOfArtMonths();
        this.careEntryPoint = artLinelist.getCareEntryPoint();
        this.kpType = artLinelist.getKpType();
        this.monthsOnArt = artLinelist.getMonthsOnArt();
        this.dateTransferredIn = artLinelist.getDateTransferredIn();
        this.transferInStatus = artLinelist.getTransferInStatus();
        this.artStartDate = artLinelist.getArtStartDate();
        this.lastPickupDate = artLinelist.getLastPickupDate();
        this.lastVisitDate = artLinelist.getLastVisitDate();
        this.daysOfArvRefil = artLinelist.getDaysOfArvRefil();
        this.pillBalance = artLinelist.getPillBalance();
        this.initialRegimenLine = artLinelist.getInitialRegimenLine();
        this.initialRegimen = artLinelist.getInitialRegimen();
        this.initialCd4Count = artLinelist.getInitialCd4Count();
        this.initialCd4CountDate = artLinelist.getInitialCd4CountDate();
        this.currentCd4Count = artLinelist.getCurrentCd4Count();
        this.currentCd4CountDate = artLinelist.getCurrentCd4CountDate();
        this.lastEacDate = artLinelist.getLastEacDate();
        this.currentRegimenLine = artLinelist.getCurrentRegimenLine();
        this.currentRegimen = artLinelist.getCurrentRegimen();
        this.pregnancyStatus = artLinelist.getPregnancyStatus();
        this.pregnancyStatusDate = artLinelist.getPregnancyStatusDate();
        this.edd = artLinelist.getEdd();
        this.lastDeliveryDate = artLinelist.getLastDeliveryDate();
        this.lmp = artLinelist.getLmp();
        this.gestationAgeWeeks = artLinelist.getGestationAgeWeeks();
        this.currentViralLoad = artLinelist.getCurrentViralLoad();
        this.viralLoadEncounterDate = artLinelist.getViralLoadEncounterDate();
        this.viralLoadSampleCollectionDate = artLinelist.getViralLoadSampleCollectionDate();
        this.viralLoadReportedDate = artLinelist.getViralLoadReportedDate();
        this.resultDate = artLinelist.getResultDate();
        this.assayDate = artLinelist.getAssayDate();
        this.approvalDate = artLinelist.getApprovalDate();
        this.viralLoadIndication = artLinelist.getViralLoadIndication();
        this.patientOutcome = artLinelist.getPatientOutcome();
        this.patientOutcomeDate = artLinelist.getPatientOutcomeDate();
        this.currentArtStatus = artLinelist.getCurrentArtStatus();
        this.dispensingModality = artLinelist.getDispensingModality();
        this.facilityDispensingModality = artLinelist.getFacilityDispensingModality();
        this.dddDispensingModality = artLinelist.getDddDispensingModality();
        this.mmdType = artLinelist.getMmdType();
        this.dateReturnedToCare = artLinelist.getDateReturnedToCare();
        this.dateOfTermination = artLinelist.getDateOfTermination();
        this.pharmacyNextAppointment = artLinelist.getPharmacyNextAppointment();
        this.clinicalNextAppointment = artLinelist.getClinicalNextAppointment();
        this.currentAgeYrs = artLinelist.getCurrentAgeYrs();
        this.currentAgeMonths = artLinelist.getCurrentAgeMonths();
        this.dateOfBirth = artLinelist.getDateOfBirth();
        this.markAsDeceased = artLinelist.getMarkAsDeceased();
        this.markAsDeceasedDeathDate = artLinelist.getMarkAsDeceasedDeathDate();
        this.registrationPhoneNo = artLinelist.getRegistrationPhoneNo();
        this.nextOfKinPhoneNo = artLinelist.getNextOfKinPhoneNo();
        this.treatmentSupporterPhoneNo = artLinelist.getTreatmentSupporterPhoneNo();
        this.biometricCaptured = artLinelist.getBiometricCaptured();
        this.biometricCaptureDate = artLinelist.getBiometricCaptureDate();
        this.validCapture = artLinelist.getValidCapture();
        this.currentWeight = artLinelist.getCurrentWeight();
        this.currentWeightDate = artLinelist.getCurrentWeightDate();
        this.tbStatus = artLinelist.getTbStatus();
        this.tbStatusDate = artLinelist.getTbStatusDate();
        this.baselineInhStartDate = artLinelist.getBaselineInhStartDate();
        this.baselineInhStopDate = artLinelist.getBaselineInhStopDate();
        this.currentInhStartDate = artLinelist.getCurrentInhStartDate();
        this.currentInhOutcome = artLinelist.getCurrentInhOutcome();
        this.currentInhOutcomeDate = artLinelist.getCurrentInhOutcomeDate();
        this.lastInhDispensedDate = artLinelist.getLastInhDispensedDate();
        this.baselineTbTreatmentStartDate = artLinelist.getBaselineTbTreatmentStartDate();
        this.baselineTbTreatmentStopDate = artLinelist.getBaselineTbTreatmentStopDate();
        this.lastViralLoadSampleCollectionFormDate = artLinelist.getLastViralLoadSampleCollectionFormDate();
        this.lastSampleTakenDate = artLinelist.getLastSampleTakenDate();
        this.otzEnrollmentDate = artLinelist.getOtzEnrollmentDate();
        this.otzOutcomeDate = artLinelist.getOtzOutcomeDate();
        this.enrollmentDate = artLinelist.getEnrollmentDate();
        this.initialFirstLineRegimen = artLinelist.getInitialFirstLineRegimen();
        this.initialFirstLineRegimenDate = artLinelist.getInitialFirstLineRegimenDate();
        this.initialSecondLineRegimen = artLinelist.getInitialSecondLineRegimen();
        this.initialSecondLineRegimenDate = artLinelist.getInitialSecondLineRegimenDate();
        this.lastPickupDatePreviousQuarter = artLinelist.getLastPickupDatePreviousQuarter();
        this.drugDurationPreviousQuarter = artLinelist.getDrugDurationPreviousQuarter();
        this.patientOutcomePreviousQuarter = artLinelist.getPatientOutcomePreviousQuarter();
        this.patientOutcomeDatePreviousQuarter = artLinelist.getPatientOutcomeDatePreviousQuarter();
        this.artStatusPreviousQuarter = artLinelist.getArtStatusPreviousQuarter();
        this.artConfirmationDate = artLinelist.getArtConfirmationDate();
        this.firstPickupDate = artLinelist.getFirstPickupDate();
        this.htsNo = artLinelist.getHtsNo();
        this.lastQtyOfArvRefill = artLinelist.getLastQtyOfArvRefill();
        this.hasCriticalError = artLinelist.isHasCriticalError();
    }

}
