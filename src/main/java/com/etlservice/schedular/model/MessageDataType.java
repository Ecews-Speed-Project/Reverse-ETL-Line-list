//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.06.06 at 02:19:42 AM WAT 
//


package com.etlservice.schedular.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for MessageDataType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="MessageDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Demographics" type="{}DemographicsType"/>
 *         &lt;element name="Visits" type="{}VisitType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Encounters" type="{}EncounterType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Obs" type="{}ObsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="EncounterProviders" type="{}EncounterProviderType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientIdentifiers" type="{}PatientIdentifierType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientBiometrics" type="{}PatientBiometricType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientPrograms" type="{}PatientProgramType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDataType", propOrder = {
        "demographics",
        "visits",
        "encounters",
        "obs",
        "encounterProviders",
        "patientIdentifiers",
        "patientBiometrics",
        "patientPrograms",
        "patientBiometricVerifications",
        "integratorClientIntakes"
})
public class MessageDataType implements Serializable {

    @XmlElement(name = "Demographics", required = true)
    private DemographicsType demographics;
    @XmlElement(name = "Visits")
    private List<VisitType> visits;
    @XmlElement(name = "Encounters")
    private List<EncounterType> encounters;
    @XmlElement(name = "Obs")
    private List<ObsType> obs;
    @XmlElement(name = "EncounterProviders")
    private List<EncounterProviderType> encounterProviders;
    @XmlElement(name = "PatientIdentifiers")
    private List<PatientIdentifierType> patientIdentifiers;
    @XmlElement(name = "PatientBiometrics")
    private List<PatientBiometricType> patientBiometrics;
    @XmlElement(name = "PatientPrograms")
    private List<PatientProgramType> patientPrograms;
    private List<PatientBiometricVerificationType> patientBiometricVerifications;
    private List<IntegratorClientIntakeType> integratorClientIntakes;

    /**
     * Gets the value of the demographics property.
     *
     * @return possible object is
     * {@link DemographicsType }
     */
    public DemographicsType getDemographics() {
        return demographics;
    }

    /**
     * Sets the value of the demographics property.
     *
     * @param value allowed object is
     *              {@link DemographicsType }
     */
    public void setDemographics(DemographicsType value) {
        this.demographics = value;
    }

    /**
     * Gets the value of the visits property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the visits property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVisits().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VisitType }
     */
    public List<VisitType> getVisits() {
        if (visits == null) {
            visits = new ArrayList<>();
        }
        return this.visits;
    }

    /**
     * Gets the value of the encounters property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the encounters property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEncounters().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EncounterType }
     */
    public List<EncounterType> getEncounters() {
        if (encounters == null) {
            encounters = new ArrayList<>();
        }
        return this.encounters;
    }

    /**
     * Gets the value of the obs property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the obs property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObs().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ObsType }
     */
    public List<ObsType> getObs() {
        if (obs == null) {
            obs = new ArrayList<>();
        }
        return this.obs;
    }

    /**
     * Gets the value of the encounterProviders property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the encounterProviders property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEncounterProviders().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EncounterProviderType }
     */
    public List<EncounterProviderType> getEncounterProviders() {
        if (encounterProviders == null) {
            encounterProviders = new ArrayList<>();
        }
        return this.encounterProviders;
    }

    /**
     * Gets the value of the patientIdentifiers property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientIdentifiers property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientIdentifiers().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientIdentifierType }
     */
    public List<PatientIdentifierType> getPatientIdentifiers() {
        if (patientIdentifiers == null) {
            patientIdentifiers = new ArrayList<>();
        }
        return this.patientIdentifiers;
    }

    /**
     * Gets the value of the patientBiometrics property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientBiometrics property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientBiometrics().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientBiometricType }
     */
    public List<PatientBiometricType> getPatientBiometrics() {
        if (patientBiometrics == null) {
            patientBiometrics = new ArrayList<>();
        }
        return this.patientBiometrics;
    }

    /**
     * Gets the value of the patientPrograms property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientPrograms property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientPrograms().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientProgramType }
     */
    public List<PatientProgramType> getPatientPrograms() {
        if (patientPrograms == null) {
            patientPrograms = new ArrayList<>();
        }
        return this.patientPrograms;
    }

    public List<PatientBiometricVerificationType> getPatientBiometricVerifications() {
        if (patientBiometricVerifications == null) {
            patientBiometricVerifications = new ArrayList<>();
        }
        return this.patientBiometricVerifications;
    }

    public List<IntegratorClientIntakeType> getIntegratorClientIntakes() {
        if (integratorClientIntakes == null) {
            integratorClientIntakes = new ArrayList<>();
        }
        return this.integratorClientIntakes;
    }
}
