package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {

    Optional<RequestType> findByCode(String code);
}