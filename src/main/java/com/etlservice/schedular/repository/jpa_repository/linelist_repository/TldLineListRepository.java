package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.TldLineList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TldLineListRepository extends JpaRepository<TldLineList, Long> {
}