package com.etlservice.schedular.repository.jpa_repository.read;

import com.etlservice.schedular.entities.linelists.FlatFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlatFileRepository extends JpaRepository<FlatFile, Integer> {
}
