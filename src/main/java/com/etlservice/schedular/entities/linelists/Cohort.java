/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.entities.linelists;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Set;

/**
 *
 * @author The Bright
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen")
    @SequenceGenerator(allocationSize = 1,initialValue = 1, name = "idgen", sequenceName = "entityaseq")
    private  Integer id;

    private String cohortName = "";
    private String cohortDescription = "";
    @Transient
    private String datimCode = "";

    public String getDatimCode() {
        return datimCode;
    }

    public void setDatimCode(String datimCode) {
        this.datimCode = datimCode;
    }

    // @ElementCollection
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Set<Integer> memberPatients;

}
