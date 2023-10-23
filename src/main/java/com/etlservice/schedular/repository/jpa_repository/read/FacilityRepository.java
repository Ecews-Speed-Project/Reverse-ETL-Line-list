package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, String> {
    Facility findFacilityByDatimCode(String datimCode);
    @Query(value = "SELECT datim_code FROM facility WHERE state_id = 1", nativeQuery = true)
    List<String> findFctFacilitiesDatimCodes();

    @Query(value = "SELECT datim_code FROM facility WHERE state_id = :stateId", nativeQuery = true)
    List<String> findStateFacilitiesDatimCodes(long stateId);
}
