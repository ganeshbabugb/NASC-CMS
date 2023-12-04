package com.nasc.application.services;

import com.nasc.application.data.model.BloodGroupEntity;
import com.nasc.application.data.repository.BloodGroupRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodGroupService extends BaseServiceClass<BloodGroupEntity> {

    private final BloodGroupRepository bloodGroupRepository;

    @Autowired
    public BloodGroupService(BloodGroupRepository bloodGroupRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
    }

    @Override
    public List<BloodGroupEntity> findAll() {
        return bloodGroupRepository.findAll();
    }

    @Override
    public void save(BloodGroupEntity item) {
        bloodGroupRepository.save(item);
    }

    @Override
    public void delete(BloodGroupEntity item) {
        bloodGroupRepository.delete(item);
    }

    public List<String> getAllStates() {
        List<BloodGroupEntity> countryEntities = bloodGroupRepository.findAll();
        return countryEntities.stream()
                .map(BloodGroupEntity::getName)
                .collect(Collectors.toList());
    }
}