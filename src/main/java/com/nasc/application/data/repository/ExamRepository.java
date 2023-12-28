package com.nasc.application.data.repository;

import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.ExamEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    List<ExamEntity> findByResponsibleUsersAndDepartmentAndSemesterAndSubject(User responsibleUsers,
                                                                              DepartmentEntity department,
                                                                              Semester semester,
                                                                              SubjectEntity subject);
}
