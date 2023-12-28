package com.nasc.application.data.core.dto;

public class StudentSubjectInfo {
    private Double marks;
    private Boolean absent;
    private Double passMarks;
    private Double maxMarks;

    // Default constructor (implicitly provided by Java)
    public StudentSubjectInfo() {
        // No need to explicitly write this constructor unless you want to do some initialization.
    }

    // Custom constructor
    public StudentSubjectInfo(Double marks, Boolean absent, Double passMarks, Double maxMarks) {
        this.marks = marks;
        this.absent = absent;
        this.passMarks = passMarks;
        this.maxMarks = maxMarks;
    }

    //     for grid
    public StudentSubjectInfo(Double mark) {
        this.marks = mark;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public Boolean getAbsent() {
        return absent;
    }

    public void setAbsent(Boolean absent) {
        this.absent = absent;
    }

    public Double getPassMarks() {
        return passMarks;
    }

    public void setPassMarks(Double passMarks) {
        this.passMarks = passMarks;
    }

    public Double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
    }
}
