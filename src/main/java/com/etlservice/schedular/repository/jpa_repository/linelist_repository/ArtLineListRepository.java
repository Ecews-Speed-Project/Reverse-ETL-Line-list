/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.repository.jpa_repository.linelist_repository;

import com.etlservice.schedular.entities.linelists.ArtLinelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 * @author MORRISON.I
 */
@Repository
public interface ArtLineListRepository extends JpaRepository<ArtLinelist, Long>{
    Optional<ArtLinelist> findByPatientUuidAndQuarterAndDatimCode(String patientUuid, String quarter, String datimCode);
    Page<ArtLinelist> findArtLinelistsByQuarter(String quarter, Pageable pageable);
}
