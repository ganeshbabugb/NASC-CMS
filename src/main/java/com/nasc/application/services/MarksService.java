package com.nasc.application.services;

import com.nasc.application.data.model.ExamEntity;
import com.nasc.application.data.model.MarksEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.dto.StudentMarksDTO;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.data.repository.MarksRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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


    public List<MarksEntity> getAllMarksByStudents(List<User> students) {
        return marksRepository.findByStudentIn(students);
    }

    public List<StudentMarksDTO> getStudentMarksByFilters(
            ExamType examType,
            Semester semester,
            User student
    ) {
        List<MarksEntity> marks = marksRepository.findByStudentAndExam_SemesterAndExam_ExamType(student, semester, examType);

        // Create StudentMarksDTO for each student
        return marks.stream()
                .collect(Collectors.groupingBy(MarksEntity::getStudent))
                .entrySet().stream()
                .map(entry -> {
                    StudentMarksDTO studentMarksDTO = new StudentMarksDTO();
                    studentMarksDTO.setStudent(entry.getKey());

                    // Map subjects and marks
                    Map<SubjectEntity, Double> subjectMarksMap = entry.getValue().stream()
                            .collect(Collectors.toMap(MarksEntity::getSubject, MarksEntity::getMarksObtained));

                    studentMarksDTO.setSubjectMarksMap(subjectMarksMap);
                    return studentMarksDTO;
                })
                .collect(Collectors.toList());
    }


    public void updateStudentMarks(StudentMarksDTO studentMarksDTO) {
        User student = studentMarksDTO.getStudent();

        // Iterate through the subject marks in the DTO
        for (Map.Entry<SubjectEntity, Double> entry : studentMarksDTO.getSubjectMarksMap().entrySet()) {
            SubjectEntity subject = entry.getKey();
            Double newMarks = entry.getValue();

            // Fetch existing MarksEntity from the database
            Optional<MarksEntity> optionalMarksEntity = marksRepository.findByStudentAndSubject(student, subject);

            // Check if the marks have changed before updating
            if (optionalMarksEntity.isPresent()) {
                MarksEntity marksEntity = optionalMarksEntity.get();

                if (marksHaveChanged(newMarks, marksEntity)) {
                    // Update marks and save the modified MarksEntity to the database
                    marksEntity.setMarksObtained(newMarks);
                    marksRepository.save(marksEntity);
                }
            }
        }
    }


    private boolean marksHaveChanged(Double modifiedMarks, MarksEntity marksEntity) {
        // Compare the modified marks with the existing ones in the database
        Double existingMarks = marksEntity.getMarksObtained();
        return !modifiedMarks.equals(existingMarks);
    }
}
