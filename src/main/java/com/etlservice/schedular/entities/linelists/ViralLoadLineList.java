package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
public class ViralLoadLineList extends BaseClass {
    private String state;
    private String lga;
    private String datimCode;
    private String facilityName;
    private String patientUniqueId;
    private String patientHospitalNo;
    private String sex;
    private LocalDate dateOfBirth;
    private Integer ageAtStartOfArtYears;
    private Integer ageAtStartOfArtMonths;
    private Integer currentAgeYears;
    private Integer currentAgeMonths;
    private LocalDate enrollmentDate;
    private LocalDate dateConfirmedPositive;
    private String careEntryPoint;
    private String kpType;
    private LocalDate dateTransferredIn;
    private String transferInStatus;
    private LocalDate artStartDate;
    private LocalDate visitDate;
    private Integer monthsOnArt;
    private Integer timeSinceDiagnosis;
    private String regimenLine;
    private String regimenPickedUp;
    private String pickupReason;
    private String oiDrugList;
    private Integer daysOfArvRefill;
    private Integer pillBalance;
    private Double cd4Count;
    private LocalDate cd4CountDate;
    private LocalDate eacDate;
    private String pregnancyStatus;
    private LocalDate pregnancyStatusDate;
    private Double currentViralLoad;
    private LocalDate viralLoadEncounterDate;
    private LocalDate viralLoadSampleCollectionDate;
    private LocalDate viralLoadReportedDate;
    private String viralLoadIndication;
    private String patientOutcome;
    private LocalDate patientOutcomeDate;
    private String currentArtStatus;
    private String dispensingModality;
    private LocalDate dateReturnedToCare;
    private LocalDate dateOfTermination;
    private LocalDate pharmacyNextAppointment;
    private Double height;
    private LocalDate heightDate;
    private Double currentWeight;
    private LocalDate currentWeightDate;
    private String tbStatus;
    private LocalDate tbStatusDate;
    private String hcv;
    private String hbsAg;
    private String otherOis;
    private Integer missedClinicAppointmentDuration;
    private String patientUuid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ViralLoadLineList that = (ViralLoadLineList) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
