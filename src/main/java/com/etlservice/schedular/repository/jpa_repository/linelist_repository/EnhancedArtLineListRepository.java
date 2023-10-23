package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.EnhancedArtLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnhancedArtLineListRepository extends JpaRepository<EnhancedArtLineList, Long> {
    Optional<EnhancedArtLineList> findByPatientUuidAndDatimCode(String patientUuid, String datimCode);
}