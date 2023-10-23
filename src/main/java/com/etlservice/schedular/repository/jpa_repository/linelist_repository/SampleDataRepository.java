package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.SampleData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SampleDataRepository extends JpaRepository<SampleData, Long> {
    Optional<SampleData> findByPatientUuidAndDatimCode(String patientUuid, String datimCode);
}