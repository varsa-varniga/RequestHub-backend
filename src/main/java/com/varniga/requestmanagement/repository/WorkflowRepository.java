package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByRequestType_Code(String code);
}