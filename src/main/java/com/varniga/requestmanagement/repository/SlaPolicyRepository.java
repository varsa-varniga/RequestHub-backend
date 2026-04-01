package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.SlaPolicy;
import com.varniga.requestmanagement.enums.Urgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByRequestTypeAndUrgency(String requestType, Urgency urgency);
}