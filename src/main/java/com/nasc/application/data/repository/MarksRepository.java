package com.nasc.application.data.repository;

import com.nasc.application.data.model.*;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<MarksEntity, Long> {
    boolean existsByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    Optional<MarksEntity> findByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    List<MarksEntity> findBySubjectAndExam(SubjectEntity subject, ExamEntity exam);

    @Query("SELECT m FROM MarksEntity m " +
            "WHERE (:semester IS NULL OR m.exam.semester = :semester) " +
            "AND (:examType IS NULL OR m.exam.examType = :examType) " +
            "AND (:academicYear IS NULL OR m.exam.academicYear = :academicYear) " +
            "AND (:department IS NULL OR m.exam.department = :department) " +
            "AND (:student IS NULL OR m.student = :student)")
    List<MarksEntity> findByCriteria(
            @Param("semester") Semester semester,
            @Param("examType") ExamType examType,
            @Param("academicYear") AcademicYearEntity academicYear,
            @Param("department") DepartmentEntity department,
            @Param("student") User student
    );

    List<MarksEntity> findByStudentIn(List<User> students);
}