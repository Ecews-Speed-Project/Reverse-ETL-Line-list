package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.linelists.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, Integer> {
}
