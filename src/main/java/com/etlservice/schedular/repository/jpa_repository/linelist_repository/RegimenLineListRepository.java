package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.RegimenLineList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegimenLineListRepository extends JpaRepository<RegimenLineList, Long> {
}