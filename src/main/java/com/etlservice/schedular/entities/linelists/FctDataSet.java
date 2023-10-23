package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class FctDataSet extends BaseClass {
    private String state;
    private String lga;
    private String datimCode;
    private String facilityName;
    private String patientUuid;
    private String sex;
    private Integer currentAge;
    private LocalDate dateOfBirth;
    private Integer ageAtStartOfArtYears;
    private Integer ageAtStartOfArtMonths;
    private String careEntryPoint;
    private String kpType;
    private LocalDate dateConfirmedPositive;
    private String occupationalStatus;
//    private String employmentStatus;
    private String educationalLevel;
    private String maritalStatus;
    private double lastViralLoad;
    private LocalDate lastViralLoadDate;
    private LocalDate artStartDate;
    private String currentRegimenLine;
    private String currentRegimen;
    private LocalDate lastPickupDate;
    private Integer drugDuration;
    private String currentStatus;
    private String patientOutcome;
    private LocalDate nextAppointmentDate;
}
