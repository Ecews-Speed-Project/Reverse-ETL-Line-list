package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.BiometricLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiometricLineListRepository extends JpaRepository<BiometricLineList, Long> {
    Optional<BiometricLineList> findByPatientUuidAndDatimCode(String patientUuid, String datimCode);
}