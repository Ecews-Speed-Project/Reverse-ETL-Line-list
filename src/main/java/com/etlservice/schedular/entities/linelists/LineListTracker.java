package com.etlservice.schedular.entities.linelists;

import com.etlservice.schedular.entities.BaseClass;
import lombok.*;

import javax.persistence.Entity;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LineListTracker extends BaseClass {
    private long totalPatients;
    private long totalPatientsProcessed;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private String status;
    private LocalDateTime dateStarted;
    private LocalDateTime dateCompleted;
    private String lineListType;
}
