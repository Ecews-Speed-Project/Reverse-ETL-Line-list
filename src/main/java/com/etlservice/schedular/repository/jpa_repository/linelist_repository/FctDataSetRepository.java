package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.FctDataSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FctDataSetRepository extends JpaRepository<FctDataSet, Long> {
    Optional<FctDataSet> findByPatientUuidAndDatimCode(String uuid, String datimCode);
}
