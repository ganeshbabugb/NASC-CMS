package com.nasc.application.services;

import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.ExamEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.enums.Semester;
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

    public List<ExamEntity> getExamsByCriteria(User user, DepartmentEntity department, Semester semester) {
        return examRepository.findByResponsibleUsersAndDepartmentAndSemester(user, department, semester);
    }
}
