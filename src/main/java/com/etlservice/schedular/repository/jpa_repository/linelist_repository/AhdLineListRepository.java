package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.AhdLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AhdLineListRepository extends JpaRepository<AhdLineList, Long> {
    Optional<AhdLineList> findByPatientUniqueIdAndDatimCode(String patientUniqueId, String datimCode);
}