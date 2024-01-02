package com.nasc.application.data.core;

import com.nasc.application.data.core.base.BaseEntity;
import com.nasc.application.data.core.enums.MajorOfPaper;
import com.nasc.application.data.core.enums.PaperType;
import com.nasc.application.data.core.enums.Semester;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "t_subjects")
public class SubjectEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subjectName;
    private String subjectShortForm;
    private String subjectCode;
    @Enumerated(EnumType.STRING)
    private PaperType paperType;

    @Enumerated(EnumType.STRING)
    private MajorOfPaper majorOfPaper;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaperType getPaperType() {
        return paperType;
    }

    public void setPaperType(PaperType paperType) {
        this.paperType = paperType;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectShortForm() {
        return subjectShortForm;
    }

    public void setSubjectShortForm(String subjectShortForm) {
        this.subjectShortForm = subjectShortForm;
    }

    public MajorOfPaper getMajorOfPaper() {
        return majorOfPaper;
    }

    public void setMajorOfPaper(MajorOfPaper majorOfPaper) {
        this.majorOfPaper = majorOfPaper;
    }

    public DepartmentEntity getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectEntity subject)) return false;
        return Objects.equals(id, subject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
