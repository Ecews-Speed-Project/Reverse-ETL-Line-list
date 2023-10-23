package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.linelists.Radet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RadetRepository extends JpaRepository<Radet, Integer> {

    List<Radet> findRadetsByArtStartDateBetween(Date start, Date end);

    //@Query(value="select id, patientid from radet r where r.current_age_years between  :ageStart and :ageStop ", nativeQuery=true)
    List<Radet> findRadetsByCurrentAgeYearsBetween(Integer ageStart, Integer ageStop);
    List<Radet> findRadetsByCurrentAgeYearsGreaterThanEqual( Integer age);
    List<Radet> findRadetsByCurrentAgeYearsLessThan( Integer age);
    List<Radet> findRadetsBySexEquals(String sex);
    List<Radet> findRadetsByPatientOutcomeEquals(String status);
    List<Radet> findRadetsByCareEntryPointEquals(String status);
    List<Radet> findRadetsByDateReturnedToCareIsNotNull();


}
