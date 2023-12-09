package com.nasc.application.data.model;

import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Semester;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @ManyToOne()
    @JoinColumn(name = "academic_year_id")
    private AcademicYearEntity academicYear;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    private LocalDate examDate;

    private int minMarks;

    private int maxMarks;

    private double portionCovered;

    private int examDuration;

    private LocalDate examCorrectionDate;

    @ManyToOne
    private SubjectEntity subject;

    @ManyToMany
    @JoinTable(
            name = "exam_responsible_users",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> responsibleUsers = new HashSet<>();

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public DepartmentEntity getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public AcademicYearEntity getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYearEntity academicYear) {
        this.academicYear = academicYear;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public int getMinMarks() {
        return minMarks;
    }

    public void setMinMarks(int minMarks) {
        this.minMarks = minMarks;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getPortionCovered() {
        return portionCovered;
    }

    public void setPortionCovered(double portionCovered) {
        this.portionCovered = portionCovered;
    }

    public int getExamDuration() {
        return examDuration;
    }

    public void setExamDuration(int examDuration) {
        this.examDuration = examDuration;
    }

    public LocalDate getExamCorrectionDate() {
        return examCorrectionDate;
    }

    public void setExamCorrectionDate(LocalDate examCorrectionDate) {
        this.examCorrectionDate = examCorrectionDate;
    }

    public SubjectEntity getSubject() {
        return subject;
    }

    public void setSubject(SubjectEntity subject) {
        this.subject = subject;
    }

    public Set<User> getResponsibleUsers() {
        return responsibleUsers;
    }

    public void setResponsibleUsers(Set<User> responsibleUsers) {
        this.responsibleUsers = responsibleUsers;
    }
}
