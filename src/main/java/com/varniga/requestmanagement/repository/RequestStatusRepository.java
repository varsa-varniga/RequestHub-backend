package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Long> {

    Optional<RequestStatus> findByName(String name);
}