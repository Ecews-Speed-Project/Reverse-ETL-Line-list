package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.ErrorReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ErrorReportRepository extends JpaRepository<ErrorReport, Long> {
    Optional<ErrorReport> findByPatientId(String patientId);
}
