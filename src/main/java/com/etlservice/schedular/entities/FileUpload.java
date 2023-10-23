/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author MORRISON.I
 */
@Data
@Entity
@Table(name = "file_upload")
@NamedQuery(name = "FileUpload.findAll", query = "SELECT f FROM FileUpload f")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class FileUpload implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "file_upload_id")
    private Long fileuploadId;
    @Column(name = "consumer_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date consumerDate;
    @Type(type = "jsonb")
    @Column(name = "data_validation_report", columnDefinition = "jsonb")
    private String dataValidationReport;
    @Column(name = "deduplication_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deduplicationDate;
    @Column(name = "etl_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date etlDate;
    @Column(name = "facility_datimcode")
    private String facilityDatimcode;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fileTimestamp;
    @Lob
    @Column(name = "patient_uuid")
    private String patientUuid;
    @Type(type = "jsonb")
    @Column(name = "schema_validation_report", columnDefinition = "jsonb")
    private String schemaValidationReport;
    @Column(name = "status")
    private String status;
    @Column(name = "upload_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;
    @Column(name = "validator_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validatorDate;
    @Column(name = "has_critical_error")
    private boolean hasCriticalError;
    @ManyToOne
    @JoinColumn(name = "file_batch_id", referencedColumnName = "file_batch_id")
    private FileBatch fileBatchId;

    public FileUpload() {
    }

    public FileUpload(Long fileuploadId) {
        this.fileuploadId = fileuploadId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fileuploadId != null ? fileuploadId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FileUpload)) {
            return false;
        }
        FileUpload other = (FileUpload) object;
        return (this.fileuploadId != null || other.fileuploadId == null) &&
                (this.fileuploadId == null || this.fileuploadId.equals(other.fileuploadId));
    }

    @Override
    public String toString() {
        return "com.etlservice.schedular.entities.FileUpload[ fileuploadId=" + fileuploadId + " ]";
    }
    
}
