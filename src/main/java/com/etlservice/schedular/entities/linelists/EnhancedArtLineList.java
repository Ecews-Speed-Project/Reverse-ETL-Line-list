package com.etlservice.schedular.entities.linelists;

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
public class EnhancedArtLineList extends BaseLineList {
    private String initialWhoStaging;
    private LocalDate initialWhoStagingDate;
    private String currentWhoStaging;
    private LocalDate currentWhoStagingDate;
    private String currentCd4LfaResult;
    private LocalDate currentCd4LfaResultDate;
    private String tblFlamResult;
    private LocalDate tblFlamResultDate;
    private String serologyForCrAgResult;
    private LocalDate serologyForCrAgResultDate;
    private String csfForCrAgResult;
    private LocalDate csfForCrAgResultDate;
    private String receivedFluconazole;
    private LocalDate receivedFluconazoleDate;
    private String previousFirstLineRegimen;
    private LocalDate previousFirstLineRegimenDate;
    private String previousSecondLineRegimen;
    private LocalDate previousSecondLineRegimenDate;
    private String previousThirdLineRegimen;
    private LocalDate previousThirdLineRegimenDate;
    private String arvDrugAndStrengthList;
    private String oiDrugListAndStrength;
    private Double weightAsAtJan2022;
    private LocalDate weightDateAsAtJan2022;
    private Double weightAsAtDec2022;
    private LocalDate weightDateAsAtDec2022;
}
