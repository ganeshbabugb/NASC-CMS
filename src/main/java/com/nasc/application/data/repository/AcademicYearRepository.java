package com.nasc.application.data.repository;

import com.nasc.application.data.model.AcademicYearEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYearEntity, Long> {
}