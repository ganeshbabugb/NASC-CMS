package com.nasc.application.services;

import com.nasc.application.data.core.ExamEntity;
import com.nasc.application.data.core.MarksEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.dto.StudentMarksDTO;
import com.nasc.application.data.core.dto.StudentSubjectInfo;
import com.nasc.application.data.core.enums.ExamType;
import com.nasc.application.data.core.enums.Semester;
import com.nasc.application.data.repository.MarksRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    public Optional<MarksEntity> findMarkByStudentAndSubject(User student, SubjectEntity subject, ExamEntity exam) {
        return marksRepository.findByStudentAndSubjectAndExam(student, subject, exam);
    }

    public List<StudentMarksDTO> getStudentMarksByFilters(ExamType examType, Semester semester, User student) {
        List<MarksEntity> marks = marksRepository.findByStudentAndExam_SemesterAndExam_ExamType(student, semester, examType);

        return marks.stream()
                .collect(Collectors.groupingBy(MarksEntity::getStudent))
                .entrySet().stream()
                .map(entry -> {
                    User currentStudent = entry.getKey();
                    List<MarksEntity> studentMarks = entry.getValue();

                    log.info("Fetching marks for student: {}", currentStudent.getUsername());

                    StudentMarksDTO studentMarksDTO = createStudentMarksDTO(currentStudent, studentMarks);

                    log.info("Fetched marks for student: {}. Details: {}", currentStudent.getUsername(), studentMarksDTO);

                    return studentMarksDTO;
                })
                .collect(Collectors.toList());
    }

    private StudentMarksDTO createStudentMarksDTO(User student, List<MarksEntity> studentMarks) {
        StudentMarksDTO studentMarksDTO = new StudentMarksDTO();
        studentMarksDTO.setStudent(student);

        Map<SubjectEntity, StudentSubjectInfo> subjectInfoMap = studentMarks.stream()
                .collect(Collectors.toMap(
                        MarksEntity::getSubject,
                        this::createStudentSubjectInfo
                ));

        studentMarksDTO.setSubjectInfoMap(subjectInfoMap);

        return studentMarksDTO;
    }

    private StudentSubjectInfo createStudentSubjectInfo(MarksEntity marksEntity) {
        StudentSubjectInfo subjectInfo = new StudentSubjectInfo();
        subjectInfo.setMarks(marksEntity.getMarksObtained());
        subjectInfo.setAbsent(marksEntity.isAbsent()); // Set absent information
        subjectInfo.setPassMarks(marksEntity.getExam().getMinMarks());
        subjectInfo.setMaxMarks(marksEntity.getExam().getMaxMarks());

        return subjectInfo;
    }

    public void updateStudentMarks(StudentMarksDTO studentMarksDTO) {
        User student = studentMarksDTO.getStudent();

        for (Map.Entry<SubjectEntity, StudentSubjectInfo> entry : studentMarksDTO.getSubjectInfoMap().entrySet()) {
            SubjectEntity subject = entry.getKey();
            StudentSubjectInfo subjectInfo = entry.getValue();

            Optional<MarksEntity> existsMarksEntity = marksRepository.findByStudentAndSubject(student, subject);

            if (existsMarksEntity.isPresent()) {
                MarksEntity marksEntity = existsMarksEntity.get();

                try {
                    Double newMarks = subjectInfo.getMarks();

                    if (marksHaveChanged(newMarks, marksEntity)) {
                        // Log existing marks
                        log.info("Updating marks for student: {}, subject: {}", student.getUsername(), subject.getSubjectName());
                        log.info("Existing Marks: {}, New Marks: {}", marksEntity.getMarksObtained(), newMarks);

                        // Update marks and other details
                        marksEntity.setMarksObtained(newMarks);

                        // Save the modified MarksEntity to the database
                        marksRepository.save(marksEntity);

                        // Update the corresponding entry in the DTO
                        studentMarksDTO.getSubjectInfoMap().replace(subject, subjectInfo);
                    }

                } catch (Exception e) {
                    // Handle exceptions and log error
                    log.error("Error updating marks for student: {}, subject: {}", student.getUsername(), subject.getSubjectName(), e);
                }
            }
        }
    }

    private boolean marksHaveChanged(Double modifiedMarks, MarksEntity marksEntity) {
        return !modifiedMarks.equals(marksEntity.getMarksObtained());
    }
}
