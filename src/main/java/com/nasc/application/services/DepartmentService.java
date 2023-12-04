package com.nasc.application.services;

import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.repository.DepartmentRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService extends BaseServiceClass<DepartmentEntity> {

    private final DepartmentRepository repository;

    @Autowired
    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DepartmentEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(DepartmentEntity item) {
        repository.save(item);
    }

    @Override
    public void delete(DepartmentEntity item) {
        repository.delete(item);
    }

    public List<String> getAllStates() {
        List<DepartmentEntity> countryEntities = repository.findAll();
        return countryEntities.stream()
                .map(DepartmentEntity::getName)
                .collect(Collectors.toList());
    }
}