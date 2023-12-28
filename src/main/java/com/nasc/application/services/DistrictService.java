package com.nasc.application.services;

import com.nasc.application.data.core.DistrictEntity;
import com.nasc.application.data.repository.DistrictRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictService extends BaseServiceClass<DistrictEntity> {

    private final DistrictRepository repository;

    @Autowired
    public DistrictService(DistrictRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DistrictEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(DistrictEntity item) {
        repository.save(item);
    }

    @Override
    public void delete(DistrictEntity item) {
        repository.delete(item);
    }
}