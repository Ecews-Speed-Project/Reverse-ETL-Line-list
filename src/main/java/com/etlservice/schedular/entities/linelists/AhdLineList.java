package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AhdLineList extends BaseClass {
    private String state;
    private String lga;
    @Column(name = "facility_name")
    private String facilityName;
    @Column(name = "datim_code")
    private String datimCode;
    @Column(name = "patient_unique_id")
    private String patientUniqueId;
    @Column(name = "patient_hospital_no")
    private String patientHospitalNo;
    private String sex;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "months_on_art")
    private Integer monthsOnArt;
    @Column(name = "art_start_date")
    private LocalDate artStartDate;
    @Column(name = "last_pickup_date")
    private LocalDate lastPickupDate;
    @Column(name = "days_of_arv_refill")
    private Integer daysOfArvRefill;
    @Column(name = "date_confirmed_positive")
    private LocalDate dateConfirmedPositive;
//    private LocalDate lastEacDate;
//    private Double lastCd4Count;
//    private LocalDate lastCd4CountDate;
    @Column(name = "first_cd4_count_numeric")
    private Double firstCd4CountNumeric;
    @Column(name = "first_cd4_count_numeric_date")
    private LocalDate firstCd4CountNumericDate;
    @Column(name = "first_who_staging")
    private String firstWhoStaging;
    @Column(name = "first_who_staging_date")
    private LocalDate firstWhoStagingDate;
    @Column(name = "first_tb_status")
    private String firstTbStatus;
    @Column(name = "first_tb_status_date")
    private LocalDate firstTbStatusDate;
    @Column(name = "tbl_flam_result")
    private String tblFlamResult;
    @Column(name = "tbl_flam_result_date")
    private LocalDate tblFlamResultDate;
//    private String lastWhoStaging;
//    private LocalDate lastWhoStagingDate;
    @Column(name = "current_regimen_line")
    private String currentRegimenLine;
    @Column(name = "current_regimen")
    private String currentRegimen;
    @Column(name = "current_viral_load")
    private Double currentViralLoad;
    @Column(name = "viral_load_encounter_date")
    private LocalDate viralLoadEncounterDate;
    @Column(name = "viral_load_sample_collection_date")
    private LocalDate viralLoadSampleCollectionDate;
    @Column(name = "patient_outcome")
    private String patientOutcome;
    @Column(name = "patient_outcome_date")
    private LocalDate patientOutcomeDate;
    @Column(name = "current_art_status")
    private String currentArtStatus;
    private String firstCd4LfaResult;
    private LocalDate firstCd4LfaResultDate;
    private String xpertMtbRifRequest;
    private LocalDate xpertMtbRifRequestDate;
    private String tbTreatment;
    private LocalDate tbTreatmentDate;
    private String serologyForCrAg;
    private LocalDate serologyForCrAgDate;
    private String csfForCrAg;
    private LocalDate csfForCrAgDate;
    private LocalDate ctxStartDate;
    private LocalDate inhStartDate;
    private String firstRegimenLine;
    private String firstRegimen;
    private LocalDate firstRegimenPickupDate;
    private String fluconazoleTreatment;
    private LocalDate fluconazoleTreatmentDate;
    private String liposomalAmphotericinB;
    private LocalDate liposomalAmphotericinBDate;
    private Date timeCd4LfaSampleCollected;
    private Date timeCd4LfaResultReceived;
    private Date timeSerologyForCrAgSampleCollected;
    private Date timeSerologyForCrAgResultReceived;
//    private String laboratoryRegistrationNo;
    private String indicationForAhd;

}
