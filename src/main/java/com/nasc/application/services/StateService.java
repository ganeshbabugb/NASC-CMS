package com.nasc.application.services;

import com.nasc.application.data.core.StateEntity;
import com.nasc.application.data.repository.StateRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StateService extends BaseServiceClass<StateEntity> {

    private final StateRepository stateRepository;

    @Autowired
    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public List<StateEntity> findAll() {
        return stateRepository.findAll();
    }

    @Override
    public void save(StateEntity item) {
        stateRepository.save(item);
    }

    @Override
    public void delete(StateEntity item) {
        stateRepository.delete(item);
    }

    public List<String> getAllStates() {
        List<StateEntity> countryEntities = stateRepository.findAll();
        return countryEntities.stream()
                .map(StateEntity::getName)
                .collect(Collectors.toList());
    }
}