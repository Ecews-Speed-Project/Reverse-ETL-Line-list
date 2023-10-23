package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.CustomArtLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomArtLineListRepository extends JpaRepository<CustomArtLineList, Long> {
    Optional<CustomArtLineList> findByPatientUuidAndDatimCodeAndQuarter(String patientUuid, String datimCode, String quarter);
}