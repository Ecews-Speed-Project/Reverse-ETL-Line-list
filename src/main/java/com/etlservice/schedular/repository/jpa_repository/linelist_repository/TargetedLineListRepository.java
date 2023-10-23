package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.TargetedLineList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TargetedLineListRepository extends JpaRepository<TargetedLineList, Long> {
    Optional<TargetedLineList> findFirstByArtUniqueIdAndDatimCode(String artUniqueId, String datimCode);
}