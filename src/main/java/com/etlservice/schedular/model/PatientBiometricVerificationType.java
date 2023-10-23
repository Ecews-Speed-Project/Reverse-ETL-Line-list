package com.etlservice.schedular.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class PatientBiometricVerificationType implements Serializable {
	
	private int biometricInfoId;
	
	private int patientId;
	
	private String template;
	
	private int imageHeight;
	
	private int imageWidth;
	
	private int imageDpi;
	
	private int imageQuality;
	
	private String fingerPosition;
	
	private String serialNumber;
	
	private String model;
	
	private String manufacturer;
	
	private int creator;
	
	private Date dateCreated;
	
	private String patientUuid;
	
	private String datimId;
	
	private String encodedTemplate;
	
	private String hashed;
	
	private Integer recaptureCount;
}
