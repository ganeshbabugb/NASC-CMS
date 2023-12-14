package com.nasc.application.data.model;

import jakarta.persistence.*;

@Entity
public class MarksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markId;

    private Double marksObtained;

    @ManyToOne(fetch = FetchType.EAGER)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    private SubjectEntity subject;

    @ManyToOne(fetch = FetchType.EAGER)
    private ExamEntity exam;

    private boolean absent;

    public Long getMarkId() {
        return markId;
    }

    public void setMarkId(Long markId) {
        this.markId = markId;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public SubjectEntity getSubject() {
        return subject;
    }

    public void setSubject(SubjectEntity subject) {
        this.subject = subject;
    }

    public ExamEntity getExam() {
        return exam;
    }

    public void setExam(ExamEntity exam) {
        this.exam = exam;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }
}
