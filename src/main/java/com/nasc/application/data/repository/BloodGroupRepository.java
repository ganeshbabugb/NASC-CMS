package com.nasc.application.data.repository;

import com.nasc.application.data.model.BloodGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodGroupRepository extends JpaRepository<BloodGroupEntity, Long> {
}