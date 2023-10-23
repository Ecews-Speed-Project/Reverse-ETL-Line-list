package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BiometricLineList extends BaseClass {
    private String state;
    private String facilityName;
    private String datimCode;
    private String patientUuid;
    private String patientUniqueId;
    private String sex;
    private LocalDate dateOfBirth;
    private Integer currentAge;
    private String currentArtStatus;
    private LocalDate artStartDate;
    private LocalDate lastPickupDate;
    private String transferredOutStatus;
    private LocalDate dateTransferredOut;
    private String transferredInStatus;
    private LocalDate dateTransferredIn;
    private String careEntryPoint;
    private String dsdModel;
    private String mmdModel;
    private LocalDate pharmacyNextAppointmentDate;
    private LocalDate dateCaptured;
    private Integer numberOfFingerPrints;
    private String serialNumber;
    private Integer leftThumbQuality;
    private Integer leftIndexQuality;
    private Integer leftMiddleQuality;
    private Integer leftWeddingQuality;
    private Integer leftSmallQuality;
    private Integer rightThumbQuality;
    private Integer rightIndexQuality;
    private Integer rightMiddleQuality;
    private Integer rightWeddingQuality;
    private Integer rightSmallQuality;
}
