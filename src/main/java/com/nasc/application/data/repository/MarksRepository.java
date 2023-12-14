package com.nasc.application.data.repository;

import com.nasc.application.data.model.ExamEntity;
import com.nasc.application.data.model.MarksEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<MarksEntity, Long> {
    boolean existsByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    Optional<MarksEntity> findByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam);

    List<MarksEntity> findByStudentAndExam_SemesterAndExam_ExamType(User student, Semester semester, ExamType examType);

    List<MarksEntity> findByStudentIn(List<User> students);

    Optional<MarksEntity> findByStudentAndSubject(User student, SubjectEntity subject); // Logic Every Student study one subject exactly one time
}

