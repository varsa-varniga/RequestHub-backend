package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.*;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.repository.UserRepository;
import com.varniga.requestmanagement.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkflowResponseDto createWorkflow(WorkflowDto workflowDto) {
        Workflow workflow = new Workflow();
        workflow.setName(workflowDto.getName());
        workflow.setRequestType(workflowDto.getRequestType());

        if (workflowDto.getStages() != null) {
            for (WorkflowStageDto stageDto : workflowDto.getStages()) {
                WorkflowStage stage = new WorkflowStage();
                stage.setStageOrder(stageDto.getStageOrder());
                stage.setStageName(stageDto.getStageName());
                stage.setApproverRole(stageDto.getApproverRole());

                if (stageDto.getAssignedUserIds() != null && !stageDto.getAssignedUserIds().isEmpty()) {
                    Set<User> users = new HashSet<>(userRepository.findAllById(stageDto.getAssignedUserIds()));
                    stage.setAssignedUsers(users);
                }

                workflow.addStage(stage);
            }
        }

        Workflow saved = workflowRepository.save(workflow);
        return toResponseDto(saved);
    }

    public List<WorkflowResponseDto> getAllWorkflows() {
        return workflowRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // Returns current stage of the request
    public WorkflowStage getCurrentStage(Request request) {
        return request.getCurrentStage();
    }

    // Returns next stage of the request
    public WorkflowStage getNextStage(Request request) {
        return request.getNextStage();
    }

    // Move request to next stage if it exists
    public boolean moveToNextStage(Request request) {
        if (!request.isLastStage()) {
            request.moveToNextStage();
            return true;
        }
        return false;
    }

    // Get all stages in workflow
    public List<WorkflowStage> getAllStages(Request request) {
        if (request.getWorkflow() == null || request.getWorkflow().getStages() == null)
            return List.of();
        return request.getWorkflow().getStages();
    }

    // --- Mappers ---

    private WorkflowResponseDto toResponseDto(Workflow workflow) {
        List<WorkflowStageResponseDto> stageDtos = workflow.getStages()
                .stream()
                .map(this::toStageResponseDto)
                .collect(Collectors.toList());

        return WorkflowResponseDto.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .requestType(workflow.getRequestType())
                .stages(stageDtos)
                .build();
    }

    private WorkflowStageResponseDto toStageResponseDto(WorkflowStage stage) {
        Set<Long> userIds = stage.getAssignedUsers()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return WorkflowStageResponseDto.builder()
                .id(stage.getId())
                .stageOrder(stage.getStageOrder())
                .stageName(stage.getStageName())
                .approverRole(stage.getApproverRole())
                .assignedUserIds(userIds)
                .build();
    }
}