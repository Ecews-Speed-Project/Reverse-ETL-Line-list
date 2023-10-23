package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.BaselineViralLoadLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaselineViralLoadLineListRepository extends JpaRepository<BaselineViralLoadLineList, Long> {
    Optional<BaselineViralLoadLineList> findByPatientUuidAndDatimCode(String patientUuid, String datimCode);
}