package com.nasc.application.services;

import com.nasc.application.data.model.*;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.data.repository.MarksRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarksService {

    private final MarksRepository marksRepository;

    public MarksService(MarksRepository marksRepository) {
        this.marksRepository = marksRepository;
    }

    public void saveMarks(MarksEntity marksEntity) {
        marksRepository.save(marksEntity);

    }

    public boolean existsByStudentAndSubjectAndExam(User selectedStudent, SubjectEntity selectedSubject, ExamEntity selectedExam) {
        return marksRepository.existsByStudentAndSubjectAndExam(selectedStudent, selectedSubject, selectedExam);
    }

    public MarksEntity getMarksByStudentAndSubjectAndExam(User student, SubjectEntity subject, ExamEntity exam) {
        return marksRepository.findByStudentAndSubjectAndExam(student, subject, exam).orElse(null);
    }

    public List<MarksEntity> getMarksByCriteria(Semester semester, ExamType examType, AcademicYearEntity academicYear, DepartmentEntity department, User student) {
        return marksRepository.findByCriteria(
                semester, examType, academicYear, department, student
        );
    }

    public List<MarksEntity> getAllMarksByStudents(List<User> students) {
        return marksRepository.findByStudentIn(students);
    }

}
