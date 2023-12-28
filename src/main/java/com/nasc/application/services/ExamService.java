package com.nasc.application.services;

import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.ExamEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.enums.Semester;
import com.nasc.application.data.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;

    @Autowired
    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<ExamEntity> getAllExams() {
        return examRepository.findAll();
    }

    public void saveExam(ExamEntity exam) {
        examRepository.save(exam);
    }

    public List<ExamEntity> getExamsByCriteria(User user, DepartmentEntity department, Semester semester, SubjectEntity subject) {
        return examRepository.findByResponsibleUsersAndDepartmentAndSemesterAndSubject(user, department, semester, subject);
    }
}
