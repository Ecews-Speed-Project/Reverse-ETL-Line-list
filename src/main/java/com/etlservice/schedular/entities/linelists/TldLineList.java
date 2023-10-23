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
public class TldLineList extends BaseClass {
    private String facilityName;
    private String identifier;
    private LocalDate artStartDate;
    private LocalDate firstTldDate;
    private LocalDate visit;
    private String regimen;
    private LocalDate regimenDate;
    private Double viralLoad;
    private LocalDate viralLoadSampleCollectionDate;
}
