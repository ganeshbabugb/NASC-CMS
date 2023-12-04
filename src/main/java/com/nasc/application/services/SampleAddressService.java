package com.nasc.application.services;

import com.nasc.application.data.model.AddressDetails;
import com.nasc.application.data.repository.SampleAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SampleAddressService {

    private final SampleAddressRepository repository;

    public SampleAddressService(SampleAddressRepository repository) {
        this.repository = repository;
    }

    public Optional<AddressDetails> get(Long id) {
        return repository.findById(id);
    }

    public AddressDetails update(AddressDetails entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<AddressDetails> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<AddressDetails> list(Pageable pageable, Specification<AddressDetails> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
