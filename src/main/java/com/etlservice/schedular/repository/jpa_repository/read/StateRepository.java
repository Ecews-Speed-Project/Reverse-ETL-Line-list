package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
}