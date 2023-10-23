package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.LineListTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineListTrackerRepository extends JpaRepository<LineListTracker, Long> {
    Optional<LineListTracker> findByStatusAndLineListType(String status, String lineListType);
}
