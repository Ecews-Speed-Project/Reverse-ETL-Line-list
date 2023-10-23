package com.etlservice.schedular.entities.linelists;

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
public class CustomLineList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String facilityName;
    private String identifier;
    private Date dob;
    private int age;
    private String gender;
    private Date visit;
    private String pregnancyStatus;
    private Date pregnancyStatusDate;
    private Date pregnancyDueDate;
    private String kpIdentifier;
    private Date dateOfHivDiagnosis;
    private Date artStartDate;
    private String pickUpReason;
    private String regimen;
    private Date regimenDate;
    private int dispensedQuantity;
    private Date dispensedDate;
    private int daysOfArv;
    private String dispenseModality;
    private String dddDispensing;
    private Double viralLoad;
    private Date viralLoadSampleCollectionDate;
    private String transferInStatus;
    private String patientOutcome;
    private Date eacDate;
    private String eacSessionType;
    private String eacBarrier;
    private String eacIntervention;
}
