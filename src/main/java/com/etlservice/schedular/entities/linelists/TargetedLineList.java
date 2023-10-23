package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class TargetedLineList extends BaseClass {
    private String state;
    private String lga;
    private String facility;
    private String datimCode;
    private String artUniqueId;
    private String form;
    private Integer conceptId;
    private Integer obsGroupId;
    private String variableName;
    private String variableValue;
    private LocalDate visitDate;
    private Integer creator;
    private LocalDate dateCreated;
    private Integer changedBy;
    private LocalDate dateChanged;
}
