package com.etlservice.schedular.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="file_batch")
public class FileBatch implements Serializable {
    private static final long serialVersionUID = 2405172041950251807L;
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    @Column(name = "file_batch_id")
    private Long fileBatchId;
    @Column(name = "zip_file_name")
    private String zipFileName;
    @Column(name = "batch_number")
    private String batchNumber;
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    private String status;
    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName="id")
    private User user;
    @ManyToOne
    @JoinColumn(name="facility_id", referencedColumnName="id")
    private Facility facility;
    @OneToMany(mappedBy = "fileBatchId")
    private List<FileUpload> fileUploads;
}
