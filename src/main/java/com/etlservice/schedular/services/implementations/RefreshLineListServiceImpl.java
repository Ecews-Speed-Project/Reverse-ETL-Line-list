package com.etlservice.schedular.services.implementations;

import com.etlservice.schedular.entities.linelists.ArtLinelist;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.entities.linelists.LineListTracker;
import com.etlservice.schedular.enums.LineListStatus;
import com.etlservice.schedular.enums.LineListType;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.ArtLineListRepository;
import com.etlservice.schedular.repository.jpa_repository.read.FacilityRepository;
import com.etlservice.schedular.repository.jpa_repository.linelist_repository.LineListTrackerRepository;
import com.etlservice.schedular.services.RefreshLineListService;
import com.etlservice.schedular.utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.etlservice.schedular.enums.LineListStatus.PROCESSING;
import static com.etlservice.schedular.enums.LineListType.QUARTERLY;
import static com.etlservice.schedular.services.implementations.ArtLineListGeneratorServiceImpl.setPatientAgeRangeOnArtLineList;
import static com.etlservice.schedular.utils.ConstantsUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshLineListServiceImpl implements RefreshLineListService {
    private final HelperFunctions helperFunctions;
    private final ArtLineListRepository artLineListRepository;
    private final LineListTrackerRepository lineListTrackerRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public String refreshQuarterlyArtLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), QUARTERLY.name());
        LocalDate currentDate = LocalDate.now();
        String currentQuarter = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
        log.info("Current quarter: {}", currentQuarter);
        Date previousQuarterCutOff = helperFunctions.getCutOffDate(currentDate);
        String previousQuarter = helperFunctions
                .getQuarterCodeFromLocalDate(previousQuarterCutOff.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        log.info("Previous quarter: {}", previousQuarter);
        int page = lineListTracker.getCurrentPage();
        int size = lineListTracker.getPageSize();
        Page<ArtLinelist> artLineListsForPreviousQuarter = artLineListRepository
                .findArtLinelistsByQuarter(previousQuarter, PageRequest.of(page, size));
        log.info("Total pages: {}", artLineListsForPreviousQuarter.getTotalPages());
        log.info("Total elements: {}", artLineListsForPreviousQuarter.getTotalElements());
        List<ArtLinelist> artLineLists = artLineListsForPreviousQuarter.getContent();
        buildLineListForCurrentQuarter(currentDate, currentQuarter, artLineLists);
        helperFunctions.updateLineListTracker(lineListTracker, page, artLineListsForPreviousQuarter, artLineLists);
        log.info("done with page {} of {}", page + 1, artLineListsForPreviousQuarter.getTotalPages());
        while (artLineListsForPreviousQuarter.hasNext()) {
            page++;
            artLineListsForPreviousQuarter = artLineListRepository
                    .findArtLinelistsByQuarter(previousQuarter, PageRequest.of(page, size));
            artLineLists = artLineListsForPreviousQuarter.getContent();
            buildLineListForCurrentQuarter(currentDate, currentQuarter, artLineLists);
            helperFunctions.updateLineListTracker(lineListTracker, page, artLineListsForPreviousQuarter, artLineLists);
            log.info("done with page {} of {}", page + 1, artLineListsForPreviousQuarter.getTotalPages());
            if (artLineListsForPreviousQuarter.isLast()) {
                log.info("No more results");
                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTrackerRepository.save(lineListTracker);
            }
        }
        log.info("done with quarterly art line list");
        return "done with quarterly art line list";
    }

    @Override
    public String refreshDailyArtLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), LineListType.DAILY.name());
        LocalDate currentDate = LocalDate.now();
        log.info("Current date: {}", currentDate);
        String currentQuarter = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
        log.info("Current quarter: {}", currentQuarter);
        int page = lineListTracker.getCurrentPage();
        int size = lineListTracker.getPageSize();
        Page<ArtLinelist> artLineListsForCurrentQuarter = artLineListRepository
                .findArtLinelistsByQuarter(currentQuarter, PageRequest.of(page, size, Sort.by("id").ascending()));
        log.info("Total pages: {}", artLineListsForCurrentQuarter.getTotalPages());
        log.info("Total elements: {}", artLineListsForCurrentQuarter.getTotalElements());
        List<ArtLinelist> artLineLists = artLineListsForCurrentQuarter.getContent();
        log.info("artLineLists size: {}", artLineLists.size());
        for (ArtLinelist artLinelist : artLineLists) {
            updateArtLineList(currentDate, artLinelist);
        }
        helperFunctions.updateLineListTracker(lineListTracker, page, artLineListsForCurrentQuarter, artLineLists);
        log.info("done with page {} of {}", page + 1, artLineListsForCurrentQuarter.getTotalPages());
        while (artLineListsForCurrentQuarter.hasNext()) {
            page++;
            artLineListsForCurrentQuarter = artLineListRepository.findArtLinelistsByQuarter(currentQuarter, PageRequest.of(page, size));
            artLineLists = artLineListsForCurrentQuarter.getContent();
            log.info("artLineLists size: {}", artLineLists.size());
            for (ArtLinelist artLinelist : artLineLists) {
                updateArtLineList(currentDate, artLinelist);
            }
            log.info("done with page {} of {}", page + 1, artLineListsForCurrentQuarter.getTotalPages());
            helperFunctions.updateLineListTracker(lineListTracker, page, artLineListsForCurrentQuarter, artLineLists);
            if (artLineListsForCurrentQuarter.isLast()) {
                log.info("No more results");
                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTrackerRepository.save(lineListTracker);
            }
        }
        log.info("done with refreshDailyArtLineList");
        return "done with refreshDailyArtLineList";
    }

    @Override
    public void cleanArtLineList() {
        LineListTracker lineListTracker = helperFunctions.getLineListTracker(PROCESSING.name(), "CLEAN UP");
        LocalDate currentDate = LocalDate.now();
        log.info("Current date: {}", currentDate);
        String currentQuarter = helperFunctions.getQuarterCodeFromLocalDate(currentDate);
        log.info("Current quarter: {}", currentQuarter);
        int page = lineListTracker.getCurrentPage();
        int size = lineListTracker.getPageSize();
        while (true) {
            log.info("Page: {}", page);
            Pageable pageable = PageRequest.of(page, size);
            Page<ArtLinelist> artLineListsForCurrentQuarter = artLineListRepository
                    .findArtLinelistsByQuarter(currentQuarter, pageable);
            if (!artLineListsForCurrentQuarter.hasContent()) {
                log.info("No more results");
                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
                lineListTracker.setDateCompleted(LocalDateTime.now());
                lineListTrackerRepository.save(lineListTracker);
                break;
            }
            List<ArtLinelist> artLineLists = artLineListsForCurrentQuarter.getContent();
            artLineLists.forEach(artLinelist -> {
                String datimCode = artLinelist.getDatimCode();
                String patientUuid = artLinelist.getPatientUuid();
                if (!patientUuid.contains(datimCode)) {
                    String newPatientUuid = patientUuid + "-" + datimCode;
                    Optional<ArtLinelist> optionalArtLinelist = artLineListRepository.findByPatientUuidAndQuarterAndDatimCode(newPatientUuid, currentQuarter, datimCode);
                    if (optionalArtLinelist.isPresent()) {
                        artLineListRepository.delete(artLinelist);
                        log.info("Deleted duplicate art line list with patient uuid: {}", patientUuid);
                    } else {
                        artLinelist.setPatientUuid(newPatientUuid);
                        artLineListRepository.save(artLinelist);
                        log.info("Updated art line list with patient uuid: {} with {}", patientUuid, newPatientUuid);
                    }
                }
            });
            helperFunctions.updateLineListTracker(lineListTracker, ++page, artLineListsForCurrentQuarter, artLineLists);
            log.info("done with page {} of {}", page, artLineListsForCurrentQuarter.getTotalPages());
        }
    }

    private void updateArtLineList(LocalDate currentDate, ArtLinelist artLineList) {
        long currentAgeYears = 0;
        long currentAgeMonths = 0;
        long monthsOnArt = 0;
        String status = "";
        Facility facility = facilityRepository.findFacilityByDatimCode(artLineList.getDatimCode());
        if (artLineList.getDateOfBirth() != null) {
            LocalDate dob = artLineList.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            currentAgeYears = ChronoUnit.YEARS.between(dob, currentDate);
            if (currentAgeYears < 5) {
                currentAgeMonths = ChronoUnit.MONTHS.between(dob, currentDate);
            }
        }
        if (artLineList.getArtStartDate() != null && artLineList.getLastPickupDate() != null) {
            LocalDate artStartDate = artLineList.getArtStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate lastPickupDate = artLineList.getLastPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysOfArvRefill = artLineList.getDaysOfArvRefil() != null ? artLineList.getDaysOfArvRefil() : 0;
            String patientOutcome = artLineList.getPatientOutcome();
            LocalDate nextPickUpDate = lastPickupDate.plusDays(daysOfArvRefill + 28);
            long daysToNextPickup = ChronoUnit.DAYS.between(currentDate, nextPickUpDate);
            if (daysToNextPickup >= 0 && patientOutcome == null) {
                status = ACTIVE_STATUS;
            } else {
                status = INACTIVE_STATUS;
            }
            if (status.equals(ACTIVE_STATUS)) {
                monthsOnArt = ChronoUnit.MONTHS.between(artStartDate, currentDate);
            } else {
                monthsOnArt = ChronoUnit.MONTHS.between(artStartDate, nextPickUpDate);
            }
        }
        artLineList.setFacilityName(facility != null ? facility.getFacilityName() : artLineList.getFacilityName());
        artLineList.setCurrentAgeYrs((int) currentAgeYears);
        artLineList.setCurrentAgeMonths((int) currentAgeMonths);
        artLineList.setMonthsOnArt(monthsOnArt);
        artLineList.setCurrentArtStatus(status);
        setPatientAgeRangeOnArtLineList(artLineList);
        if (artLineList.getSex().equalsIgnoreCase(FULL_GENDER_MALE))
            artLineList.setSex(SHORT_GENDER_MALE);
        if (artLineList.getSex().equalsIgnoreCase(FULL_GENDER_FEMALE))
            artLineList.setSex(SHORT_GENDER_FEMALE);
        artLineListRepository.save(artLineList);
    }

    private void buildLineListForCurrentQuarter(LocalDate currentDate, String currentQuarter, List<ArtLinelist> artLineLists) {
        artLineLists.forEach(artLineList -> {
            Optional<ArtLinelist> artLineListOptional = artLineListRepository.findByPatientUuidAndQuarterAndDatimCode(artLineList.getPatientUuid(), currentQuarter, artLineList.getDatimCode());
            if (!artLineListOptional.isPresent()) {
                ArtLinelist newArtLineList = new ArtLinelist(artLineList);
                newArtLineList.setQuarter(currentQuarter);
                newArtLineList.setArtStatusPreviousQuarter(artLineList.getCurrentArtStatus());
                newArtLineList.setLastPickupDatePreviousQuarter(artLineList.getLastPickupDate());
                newArtLineList.setDrugDurationPreviousQuarter(artLineList.getDaysOfArvRefil() == null ? null : artLineList.getDaysOfArvRefil().doubleValue());
                newArtLineList.setPatientOutcomePreviousQuarter(artLineList.getPatientOutcome());
                newArtLineList.setPatientOutcomeDatePreviousQuarter(artLineList.getPatientOutcomeDate());
                updateArtLineList(currentDate, newArtLineList);
            }
        });
    }
}
