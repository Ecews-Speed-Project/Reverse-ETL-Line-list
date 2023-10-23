package com.etlservice.schedular.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="role",uniqueConstraints=@UniqueConstraint(columnNames={"role_name"}))
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name="role_name", nullable=false, unique=true)
    private String roleName;
    @Column(name="role_description")
    private String roleDescription;
}
