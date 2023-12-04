package com.nasc.application.services;

import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.repository.AcademicYearRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicYearService extends BaseServiceClass<AcademicYearEntity> {

    private final AcademicYearRepository repository;

    @Autowired
    public AcademicYearService(AcademicYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AcademicYearEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(AcademicYearEntity item) {
        repository.save(item);
    }

    @Override
    public void delete(AcademicYearEntity item) {
        repository.delete(item);
    }

    public List<String> getAllAcademicYears() {
        List<AcademicYearEntity> academicYearEntities = repository.findAll();
        return academicYearEntities.stream()
                .map(academicYearEntity -> academicYearEntity.getStartYear() + "-" + academicYearEntity.getEndYear())
                .collect(Collectors.toList());
    }
}