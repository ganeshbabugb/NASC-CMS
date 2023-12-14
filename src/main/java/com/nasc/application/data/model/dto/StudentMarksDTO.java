package com.nasc.application.data.model.dto;

import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;

import java.util.HashMap;
import java.util.Map;

public class StudentMarksDTO {

    private User student;
    private Map<SubjectEntity, Double> subjectMarksMap = new HashMap<>();

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Map<SubjectEntity, Double> getSubjectMarksMap() {
        return subjectMarksMap;
    }

    public void setSubjectMarksMap(Map<SubjectEntity, Double> subjectMarksMap) {
        this.subjectMarksMap = subjectMarksMap;
    }

}
