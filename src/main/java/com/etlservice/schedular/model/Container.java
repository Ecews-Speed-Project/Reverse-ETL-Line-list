/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.model;

/**
 * @author lordmaul
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MessageHeader" type="{}MessageHeaderType"/>
 *         &lt;choice>
 *           &lt;element name="IndividualReport" type="{}IndividualReportType"/>
 *         &lt;/choice>
 *         &lt;element name="Validation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"id", "messageHeader", "messageData"})
@XmlRootElement(name = "Container")
@Getter
@Setter
public class Container implements Serializable {

    @Id
    @XmlElement(name = "Id", required = true)
    String id;

    @XmlElement(name = "MessageHeader", required = true)
    private MessageHeaderType messageHeader;

    @XmlElement(name = "MessageData")
    private MessageDataType messageData;

}
