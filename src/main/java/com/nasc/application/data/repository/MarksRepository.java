package com.nasc.application.data.repository;

import com.nasc.application.data.core.ExamEntity;
import com.nasc.application.data.core.MarksEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.enums.ExamType;
import com.nasc.application.data.core.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<MarksEntity, Long> {
    boolean existsByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    Optional<MarksEntity> findByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    List<MarksEntity> findByStudentAndExam_SemesterAndExam_ExamType(User student, Semester semester, ExamType examType);

    Optional<MarksEntity> findByStudentAndSubject(User student, SubjectEntity subject); // Logic Every Student study one subject exactly one time
}

