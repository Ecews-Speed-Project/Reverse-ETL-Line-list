package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.HtsLinelist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HtsLineListRepository extends JpaRepository<HtsLinelist, Integer> {
    Optional<HtsLinelist> findByPatientIdAndDatimCode(String patientId, String datimCode);
}
