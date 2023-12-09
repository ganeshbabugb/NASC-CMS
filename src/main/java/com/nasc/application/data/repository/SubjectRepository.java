package com.nasc.application.data.repository;

import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    List<SubjectEntity> findByDepartmentAndSemester(DepartmentEntity department, Semester semester);

    @Query("SELECT DISTINCT s.subjectName FROM SubjectEntity s " +
            "WHERE (:semester is null or s.semester = :semester) " +
            "AND (:department is null or s.department = :department)")
    List<String> findDistinctSubjectNamesByCriteria(
            @Param("semester") Semester semester,
            @Param("department") DepartmentEntity department
    );


}