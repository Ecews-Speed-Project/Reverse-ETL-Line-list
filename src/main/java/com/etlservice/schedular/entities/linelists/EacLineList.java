package com.etlservice.schedular.entities.linelists;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class EacLineList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String state;
    private String lga;
    private String datimCode;
    private String facilityName;
    private String patientUniqueId;
    private String patientHospitalNo;
    private String sex;
    private Integer ageAtStartOfArtYears;
    private Integer ageAtStartOfArtMonths;
    private String careEntryPoint;
    private String kpType;
    private Integer monthsOnArt;
    private LocalDate dateTransferredIn;
    private String transferInStatus;
    private LocalDate artStartDate;
    private LocalDate lastPickupDate;
    private LocalDate lastVisitDate;
    private Integer daysOfArvRefill;
    private Integer pillBalance;
    private LocalDate eac1Date;
    private LocalDate eac2Date;
    private LocalDate eac3Date;
    private LocalDate eac4Date;
    private Double viralLoad1;
    private Double viralLoad2;
    private Double viralLoad3;
    private LocalDate viralLoad1SampleDate;
    private LocalDate viralLoad2SampleDate;
    private LocalDate viralLoad3SampleDate;
    private LocalDate viralLoad1ReportDate;
    private LocalDate viralLoad2ReportDate;
    private LocalDate viralLoad3ReportDate;
    private String currentRegimenLine;
    private String currentRegimen;
    private LocalDate secondLineRegimenStartDate;
    private LocalDate thirdLineRegimenStartDate;
    private String pregnancyStatus;
    private LocalDate pregnancyStatusDate;
    private LocalDate edd;
    private String lastEacSessionType;
    private LocalDate lastEacSessionDate;
    private String lastEacBarriersToAdherence;
    private String lastEacRegimenPlan;
    private LocalDate lastEacFollowupDate;
    private String lastEacAdherenceCounsellorComments;
    private Double currentViralLoad;
    private LocalDate viralLoadEncounterDate;
    private LocalDate viralLoadSampleCollectionDate;
    private String viralLoadIndication;
    private LocalDate lastSampleTakenDate;
    private String patientOutcome;
    private LocalDate patientOutcomeDate;
    private String currentArtStatus;
    private String dispensingModality;
    private String facilityDispensingModality;
    private String dddDispensingModality;
    private String mmdType;
    private LocalDate pharmacyNextAppointmentDate;
    private LocalDate clinicalNextAppointmentDate;
    private Integer currentAgeYears;
    private Integer currentAgeMonths;
    private LocalDate dateOfBirth;
    private String patientUuid;
}
