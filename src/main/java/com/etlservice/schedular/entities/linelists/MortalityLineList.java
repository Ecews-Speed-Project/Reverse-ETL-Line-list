package com.etlservice.schedular.entities.linelists;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
@Getter
@Setter
@Entity
public class MortalityLineList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String facilityState;
    private String facilityName;
    private String datimCode;
    private String hospitalNumber;
    private String patientId;
    private String stateOfResidence;
    private String lgaOfResidence;
    private String maritalStatus;
    private String sex;
    private LocalDate dateOfBirth;
    private Integer markAsDeceased;
    private LocalDate markAsDeceasedDate;
    private LocalDate patientDeceasedDate;
    private LocalDate dateOfDiagnosis;
    private String whoClinicalStageAtDiagnosis;
    private String lastRecordedWhoClinicalStage;
    private LocalDate dateOfLastRecordedWhoClinicalStage;
    private Double cd4CountAtDiagnosis;
    private LocalDate dateOfBaselineCd4Count;
    private Double lastRecordedCd4Count;
    private LocalDate dateOfLastRecordedCd4Count;
    private LocalDate dateOfArtInitiation;
    private String artRegimenLineAtInitiation;
    private String artRegimenAtInitiation;
    private String lastArtRegimenLine;
    private String lastArtRegimen;
    private LocalDate dateOfLastDrugPickup;
    private Integer daysOfArvRefillAtLastDrugPickup;
    private Double lastRecordedWeight;
    private LocalDate dateOfLastRecordedWeight;
    private Double lastRecordedSystolicBloodPressure;
    private LocalDate dateOfLastRecordedSystolicBloodPressure;
    private Double lastRecordedDiastolicBloodPressure;
    private LocalDate dateOfLastRecordedDiastolicBloodPressure;
    private Double lastAvailableViralLoadResult;
    private LocalDate dateSampleCollectedForLastAvailableViralLoadResult;
    private LocalDate lastViralLoadSampleCollectionDate;
    private String nonVaCauseOfDeath;
    @Column(name = "smart_va_cod")
    private String smartVACOD;
    private String tbStatus;
    private LocalDate tbStatusDate;
    @Column(name = "prophylaxis_use_cpt")
    private String prophylaxisUseCPT;
    private LocalDate lastCptPickupDate;
    @Column(name = "prophylaxis_use_tpt")
    private String prophylaxisUseTPT;
    private LocalDate firstTptPickupDate;
    private LocalDate tptStartDate;
    private LocalDate lastTptPickupDate;
    private String currentInhOutcome;
    private LocalDate currentInhOutcomeDate;
}
