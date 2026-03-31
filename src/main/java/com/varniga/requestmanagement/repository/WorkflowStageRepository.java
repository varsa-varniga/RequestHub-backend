package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Workflow;
import com.varniga.requestmanagement.entity.WorkflowStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStageRepository extends JpaRepository<WorkflowStage, Long> {

    List<WorkflowStage> findByWorkflowOrderByStageOrderAsc(Workflow workflow);

    WorkflowStage findByWorkflowAndStageOrder(Workflow workflow, Integer stageOrder);
}