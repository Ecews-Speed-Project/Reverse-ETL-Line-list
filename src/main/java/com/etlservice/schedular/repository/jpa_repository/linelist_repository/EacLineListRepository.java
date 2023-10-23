package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.EacLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EacLineListRepository extends JpaRepository<EacLineList, Long> {
    Optional<EacLineList> findByPatientUuidAndDatimCode(String patientUuid, String datimCode);
}
