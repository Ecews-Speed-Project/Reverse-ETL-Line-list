package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.entities.linelists.ArtLinelist;
import com.etlservice.schedular.entities.linelists.EnhancedArtLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.model.Container;
import com.etlservice.schedular.model.ObsType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.EnhancedArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.services.ARTLineListETL;
import com.etlservice.schedular.services.ArtLineListGeneratorService;
import com.etlservice.schedular.services.EnhancedArtLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.etlservice.schedular.utils.ConstantsUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedArtLineListServiceImpl implements EnhancedArtLineListService {
    private final ARTLineListETL artLineListETL;
    private final EnhancedArtLineListRepository enhancedArtLineListRepository;
    private final ModelMapper modelMapper;
    private final FacilityRepository facilityRepository;
    private final HelperFunctions helperFunctions;
    private final ArtLineListGeneratorService artLineListGeneratorService;

    @Override
    public void buildEnhancedArtLineList(List<Container> mongoContainers) {
        log.info("Building Enhanced Art Line List");
        Date cutOff = Date.from(LocalDate.of(2022,12,31).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        log.info("Cut off date is {}", cutOff);
        Date janCutOff = Date.from(LocalDate.of(2022, 1, 31).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        log.info("Jan cut off date is {}", janCutOff);
        mongoContainers.forEach(container -> {
            Facility facility = facilityRepository.findFacilityByDatimCode(container.getMessageHeader().getFacilityDatimCode());
            if (facility != null) {
                ArtLinelist artLinelist = artLineListGeneratorService.mapARTLineList(container, facility, cutOff, null);
                if (artLinelist != null) {
                    Optional<EnhancedArtLineList> optionalEnhancedArtLineList = enhancedArtLineListRepository.findByPatientUuidAndDatimCode(container.getId(), facility.getDatimCode());
                    if (!optionalEnhancedArtLineList.isPresent()) {
                        EnhancedArtLineList enhancedArtLineList = modelMapper.map(artLinelist, EnhancedArtLineList.class);
                        Optional<ObsType> initialWhoObsType = helperFunctions.getMinConceptObsIdWithFormId(CARE_CARD, 5356, container, cutOff);
                        if (initialWhoObsType.isPresent()) {
                            enhancedArtLineList.setInitialWhoStaging(initialWhoObsType.get().getVariableValue());
                            enhancedArtLineList.setInitialWhoStagingDate(convertToLocalDateViaInstant(initialWhoObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> currentWhoObsType = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 5356, container, cutOff);
                        if (currentWhoObsType.isPresent()) {
                            enhancedArtLineList.setCurrentWhoStaging(currentWhoObsType.get().getVariableValue());
                            enhancedArtLineList.setCurrentWhoStagingDate(convertToLocalDateViaInstant(currentWhoObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> currentCd4LfaObsType = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 167088, container, cutOff);
                        if (currentCd4LfaObsType.isPresent()) {
                            enhancedArtLineList.setCurrentCd4LfaResult(currentCd4LfaObsType.get().getVariableValue());
                            enhancedArtLineList.setCurrentCd4LfaResultDate(convertToLocalDateViaInstant(currentCd4LfaObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> tblFlaObsType = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 166697, container, cutOff);
                        if (tblFlaObsType.isPresent()) {
                            enhancedArtLineList.setTblFlamResult(tblFlaObsType.get().getVariableValue());
                            enhancedArtLineList.setTblFlamResultDate(convertToLocalDateViaInstant(tblFlaObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> serologyObsType = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 167090, container, cutOff);
                        if (serologyObsType.isPresent()) {
                            enhancedArtLineList.setSerologyForCrAgResult(serologyObsType.get().getVariableValue());
                            enhancedArtLineList.setSerologyForCrAgResultDate(convertToLocalDateViaInstant(serologyObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> csfObsType = helperFunctions.getMaxConceptObsIdWithFormId(LABORATORY_ORDER_AND_RESULT_FORM, 167082, container, cutOff);
                        if (csfObsType.isPresent()) {
                            enhancedArtLineList.setCsfForCrAgResult(csfObsType.get().getVariableValue());
                            enhancedArtLineList.setCsfForCrAgResultDate(convertToLocalDateViaInstant(csfObsType.get().getObsDatetime()));
                        }
                        Optional<ObsType> fluconazoleObsType = container.getMessageData().getObs()
                                .stream()
                                .filter(obsType -> obsType.getConceptId() == 165727 &&
                                        obsType.getValueCoded() == 76488 &&
                                        obsType.getFormId() == PHARMACY_FORM &&
                                        obsType.getObsDatetime().before(cutOff) &&
                                        obsType.getVoided() == 0)
                                .max(Comparator.comparing(ObsType::getObsDatetime));
                        if (fluconazoleObsType.isPresent()) {
                            enhancedArtLineList.setReceivedFluconazole(fluconazoleObsType.get().getVariableValue());
                            enhancedArtLineList.setReceivedFluconazoleDate(convertToLocalDateViaInstant(fluconazoleObsType.get().getObsDatetime()));
                        }
                        String currentRegimenLine = enhancedArtLineList.getCurrentRegimenLine();
                        if (currentRegimenLine != null) {
                            String currentRegimen = enhancedArtLineList.getCurrentRegimen();
                            String firstLine = "";
                            String secondLine = "";
                            String thirdLine = "";
                            if (currentRegimenLine.contains("3rd")) {
                                thirdLine = currentRegimenLine;
                            } else if (currentRegimenLine.contains("2nd")) {
                                secondLine = currentRegimenLine;
                            } else if (currentRegimenLine.contains("1st")) {
                                firstLine = currentRegimenLine;
                            }
                            Optional<ObsType> currentRegimenLineObsType = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 165708, container, cutOff);
                            String finalThirdLine = thirdLine;
                            String finalSecondLine = secondLine;
                            String finalFirstLine = firstLine;
                            currentRegimenLineObsType.ifPresent(obsType -> {
                                Date obsDate = obsType.getObsDatetime();
                                if (!finalThirdLine.isEmpty()) {
                                    setPrevRegForThirdLine(container, enhancedArtLineList, obsType, obsDate);
                                } else if (!finalSecondLine.isEmpty()) {
                                    setPrevRegForSecondLine(container, enhancedArtLineList, obsType, obsDate);
                                } else if (!finalFirstLine.isEmpty()) {
                                    setPrevRegForFirstLine(container, enhancedArtLineList, obsType, obsDate);
                                }
                            });
                        }
                        Optional<ObsType> arvDrugObsType = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 165724, container, cutOff);
                        arvDrugObsType.ifPresent(obsType -> {
                            if (obsType.getVariableValue() != null) {
                                StringBuilder arvDrugAndStrength = new StringBuilder(obsType.getVariableValue());
                                Optional<ObsType> arvStrengthObsType = container.getMessageData().getObs()
                                        .stream()
                                        .filter(obsType1 -> obsType1.getObsGroupId() == obsType.getObsGroupId() &&
                                                obsType1.getConceptId() == 165725 &&
                                                obsType1.getFormId() == PHARMACY_FORM &&
                                                obsType1.getObsDatetime().before(cutOff) &&
                                                obsType1.getVariableValue() != null &&
                                                obsType1.getVoided() == 0)
                                        .max(Comparator.comparing(ObsType::getObsDatetime));
                                arvStrengthObsType.ifPresent(obsType1 -> arvDrugAndStrength.append("(").append(obsType1.getVariableValue()).append(")"));
                                List<ObsType> arvDrugObsTypeList = container.getMessageData().getObs()
                                        .stream()
                                        .filter(obsType1 -> obsType1.getConceptId() == 165724 &&
                                                obsType1.getFormId() == PHARMACY_FORM &&
                                                obsType1.getObsDatetime().before(cutOff) &&
                                                obsType1.getEncounterId() == obsType.getEncounterId() &&
                                                obsType1.getObsId() != obsType.getObsId() &&
                                                obsType1.getVariableValue() != null &&
                                                obsType1.getVoided() == 0)
                                        .collect(Collectors.toList());
                                if (!arvDrugObsTypeList.isEmpty()) {
                                    arvDrugObsTypeList.forEach(obsType1 -> {
                                        StringBuilder arvDrugAndStrength1 = new StringBuilder(obsType1.getVariableValue());
                                        Optional<ObsType> arvStrengthObsType1 = container.getMessageData().getObs()
                                                .stream()
                                                .filter(obsType2 -> obsType2.getObsGroupId() == obsType1.getObsGroupId() &&
                                                        obsType2.getConceptId() == 165725 &&
                                                        obsType2.getFormId() == PHARMACY_FORM &&
                                                        obsType2.getObsDatetime().before(cutOff) &&
                                                        obsType2.getVariableValue() != null &&
                                                        obsType2.getVoided() == 0)
                                                .max(Comparator.comparing(ObsType::getObsDatetime));
                                        arvStrengthObsType1.ifPresent(obsType2 -> arvDrugAndStrength1.append(" (").append(obsType2.getVariableValue()).append(")"));
                                        arvDrugAndStrength.append(" | ").append(arvDrugAndStrength1);
                                    });
                                }
                                enhancedArtLineList.setArvDrugAndStrengthList(arvDrugAndStrength.toString());
                            }
                        });
                        Optional<ObsType> oiDrugObsType = helperFunctions.getMaxConceptObsIdWithFormId(PHARMACY_FORM, 165727, container, cutOff);
                        oiDrugObsType.ifPresent(obsType -> {
                            if (obsType.getVariableValue() != null) {
                                StringBuilder oiDrugAndStrength = new StringBuilder(obsType.getVariableValue());
                                Optional<ObsType> oiStrengthObsType = container.getMessageData().getObs()
                                        .stream()
                                        .filter(obsType1 -> obsType1.getObsGroupId() == obsType.getObsGroupId() &&
                                                obsType1.getConceptId() == 165725 &&
                                                obsType1.getFormId() == PHARMACY_FORM &&
                                                obsType1.getObsDatetime().before(cutOff) &&
                                                obsType1.getVariableValue() != null &&
                                                obsType1.getVoided() == 0)
                                        .max(Comparator.comparing(ObsType::getObsDatetime));
                                oiStrengthObsType.ifPresent(obsType1 -> oiDrugAndStrength.append(" (").append(obsType1.getVariableValue()).append(")"));
                                List<ObsType> oiDrugObsTypeList = container.getMessageData().getObs()
                                        .stream()
                                        .filter(obsType1 -> obsType1.getConceptId() == 165727 &&
                                                obsType1.getFormId() == PHARMACY_FORM &&
                                                obsType1.getObsDatetime().before(cutOff) &&
                                                obsType1.getEncounterId() == obsType.getEncounterId() &&
                                                obsType1.getObsId() != obsType.getObsId() &&
                                                obsType1.getVariableValue() != null &&
                                                obsType1.getVoided() == 0)
                                        .collect(Collectors.toList());
                                if (!oiDrugObsTypeList.isEmpty()) {
                                    oiDrugObsTypeList.forEach(obsType1 -> {
                                        StringBuilder oiDrugAndStrength1 = new StringBuilder(obsType1.getVariableValue());
                                        Optional<ObsType> oiStrengthObsType1 = container.getMessageData().getObs()
                                                .stream()
                                                .filter(obsType2 -> obsType2.getObsGroupId() == obsType1.getObsGroupId() &&
                                                        obsType2.getConceptId() == 165725 &&
                                                        obsType2.getFormId() == PHARMACY_FORM &&
                                                        obsType2.getObsDatetime().before(cutOff) &&
                                                        obsType2.getVariableValue() != null &&
                                                        obsType2.getVoided() == 0)
                                                .max(Comparator.comparing(ObsType::getObsDatetime));
                                        oiStrengthObsType1.ifPresent(obsType2 -> oiDrugAndStrength1.append(" (").append(obsType2.getVariableValue()).append(")"));
                                        oiDrugAndStrength.append(" | ").append(oiDrugAndStrength1);
                                    });
                                }
                                enhancedArtLineList.setOiDrugListAndStrength(oiDrugAndStrength.toString());
                            }
                        });
                        Optional<ObsType> weightJanObsType = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 5089, container, janCutOff);
                        weightJanObsType.ifPresent(obsType -> {
                            enhancedArtLineList.setWeightAsAtJan2022(obsType.getValueNumeric().doubleValue());
                            enhancedArtLineList.setWeightDateAsAtJan2022(convertToLocalDateViaInstant(obsType.getObsDatetime()));
                        });
                        Optional<ObsType> weightDecObsType = helperFunctions.getMaxConceptObsIdWithFormId(CARE_CARD, 5089, container, cutOff);
                        weightDecObsType.ifPresent(obsType -> {
                            enhancedArtLineList.setWeightAsAtDec2022(obsType.getValueNumeric().doubleValue());
                            enhancedArtLineList.setWeightDateAsAtDec2022(convertToLocalDateViaInstant(obsType.getObsDatetime()));
                        });
                        try {
                            enhancedArtLineListRepository.save(enhancedArtLineList);
                        } catch (Exception e) {
                            try {
                                log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(enhancedArtLineList));
                            } catch (JsonProcessingException ex) {
                                log.info("Error converting enhancedArtLineList to JSON");
                            }
                        }

                    }
                }
            }
        });
        log.info("Finished processing Enhanced Art Line List");
    }

    private void setPrevRegForFirstLine(Container container, EnhancedArtLineList enhancedArtLineList, ObsType obsType, Date obsDate) {
        Optional<ObsType> prevFirstLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        (obsType1.getVariableValue().contains("1st") && obsType1.getValueCoded() != obsType.getValueCoded())
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        setPrevFirstLineRegimen(container, enhancedArtLineList, obsDate, prevFirstLineObsType);
    }

    private void setPrevFirstLineRegimen(Container container, EnhancedArtLineList enhancedArtLineList, Date obsDate, Optional<ObsType> prevFirstLineObsType) {
        if (prevFirstLineObsType.isPresent()) {
            Optional<ObsType> prevReg = helperFunctions.getCurrentRegimen(prevFirstLineObsType.get().getEncounterId(), prevFirstLineObsType.get().getValueCoded(), container, obsDate);
            prevReg.ifPresent(obsType1 -> {
                enhancedArtLineList.setPreviousFirstLineRegimen(obsType1.getVariableValue());
                enhancedArtLineList.setPreviousFirstLineRegimenDate(convertToLocalDateViaInstant(obsType1.getObsDatetime()));
            });
        }
    }

    private void setPrevRegForSecondLine(Container container, EnhancedArtLineList enhancedArtLineList, ObsType obsType, Date obsDate) {
        Optional<ObsType> prevSecondLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        (obsType1.getVariableValue().contains("2nd") && obsType1.getValueCoded() != obsType.getValueCoded())
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        if (prevSecondLineObsType.isPresent()) {
            Optional<ObsType> prevReg = helperFunctions.getCurrentRegimen(prevSecondLineObsType.get().getEncounterId(), prevSecondLineObsType.get().getValueCoded(), container, obsDate);
            prevReg.ifPresent(obsType1 -> {
                enhancedArtLineList.setPreviousSecondLineRegimen(obsType1.getVariableValue());
                enhancedArtLineList.setPreviousSecondLineRegimenDate(convertToLocalDateViaInstant(obsType1.getObsDatetime()));
            });
        }
        Optional<ObsType> prevFirstLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        obsType1.getVariableValue().contains("1st")
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        setPrevFirstLineRegimen(container, enhancedArtLineList, obsDate, prevFirstLineObsType);
    }

    private void setPrevRegForThirdLine(Container container, EnhancedArtLineList enhancedArtLineList, ObsType obsType, Date obsDate) {
        Optional<ObsType> prevThirdLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        (obsType1.getVariableValue().contains("3rd") && obsType1.getValueCoded() != obsType.getValueCoded())
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        if (prevThirdLineObsType.isPresent()) {
            Optional<ObsType> prevReg = helperFunctions.getCurrentRegimen(prevThirdLineObsType.get().getEncounterId(), prevThirdLineObsType.get().getValueCoded(), container, obsDate);
            prevReg.ifPresent(obsType1 -> {
                enhancedArtLineList.setPreviousThirdLineRegimen(obsType1.getVariableValue());
                enhancedArtLineList.setPreviousThirdLineRegimenDate(convertToLocalDateViaInstant(prevThirdLineObsType.get().getObsDatetime()));
            });
        }
        Optional<ObsType> prevSecondLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        obsType1.getVariableValue().contains("2nd")
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        if (prevSecondLineObsType.isPresent()) {
            Optional<ObsType> prevReg = helperFunctions.getCurrentRegimen(prevSecondLineObsType.get().getEncounterId(), prevSecondLineObsType.get().getValueCoded(), container, obsDate);
            prevReg.ifPresent(obsType1 -> {
                enhancedArtLineList.setPreviousSecondLineRegimen(obsType1.getVariableValue());
                enhancedArtLineList.setPreviousSecondLineRegimenDate(convertToLocalDateViaInstant(prevSecondLineObsType.get().getObsDatetime()));
            });
        }
        Optional<ObsType> prevFirstLineObsType = container.getMessageData().getObs()
                .stream()
                .filter(obsType1 -> obsType1.getConceptId() == 165708 &&
                        obsType1.getFormId() == PHARMACY_FORM &&
                        obsType1.getObsDatetime().before(obsDate) &&
                        obsType1.getVoided() == 0 &&
                        obsType1.getVariableValue() != null &&
                        obsType1.getVariableValue().contains("1st")
                ).max(Comparator.comparing(ObsType::getObsDatetime));
        if (prevFirstLineObsType.isPresent()) {
            Optional<ObsType> prevReg = helperFunctions.getCurrentRegimen(prevFirstLineObsType.get().getEncounterId(), prevFirstLineObsType.get().getValueCoded(), container, obsDate);
            prevReg.ifPresent(obsType1 -> {
                enhancedArtLineList.setPreviousFirstLineRegimen(obsType1.getVariableValue());
                enhancedArtLineList.setPreviousFirstLineRegimenDate(convertToLocalDateViaInstant(prevFirstLineObsType.get().getObsDatetime()));
            });
        }
    }

    private LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
