package com.nasc.application.data.repository;

import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.ExamEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    List<ExamEntity> findByResponsibleUsersAndDepartmentAndSemester(User user,
                                                                    DepartmentEntity department,
                                                                    Semester semester
    );
}
