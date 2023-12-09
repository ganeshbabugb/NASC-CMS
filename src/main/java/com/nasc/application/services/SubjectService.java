package com.nasc.application.services;

import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.data.repository.SubjectRepository;
import com.nasc.application.security.AuthenticatedUser;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService extends BaseServiceClass<SubjectEntity> {

    private final SubjectRepository repository;

    private final AuthenticatedUser authenticatedUser;

    @Autowired
    public SubjectService(SubjectRepository repository, AuthenticatedUser authenticatedUser) {
        this.repository = repository;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public List<SubjectEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(SubjectEntity item) {
        Optional<User> user = authenticatedUser.get();
        item.setDepartment(user.get().getDepartment());
        repository.save(item);
    }

    @Override
    public void delete(SubjectEntity item) {
        repository.delete(item);
    }

    public List<SubjectEntity> getSubjectsByDepartmentAndSemester(DepartmentEntity department, Semester semester) {
        return repository.findByDepartmentAndSemester(department, semester);
    }

}