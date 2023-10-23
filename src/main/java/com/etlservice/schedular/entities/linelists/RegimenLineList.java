package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class RegimenLineList extends BaseClass {
    private LocalDate artStartDate;
    private String pepfarId;
    private String facilityName;
    private String regimen;
    private LocalDate regimenDate;
}
