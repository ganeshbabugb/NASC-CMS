package com.nasc.application.data.repository;


import com.nasc.application.data.model.AddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SampleAddressRepository
        extends
        JpaRepository<AddressDetails, Long>,
        JpaSpecificationExecutor<AddressDetails> {

}
