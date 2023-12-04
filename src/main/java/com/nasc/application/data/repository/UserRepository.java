package com.nasc.application.data.repository;


import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.Role;
import com.nasc.application.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.department = :department AND :role MEMBER OF u.roles")
    List<User> findUsersByDepartmentAndRole(@Param("department") DepartmentEntity department, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.department = :department AND :role MEMBER OF u.roles AND u.academicYear = :academicYear")
    List<User> findUsersByDepartmentAndRoleAndAcademicYear(
            @Param("department") DepartmentEntity department,
            @Param("role") Role role,
            @Param("academicYear") AcademicYearEntity academicYear
    );
}
