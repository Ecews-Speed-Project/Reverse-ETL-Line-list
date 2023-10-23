/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.utils;

import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.model.*;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.LineListTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.etlservice.schedular.utils.ConstantsUtils.*;

/**
 *
 * @author MORRISON.I
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelperFunctions {
    private final LineListTrackerRepository lineListTrackerRepository;

    public Optional<String> returnIdentifiers(int idType, Container container) {

        return container.getMessageData().getPatientIdentifiers()
                .stream()
                .filter(patientIdentifierType ->
                        patientIdentifierType.getIdentifierType() == idType &&
                        patientIdentifierType.getVoided() == 0 &&
                        patientIdentifierType.getIdentifier() != null)
                .map(PatientIdentifierType::getIdentifier)
                .findAny();
    }

    public Long getAgeAtStartOfARTYears(Container container, Date cutOff) {

        long ageAtStartOfARTYears = 0;
        try {
            Date birthDate = container.getMessageData().getDemographics().getBirthdate() != null ?
                    container.getMessageData().getDemographics().getBirthdate() : null;
            if (birthDate == null) {
                return ageAtStartOfARTYears;
            }

            LocalDate start = convertDate(birthDate);
            Date artStartDate;

            Optional<Date> optionalArtStartDate = getStartOfArt(container, cutOff);
            if (optionalArtStartDate.isPresent()) {
                artStartDate = optionalArtStartDate.get();
                LocalDate stop = convertDate(artStartDate);
                long years = ChronoUnit.YEARS.between(start, stop);
                ageAtStartOfARTYears = (int) years;
            }
        } catch (Exception e) {
            log.error("age at start of art years {}",e.getMessage());
        }
        return ageAtStartOfARTYears;
    }

    public Long getAgeAtStartOfARTMonths(Container container, Date cutOff) {

        long ageAtStartOfARTYears = 0;
        try {
            Date birthDate = container.getMessageData().getDemographics().getBirthdate() != null ?
                    container.getMessageData().getDemographics().getBirthdate() : null;
            if (birthDate == null) {
                return ageAtStartOfARTYears;
            }
            LocalDate start = convertDate(birthDate);
            Date artStartDate;

            Optional<Date> optionalArtStartDate = getStartOfArt(container, cutOff);
            if (optionalArtStartDate.isPresent()) {
                artStartDate = optionalArtStartDate.get();
                LocalDate stop = convertDate(artStartDate);
                long months = ChronoUnit.MONTHS.between(start, stop);

                ageAtStartOfARTYears = (int) months;
            }

        } catch (Exception e) {
            log.error("age at start of art months {}",e.getMessage());
        }
        return ageAtStartOfARTYears;
    }

    public Optional<Date> getStartOfArt(Container container, Date cutOff) {
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getConceptId() == 159599 &&
                                (obsType.getFormId() == ART_COMMENCEMENT_FORM || obsType.getFormId() == ENROLLMENT_FORM) &&
                                obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff) &&
                        obsType.getValueDatetime() != null)
                .map(ObsType::getValueDatetime)
                .findAny();
    }

    public static LocalDate convertDate(Date date) {

        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

        return zdt.toLocalDate();
    }

    public Optional<ObsType> getMaxObsByConceptID(int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime() != null &&
                        obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getMinObsByConceptID(int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime() != null &&
                        obsType.getObsDatetime().before(cutOff))
                .min(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Long getMonthsOnArt(Container container, Date lastPickupDate, Date cutOff) {
        long monthsOnArt = 0;

        try {
            Date artStartDate = getArtStartDate(container, cutOff);

            if (artStartDate != null && lastPickupDate != null) {
                LocalDate start = convertDate(artStartDate);
                LocalDate stop = convertDate(lastPickupDate);
                monthsOnArt = ChronoUnit.MONTHS.between(start, stop);
            }
        } catch (Exception e) {
            log.error("months on art {}",e.getMessage());
        }
        return monthsOnArt;
    }

    public Date getArtStartDate(Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getConceptId() == 159599 &&
                                (obsType.getFormId() == 56 || obsType.getFormId() == 23) &&
                                obsType.getVoided() == 0 &&
                                obsType.getValueDatetime() != null &&
                                obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime))
                .map(ObsType::getValueDatetime)
                .orElse(null);
    }

    public Date getMaxEncounterDateTime(int formID, Container container, Date cutOff) {
        Date encounterDate = null;
        try {
            List<EncounterType> encounterTypeList = container.getMessageData().getEncounters();
            encounterDate = encounterTypeList.stream()
                    .filter(encounterType -> encounterType.getFormId() == formID &&
                            encounterType.getVoided() == 0 &&
                            encounterType.getEncounterDatetime() != null &&
                            encounterType.getEncounterDatetime().before(cutOff))
                    .map(EncounterType::getEncounterDatetime)
                    .max(Date::compareTo).orElse(null);
        } catch (Exception e) {
            log.error("Max encounter {}",e.getMessage());
        }
        return encounterDate;
    }

    public Date getEnrollmentDate(Container container) {
        return container.getMessageData().getPatientPrograms()
                .stream()
                .filter(patientProgramType -> patientProgramType.getProgramId() == 1 &&
                        patientProgramType.getVoided() == 0)
                .map(PatientProgramType::getDateEnrolled)
                .max(Date::compareTo)
                .orElse(null);
    }

    public Optional<Date> getMinEncounterDateTime(int formID, Container container) {
        Date encounterDate = null;
        try {
            List<EncounterType> obsTypeList = container.getMessageData().getEncounters();
            encounterDate = obsTypeList.stream()
                    .filter(encounterType -> encounterType.getFormId() == formID)
                    .map(EncounterType::getEncounterDatetime)
                    .min(Date::compareTo).orElse(null);
        } catch (Exception e) {
            log.error("min encounter {}",e.getMessage());
        }
        return Optional.ofNullable(encounterDate);
    }

    public Optional<Date> getMaxVisitDate(Container container, Date cutOff) {
        List<Integer> formIds = new ArrayList<>(Arrays.asList(22,56,14,69,23,44,74,53,21,73,20,27,67));
        Date lastVisitDate = null;
        try {
            List<EncounterType> obsTypeList = container.getMessageData().getEncounters();
            lastVisitDate = obsTypeList.stream()
                    .filter(encounterType -> formIds.contains(encounterType.getFormId()) &&
                            encounterType.getVoided() == 0 &&
                            encounterType.getEncounterDatetime().before(cutOff))
                    .max(Comparator.comparing(EncounterType::getEncounterDatetime))
                    .map(EncounterType::getEncounterDatetime)
                    .orElse(null);
        }catch(Exception e){
            log.error("max visit{}",e.getMessage());
        }
        return Optional.ofNullable(lastVisitDate);
    }

    public Optional<ObsType> getMaxObsByConceptAndEncounterTypeID(int encounterTypeId, int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getEncounterType() == encounterTypeId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getMinObsByConceptAndEncounterTypeID(int encounterTypeId, int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getEncounterType() == encounterTypeId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .min(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getInitialRegimen(int encounterId, int valueCoded, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getEncounterId() == encounterId &&
                        obsType.getConceptId() == valueCoded &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .min(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getCurrentRegimen(int encounterId, int valueCoded, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getEncounterId() == encounterId &&
                        obsType.getConceptId() == valueCoded &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Integer getCurrentAge(Container container, String type, Date cutOff) {
        int currentAge = 0;
        long years;
        long months;
        try {
            Date birthDate = container.getMessageData().getDemographics().getBirthdate();
            if (birthDate == null) {
                return currentAge;
            }

            LocalDate start = convertDate(birthDate);
            LocalDate stop = convertDate(cutOff);
            if(type.equals("YEARS")) {
                years = ChronoUnit.YEARS.between(start, stop);
                currentAge = (int) years;
            } else {
                months = ChronoUnit.MONTHS.between(start, stop);
                currentAge = (int) months;
            }

        }catch (Exception e){
            log.error("current age {}",e.getMessage());
        }
        return currentAge;
    }

    public String getCurrentArtStatus(Date lastPickupDate, Long daysOfARVRefil, Date cutOff, String patientOutcome) {
        if (lastPickupDate != null && daysOfARVRefil != null) {
            long ltfunumber = daysOfARVRefil + 28;
            LocalDate pickup = convertDate(lastPickupDate).plusDays(ltfunumber);
            long daysDiff = ChronoUnit.DAYS.between(convertDate(cutOff), pickup);
            if (daysDiff >= 0 && patientOutcome == null) {
                return "Active";
            }
        }
        return "InActive";
    }

    public String getBiometricCaptured(Container container) {
        String biometricCaptured = null;
        try {
            if (container.getMessageData().getPatientBiometrics() != null &&
                    !container.getMessageData().getPatientBiometrics().isEmpty()) {
                biometricCaptured = "Yes";
            } else
                biometricCaptured = "No";
        }catch (Exception e){
            log.error("biometric captured {}",e.getMessage());
        }
        return biometricCaptured;
    }

    public Date getBiometricCaptureDate(Container container) {
        Date biometricCapturedDate = null;
        try{
            if (!container.getMessageData().getPatientBiometrics().isEmpty())
                biometricCapturedDate = container.getMessageData().getPatientBiometrics().get(0).getDateCreated();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return biometricCapturedDate;
    }

    public Optional<ObsType> getInitialRegimenLine(int adultConceptID, int childConceptId, Container container) {
        ObsType obs = null;
        try {
            List<ObsType> obsList = container.getMessageData().getObs();
            obs = obsList.stream()
                    .filter(obsType -> (obsType.getConceptId() == adultConceptID || obsType.getConceptId() == childConceptId) &&
                            obsType.getVoided() == 0 &&
                            obsType.getFormId() == PHARMACY_FORM)
                    .min(Comparator.comparing(ObsType::getObsDatetime))
                    .orElse(null);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return Optional.ofNullable(obs);
    }

    public Optional<Date> getLastINHDispensedDate(int conceptId, int valueCoded,int formID,
                                                         Container container, Date cutOff)
    {
        Date lastINHDispenseDate = null;
        try {
            List<ObsType> lastINHDispensedDateList = container.getMessageData().getObs();
            Map<Date, ObsType> lastINHDispensedDateMap = new HashMap<>();

            for (ObsType obsType : lastINHDispensedDateList) {
                if (obsType.getConceptId() == conceptId && obsType.getFormId() == formID &&
                        obsType.getValueCoded() == valueCoded && obsType.getVoided() == 0  &&
                        obsType.getObsDatetime().before(cutOff))

                    lastINHDispensedDateMap.put(obsType.getObsDatetime(), obsType);
            }
            List<Date> tbStatusDateList = new ArrayList<>(lastINHDispensedDateMap.keySet());
            if (!tbStatusDateList.isEmpty()) {
                Date latestDate = Collections.max(tbStatusDateList);
                lastINHDispenseDate = lastINHDispensedDateMap.get(latestDate).getObsDatetime();
            }
        }catch (Exception e){
            log.error("last inh dispense date {}",e.getMessage());
        }
        return Optional.ofNullable(lastINHDispenseDate);
    }

    public String getValidCapture(Container container) {
        String validCapture = null;
        try {
            List<PatientBiometricType> patientBiometricTypeList =
                    container.getMessageData().getPatientBiometrics() != null ?
                            container.getMessageData().getPatientBiometrics() : new ArrayList<>();
            for (PatientBiometricType patientBiometricType : patientBiometricTypeList) {
                if (patientBiometricType.getTemplate() != null && patientBiometricType.getTemplate().startsWith("Rk1S"))
                    validCapture = "Yes";
                else {
                    validCapture = "No";
                    break;
                }
            }
            if (container.getMessageData().getPatientBiometrics().isEmpty())
                validCapture = "No";
        }catch (Exception e){
            log.error("valid capture {}",e.getMessage());
        }
        return validCapture;
    }

    public Optional<ObsType> getMaxObsByEncounterIdAndConceptId(int encounterId, int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getEncounterId() == encounterId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getDaysOfArv(int obsId, int conceptId, Container container, Date cutOff) {

        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getObsGroupId() == obsId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getDaysOfArvPreviousQuarter(int obsId, int conceptId, Container container, LocalDate localDate) {
        Date date = getCutOffDate(localDate);
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getObsGroupId() == obsId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getObsDatetime().before(date) &&
                        obsType.getVoided() == 0)
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getMinConceptObsIdWithFormId (int formId, int conceptId, Container container, Date cutOff) {
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getFormId() == formId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOff))
                .min(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getMaxConceptObsIdWithFormId (int formId, int conceptId, Container container, Date date) {
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getFormId() == formId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(date))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public Optional<ObsType> getMaxConceptObsIdWithFormId (int formId, int conceptId, Container container, LocalDate localDate) {
        Date cutOffDate = getCutOffDate(localDate);
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> obsType.getFormId() == formId &&
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0 &&
                        obsType.getObsDatetime().before(cutOffDate))
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    /*
    *This method returns a list of the cutoff dates of different quarters (Q1 - Q4)
    * from the current date
     */
    public List<LocalDate> getCutOffDatesForEachQuarter(LocalDate currentDate) {
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        LocalDate q1 = LocalDate.of(year, 1, 1);
        LocalDate q2;
        LocalDate q3;
        LocalDate q4;
        if (month <= 3) {
            q2 = LocalDate.of(year - 1, 4, 1);
            q3 = LocalDate.of(year - 1, 7, 1);
            q4 = LocalDate.of(year - 1, 10, 1);
        } else {
            q2 = LocalDate.of(year, 4, 1);
            if (month <= 6) {
                q3 = LocalDate.of(year - 1, 7, 1);
                q4 = LocalDate.of(year - 1, 10, 1);
            } else {
                q3 = LocalDate.of(year, 7, 1);
                q4 = LocalDate.of(month <= 9 ? year - 1 : year, 10, 1);
            }
        }
        return new ArrayList<>(Arrays.asList(currentDate, q1, q2, q3, q4));
    }

    public Date getCutOffDate(LocalDate localDate) {
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        TreeMap<Integer, Date> rangeTreeMap = new TreeMap<>();

        rangeTreeMap.put(0, Date.from(LocalDate.of(year - 1, 12, 31)
                .atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));

        rangeTreeMap.put(3, Date.from(LocalDate.of(year, 3, 31)
                .atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));

        rangeTreeMap.put(6, Date.from(LocalDate.of(year, 6, 30)
                .atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));

        rangeTreeMap.put(9, Date.from(LocalDate.of(year, 9, 30)
                .atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));

        return rangeTreeMap.get(rangeTreeMap.lowerKey(month));
    }

    public Optional<EncounterType> getHtsVisitDate(Container container){
        return container.getMessageData().getEncounters()
                .stream()
                .filter(encounterType ->
                        (encounterType.getFormId() == 10 || encounterType.getFormId() == 75)
                        && encounterType.getVoided() == 0)
                .max(Comparator.comparing(EncounterType::getEncounterDatetime));
    }

    public Optional<ObsType> getMaxConceptObs(int conceptId, Container container) {
        return container.getMessageData().getObs()
                .stream().filter(obsType ->
                        obsType.getConceptId() == conceptId &&
                        obsType.getVoided() == 0)
                .max(Comparator.comparing(ObsType::getObsDatetime));
    }

    public long getScreeningScore(Container container, List<Integer> conceptIds, List<Integer> valueCodes) {
        return container.getMessageData().getObs()
                .stream()
                .filter(obsType -> conceptIds.contains(obsType.getConceptId())
                                && valueCodes.contains(obsType.getValueCoded())
                                && obsType.getVoided() == 0)
                .count();
    }

    public String getQuarterCodeFromLocalDate(LocalDate currentDate) {
        String quarter;
        int month = currentDate.getMonthValue();
        int year = currentDate.getYear();
        String fy = String.format("FY%sQ", String.valueOf(year).substring(2));
        switch (month <= 3 ? 3 : month <= 6 ? 6 : month <= 9 ? 9 : 12) {
            case 3:
                quarter = fy + 2;
                break;
            case 6:
                quarter = fy + 3;
                break;
            case 9:
                quarter = fy + 4;
                break;
            default:
                quarter = String.format("FY%sQ%d", String.valueOf(year + 1).substring(2), 1);
        }
        return quarter;
    }

    public LineListTracker getLineListTracker (String status, String type) {
        LineListTracker lineListTracker = lineListTrackerRepository.findByStatusAndLineListType(status, type)
                .orElse(null);
        if (lineListTracker == null) {
            lineListTracker = LineListTracker.builder()
                    .status(status)
                    .currentPage(0)
                    .dateStarted(LocalDateTime.now())
                    .pageSize(500)
                    .lineListType(type)
                    .build();
            lineListTracker = lineListTrackerRepository.save(lineListTracker);
        }
        return lineListTracker;
    }

    public LineListTracker updateLineListTracker(LineListTracker lineListTracker, int page, Page<?> containersPage, List<?> containers) {
        long totalPatients = containersPage.getTotalElements();
        lineListTracker.setCurrentPage(page);
        lineListTracker.setTotalPatientsProcessed(lineListTracker.getTotalPatientsProcessed() + containers.size());
        lineListTracker.setTotalPatients(totalPatients);
        lineListTracker.setTotalPages(containersPage.getTotalPages());
        return lineListTrackerRepository.save(lineListTracker);
    }

    public LineListTracker saveLineListTracker(LineListTracker lineListTracker) {
        if (lineListTracker != null)
            return lineListTrackerRepository.save(lineListTracker);
        else
            log.error("LineListTracker is null");
        return null;
    }
}
