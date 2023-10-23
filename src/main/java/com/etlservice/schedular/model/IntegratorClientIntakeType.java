package com.etlservice.schedular.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IntegratorClientIntakeType implements Serializable {
	
	private Integer clientIntakeId;
	
	private Integer patientId;
	
	private String patientIdentifier;
	
	private String gender;
	
	private String phoneNumber;
	
	private String patientName;
	
	private String familyName;
	
	private Integer age;
	
	private String address;
	
	private String state;
	
	private String lga;
	
	private Date encounterDate;
	
	private Integer serviceAreaId;
	
	private String serviceAreaName;
	
	private Integer hivBloodTransfusion;
	
	private Integer hivUnprotected;
	
	private Integer hivSti;
	
	private Integer hivDiagnosed;
	
	private Integer hivIvDrug;
	
	private Integer hivForced;
	
	private Integer stiMaleGenital;
	
	private Integer stiMaleScrotal;
	
	private Integer stiMaleUrethral;
	
	private Integer stiFemaleGenital;
	
	private Integer stiFemaleAbdominal;
	
	private Integer stiFemaleVaginal;
	
	private Integer hivScore;
	
	private Integer tbCoughTwoWeek;
	
	private Integer tbFever;
	
	private Integer tbCoughHeamoptysis;
	
	private Integer tbCoughUnexplainedWeightLoss;
	
	private Integer tbUnexplainedWeightLoss;
	
	private Integer tbWeightLoss;
	
	private Integer tbNightSweat;
	
	private Integer tbCoughWeightLoss;
	
	private Integer tbCoughFever;
	
	private Integer tbCoughSputum;
	
	private Integer tbScore;
	
	private Double covidTemperature;
	
	private Integer covidDryCough;
	
	private Integer covidFever;
	
	private Integer covidNotVaccinated;
	
	private Integer covidLossSmell;
	
	private Integer covidHeadache;
	
	private Integer covidCloseContact;
	
	private Integer covidHealthCare;
	
	private Integer covidShortnessBreath;
	
	private Integer covidMuscleAche;
	
	private Integer covidLossTaste;
	
	private Integer covidSoreThroat;
	
	private Integer covidTravel;
	
	private Integer covidChronicNcd;
	
	private Integer covidScore;
	
	private Integer ncdHypertensive;
	
	private Integer ncdHtnMedication;
	
	private Integer ncdBpUpper;
	
	private Integer ncdBpLower;
	
	private Integer ncdDiabetic;
	
	private Integer ncdDmMedication;
	
	private Integer ncdRbs;
	
	private Integer ncdScore;
	
	private Integer tbTest;
	
	private Integer covidTest;
	
	private Integer hivTest;
	
	private Integer ncdTest;
	
	private Date dateCreated;
	
	private Double bmiWeight;
	
	private Double bmiHeight;
	
	private Double bmiValue;
	
	private String bmiRemark;
	
	private Integer covidHaveYouBeenVaccinated;
	
	private Integer covidVaccinationDose;
	
	private String covidNameOfVaccine;
	
	private Date covidDateOfVaccination;
	
	private Integer cervicalEverCervical;
	
	private String cervicalCervical;
	
	private String ncdOther;
	
	private String ncdComment;

}
