package com.nasc.application.services;

import com.nasc.application.data.model.*;
import com.nasc.application.data.repository.UserRepository;
import com.nasc.application.security.AuthenticatedUser;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ActiveUsersManagerService activeUsersManagerService;

    private final AuthenticationContext authenticationContext;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticatedUser authenticatedUser;

    public UserService(UserRepository userRepository, ActiveUsersManagerService activeUsersManagerService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationContext authenticationContext, AuthenticatedUser authenticatedUser
    ) {
        this.activeUsersManagerService = activeUsersManagerService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationContext = authenticationContext;
        this.authenticatedUser = authenticatedUser;
    }

    public Optional<User> get(Long id) {
        return userRepository.findById(id);
    }

    public User update(User entity) {
        return userRepository.save(entity);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return userRepository.findAll(filter, pageable);
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public int count() {
        return (int) userRepository.count();
    }

    @Transactional
    public void saveUserWithBankDetails(User user, BankDetails bankDetails) {
        user.setBankDetailsCompleted(Boolean.TRUE);
        user.setBankDetails(bankDetails);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void saveUserWithAddressDetails(User user, AddressDetails addressDetails) {
        user.setAddressDetailsCompleted(Boolean.TRUE);
        user.setAddressDetails(addressDetails);
        userRepository.save(user);
    }

    public List<UserDetails> getOnlineUsers() {
        return activeUsersManagerService.getActiveUsers();
    }

    public Principal getCurrentUserPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public void changePassword(String oldPasswordValue, String newPasswordValue, String confirmPasswordValue) {
        try {
            String name = getCurrentUserPrincipal().getName();
            User user = getPersonByUserName(name);
            if (newPasswordValue.equals(confirmPasswordValue)) {
                if (passwordEncoder.matches(oldPasswordValue, user.getPassword())) {
                    if (passwordEncoder.matches(newPasswordValue, user.getPassword())) {
                        throw new IllegalArgumentException("NEW PASSWORD AND OLD PASSWORD WERE THE SAME");
                    } else {
                        String encodedNewPassword = passwordEncoder.encode(newPasswordValue);
                        user.setPassword(encodedNewPassword);
                        userRepository.save(user);
                        authenticationContext.logout();
                    }
                } else {
                    throw new IllegalArgumentException("OLD PASSWORD DOESN'T MATCH WITH USER PASSWORD");
                }
            } else {
                throw new IllegalArgumentException("CONFIRM PASSWORD AND NEW PASSWORD DON'T MATCH");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private User getPersonByUserName(String name) {
        return userRepository.findByUsername(name);
    }

    @Transactional
    public void saveUserWithPersonalDetails(User currentUser, PersonalDetails personalDetailsFromForm) {
        currentUser.setPersonalDetailsCompleted(Boolean.TRUE);
        currentUser.setPersonalDetails(personalDetailsFromForm);
        userRepository.save(currentUser);
    }

    /*
    public List<User> getStudentsForHod(User hod) {
        if (hod.getRoles().contains(Role.HOD)) {
            DepartmentEntity hodDepartmentEntity = hod.getDepartment();
            return userRepository.findByDepartment(hodDepartmentEntity);
        }
        return Collections.emptyList();
    }
    */

    public List<User> findUsersByDepartmentAndRoleAndAcademicYear(Role targetRole, AcademicYearEntity academicYear) {
        Optional<User> user = authenticatedUser.get();
        return user.map(value -> userRepository.findUsersByDepartmentAndRoleAndAcademicYear(value.getDepartment(), targetRole, academicYear))
                .orElse(Collections.emptyList());
    }

    public List<User> findUsersByDepartmentAndRole(Role targetRole) {
        Optional<User> user = authenticatedUser.get();
        return user.map(value -> userRepository.findUsersByDepartmentAndRole(value.getDepartment(), targetRole))
                .orElse(Collections.emptyList());
    }

    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }
}

