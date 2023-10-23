package com.etlservice.schedular.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "error_report")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
public class ErrorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "patient_id")
    private String patientId;

    @Type(type = "jsonb")
    @Column(name = "error_message", columnDefinition = "jsonb")
    private String errorMessage;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "has_critical_error")
    private boolean hasCriticalError;

    @PrePersist
    public void prePersist() {
        dateCreated = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ErrorReport that = (ErrorReport) o;
        return Id != null && Objects.equals(Id, that.Id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
