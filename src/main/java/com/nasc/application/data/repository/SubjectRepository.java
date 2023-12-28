package com.nasc.application.data.repository;

import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    List<SubjectEntity> findByDepartmentAndSemester(DepartmentEntity department, Semester semester);
}