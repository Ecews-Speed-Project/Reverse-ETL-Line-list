package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class BaselineViralLoadLineList extends BaseClass {
    private String state;
    private String lga;
    private String datimCode;
    private String facilityName;
    private String patientUniqueId;
    private String artStartMonth;
    private LocalDate artStartDate;
    private String txNew;
    private LocalDate dateOfBirth;
    private Integer ageAtStartOfArt;
    private LocalDate lastViralLoadSampleCollectionDate;
    private String fourteenDaysAfterArtStart;
    private String baselineViralLoadSample;
    private LocalDate lastViralLoadSampleCollectionDateDocumentedResult;
    private String baselineViralLoadResult;
    private Double viralLoadResult;
    private String baselineViralLoadResultSuppression;
    private LocalDate recencyTestDate;
    private String recencyTestDone;
    private String recencyTestResult;
    private String possibleRecycler;
    private String careEntryPoint;
    private String kpType;
    @Column(name = "CD4_date")
    private LocalDate cd4Date;
    @Column(name = "CD4_result")
    private Double cd4Result;
    private String patientUuid;
    private String currentArtStatus;
}
