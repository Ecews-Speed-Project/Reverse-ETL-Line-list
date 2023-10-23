package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.CustomLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface CustomLineListRepository extends JpaRepository<CustomLineList, Long> {
    Optional<CustomLineList> findByIdentifierAndVisit(String identifier, Date visit);
}
