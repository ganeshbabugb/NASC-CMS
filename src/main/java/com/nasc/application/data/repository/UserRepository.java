package com.nasc.application.data.repository;


import com.nasc.application.data.core.AcademicYearEntity;
import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.enums.Role;
import com.nasc.application.data.core.enums.StudentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.department = :department AND :role MEMBER OF u.roles")
    List<User> findUsersByDepartmentAndRole(@Param("department") DepartmentEntity department, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.department = :department AND :role MEMBER OF u.roles AND u.academicYear = :academicYear AND u.studentSection = :studentSection")
    List<User> findUsersByDepartmentAndRoleAndAcademicYearAndStudentSection(
            @Param("department") DepartmentEntity department,
            @Param("role") Role role,
            @Param("academicYear") AcademicYearEntity academicYear,
            @Param("studentSection") StudentSection studentSection
    );

    @Query("SELECT u.registerNumber FROM User u WHERE u.registerNumber IN :registerNumbers")
    List<String> findExistingRegisterNumbers(@Param("registerNumbers") List<String> registerNumbers);

    List<User> findUsersByRolesIn(Collection<Role> roles);

}
