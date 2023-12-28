package com.nasc.application.data.core.dto;

import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StudentMarksDTO {

    private String id;
    private User student;
    private Map<SubjectEntity, StudentSubjectInfo> subjectInfoMap = new HashMap<>();

    // TESTING
    private int totalPass;
    private int totalFail;
    private int totalAbsent;
    private int totalPresent;

    public StudentMarksDTO() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Map<SubjectEntity, StudentSubjectInfo> getSubjectInfoMap() {
        return subjectInfoMap;
    }

    public void setSubjectInfoMap(Map<SubjectEntity, StudentSubjectInfo> subjectInfoMap) {
        this.subjectInfoMap = subjectInfoMap;
    }

    public int getTotalPass() {
        return totalPass;
    }

    public void setTotalPass(int totalPass) {
        this.totalPass = totalPass;
    }

    public int getTotalFail() {
        return totalFail;
    }

    public void setTotalFail(int totalFail) {
        this.totalFail = totalFail;
    }

    public int getTotalAbsent() {
        return totalAbsent;
    }

    public void setTotalAbsent(int totalAbsent) {
        this.totalAbsent = totalAbsent;
    }

    public int getTotalPresent() {
        return totalPresent;
    }

    public void setTotalPresent(int totalPresent) {
        this.totalPresent = totalPresent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentMarksDTO that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
