package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.model.Container;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "radet")
public class Radet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "DatimCode")
    private String datimCode;

    @Column(name = "FacilityName")
    private String facilityName;

    @Column(name = "PatientID")
    private Integer patientID;

    @Column(name = "PatientUniqueID")
    private String patientUniqueID;

    @Column(name = "PatientHospitalNo")
    private String patientHospitalNo;

    @Column(name = "AncNo")
    private String ancNo;

    @Column(name = "HtsNo")
    private String htsNo;

    @Column(name = "Sex")
    private String sex;

    @Column(name = "AgeAtStartOfArtYears")
    private Integer ageAtStartOfARTYears;

    @Column(name = "AgeAtStartOfArtMonths")
    private Integer ageAtStartOfARTMonths;

    @Column(name = "CareEntryPoint")
    private String careEntryPoint;

    @Column(name = "KpType")
    private String kpType;

    @Column(name = "MonthsOnArt")
    private String monthsOnArt;

    @Column(name = "DateTransferredIn")
    private Date dateTransferredIn;

    @Column(name = "TransferInStatus")
    private String transferInStatus;

    @Column(name = "ArtStartDate")
    private Date artStartDate;

    @Column(name = "LastPickupDate")
    private Date lastPickupDate;

    @Column(name = "LastVisitDate")
    private Date lastVisitDate;

    @Column(name = "DaysOfArvRefil")
    private String daysOfARVRefil;

    @Column(name = "PillBalance")
    private String pillBalance;

    @Column(name = "InitialRegimenLine")
    private String initialRegimenLine;

    @Column(name = "InitialRegimen")
    private String initialRegimen;

    @Column(name = "InitialCd4Count")
    private String initialCd4Count;

    @Column(name = "InitialCd4CountDate")
    private Date initialCd4CountDate;

    @Column(name = "CurrentCd4Count")
    private String currentCd4Count;

    @Column(name = "CurrentCd4CountDate")
    private Date currentCd4CountDate;

    @Column(name = "LastEacDate")
    private Date lastEacDate;

    @Column(name = "CurrentRegimenLine")
    private String currentRegimenLine;

    @Column(name = "CurrentRegimen")
    private String currentRegimen;

    @Column(name = "PregnancyStatus")
    private String pregnancyStatus;

    @Column(name = "PregnancyStatusDate")
    private Date pregnancyStatusDate;

    @Column(name = "EDD")
    private Date edd;

    @Column(name = "LastDeliveryDate")
    private Date lastDeliveryDate;

    @Column(name = "LMP")
    private Date lmp;

    @Column(name = "EstimatedGestationAgeWeeks")
    private String estimatedGestationAgeWeeks;

    @Column(name = "CurrentViralLoad")
    private String currentViralLoad;

    @Column(name = "ViralLoadEncounterDate")
    private Date viralLoadEncounterDate;

    @Column(name = "ViralLoadSampleCollectionDate")
    private Date viralLoadSampleCollectionDate;

    @Column(name = "ViralLoadReportedDate")
    private Date viralLoadReportedDate;

    @Column(name = "ResultDate")
    private Date resultDate;

    @Column(name = "AssayDate")
    private Date assayDate;

    @Column(name = "ApprovalDate")
    private Date approvalDate;

    @Column(name = "ViralLoadIndication")
    private String viralLoadIndication;

    @Column(name = "PatientOutcome")
    private String patientOutcome;

    @Column(name = "PatientOutcomeDate")
    private Date patientOutcomeDate;

    @Column(name = "CurrentArtStatus")
    private String currentARTStatus;

    @Column(name = "DispensingModality")
    private String dispensingModality;

    @Column(name = "FacilityDispensingModality")
    private String facilityDispensingModality;

    @Column(name = "DddDispensingModality")
    private String dddDispensingModality;

    @Column(name = "MmdType")
    private String mmdType;

    @Column(name = "DateReturnedToCare")
    private Date dateReturnedToCare;

    @Column(name = "DateOfTermination")
    private Date dateOfTermination;

    @Column(name = "PharmacyNextAppointment")
    private Date pharmacyNextAppointment;

    @Column(name = "ClinicalNextAppointment")
    private Date clinicalNextAppointment;

    @Column(name = "CurrentAgeYears")
    private Integer currentAgeYears;

    @Column(name = "CurrentAgeMonths")
    private Integer currentAgeMonths;

    @Column(name = "DateOfBirth")
    private Date dateOfBirth;

    @Column(name = "MarkAsDeseased")
    private String markAsDeseased;

    @Column(name = "MarkAsDeseasedDeathDate")
    private Date markAsDeseasedDeathDate;

    @Column(name = "RegistrationPhoneNo")
    private String registrationPhoneNo;

    @Column(name = "NextofKinPhoneNo")
    private String nextOfkinPhoneNo;

    @Column(name = "TreatmentSupporterPhoneNo")
    private String treatmentSupporterPhoneNo;

    @Column(name = "BiometricCaptured")
    private String biometricCaptured;

    @Column(name = "BiometricCapturedDate")
    private Date biometricCapturedDate;

    @Column(name = "ValidCapture")
    private String validCapture;

    @Column(name = "CurrentWeight")
    private String currentWeight;

    @Column(name = "CurrentWeightDate")
    private Date currentWeightDate;

    @Column(name = "TbStatus")
    private String tbStatus;

    @Column(name = "TbStatusDate")
    private Date tbStatusDate;

    @Column(name = "InhStartDate")
    private Date inhStartDate;

    @Column(name = "InhStopDate")
    private Date inhStopDate;

    @Column(name = "LastInhDispensedDate")
    private Date lastINHDispensedDate;

    @Column(name = "TbTreatmentStartDate")
    private Date tbTreatmentStartDate;

    @Column(name = "TnTreatmentStopDate")
    private Date tbTreatmentStopDate;

    @Column(name = "LastViralLoadSampleCollectionFormDate")
    private Date lastViralLoadSampleCollectionFormDate;

    @Column(name = "LastSampleTakenDate")
    private Date lastSampleTakenDate;

    @Column(name = "OtzEnrollmentDate")
    private Date otzEnrollmentDate;

    @Column(name = "OtzOutcomeDate")
    private Date otzOutcomeDate;

    @Column(name = "EnrollmentDate")
    private Date enrollmentDate;

    @Column(name = "InitialFirstLineRegimen")
    private String initialFirstLineRegimen;

    @Column(name = "InitialFirstLineRegimenDate")
    private Date initialFirstLineRegimenDate;

    @Column(name = "InitialSecondLineRegimen")
    private String initialSecondLineRegimen;

    @Column(name = "InitialSecondLineRegimenDate")
    private Date initialSecondLineRegimenDate;

    @Column(name = "LastPickupDatePreviousQuarter")
    private Date lastPickupDatePreviousQuarter;

    @Column(name = "DrugDurationPreviousQuarter")
    private String drugDurationPreviousQuarter;

    @Column( name = "PatientOutcomePreviousQuarter")
    private String patientOutcomePreviousQuarter;

    @Column(name = "PatientOutcomeDatePreviousQuarter")
    private Date patientOutcomeDatePreviousQuarter;

    @Column( name = "ArtStatusPreviousQuarter")
    private String artStatusPreviousQuarter;

    @Column(name = "TouchTime")
    private Date touchTime;

    @Column(name = "PatientUuid")
    protected String patientUuid;

    @Transient
    Container container;
}
