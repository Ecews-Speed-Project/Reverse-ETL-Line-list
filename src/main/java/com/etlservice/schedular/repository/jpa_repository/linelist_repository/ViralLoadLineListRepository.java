package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.ViralLoadLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ViralLoadLineListRepository extends JpaRepository<ViralLoadLineList, Long> {
    Optional<ViralLoadLineList> findByPatientUuidAndDatimCodeAndVisitDate(String patientUuid, String datimCode, LocalDate visitDate);
}