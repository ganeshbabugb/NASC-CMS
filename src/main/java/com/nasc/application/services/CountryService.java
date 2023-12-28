package com.nasc.application.services;

import com.nasc.application.data.core.CountryEntity;
import com.nasc.application.data.repository.CountryRepository;
import com.nasc.application.services.base.BaseServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountryService extends BaseServiceClass<CountryEntity> {

    private final CountryRepository repository;

    @Autowired
    public CountryService(CountryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CountryEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(CountryEntity item) {
        repository.save(item);
    }

    @Override
    public void delete(CountryEntity item) {
        repository.delete(item);
    }

    public List<String> getAllCountries() {
        List<CountryEntity> countryEntities = repository.findAll();
        return countryEntities.stream()
                .map(CountryEntity::getName)
                .collect(Collectors.toList());
    }
}