package com.nasc.application.data.model;

import com.nasc.application.data.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_academic_year")
public class AcademicYearEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startYear;

    private String endYear;

    @Override
    public Long getId() {
        return id;
    }
}