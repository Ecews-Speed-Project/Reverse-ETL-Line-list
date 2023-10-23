package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.MortalityLineList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MortalityLineListRepository extends JpaRepository<MortalityLineList, Long> {
}
