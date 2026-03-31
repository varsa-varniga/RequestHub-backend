package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.WorkflowDto;
import com.varniga.requestmanagement.dto.WorkflowResponseDto;
import com.varniga.requestmanagement.dto.WorkflowStageDto;
import com.varniga.requestmanagement.dto.WorkflowStageResponseDto;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.RequestType;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.entity.Workflow;
import com.varniga.requestmanagement.entity.WorkflowStage;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.repository.RequestTypeRepository;
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
    private final RequestTypeRepository requestTypeRepository;
    private final RequestRepository requestRepository;// ← ADDED

    // =========================
    // CREATE WORKFLOW
    // =========================
    @Transactional
    public WorkflowResponseDto createWorkflow(WorkflowDto workflowDto) {

        // ← FIXED: fetch RequestType from DB using code
        RequestType requestType = requestTypeRepository
                .findByCode(workflowDto.getRequestTypeCode())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid request type: " + workflowDto.getRequestTypeCode()));

        Workflow workflow = new Workflow();
        workflow.setName(workflowDto.getName());
        workflow.setRequestType(requestType);
        requestType.setWorkflow(workflow); // 🔥 VERY IMPORTANT

        if (workflowDto.getStages() != null) {
            for (WorkflowStageDto stageDto : workflowDto.getStages()) {

                WorkflowStage stage = new WorkflowStage();
                stage.setStageOrder(stageDto.getStageOrder());
                stage.setStageName(stageDto.getStageName());
                stage.setApproverRole(stageDto.getApproverRole());

                if (stageDto.getAssignedUserIds() != null && !stageDto.getAssignedUserIds().isEmpty()) {
                    Set<User> users = new HashSet<>(
                            userRepository.findAllById(stageDto.getAssignedUserIds())
                    );
                    stage.setAssignedUsers(users);
                }

                workflow.addStage(stage);
            }
        }

        Workflow saved = workflowRepository.save(workflow);
        requestTypeRepository.save(requestType);
        return toResponseDto(saved);
    }

    // =========================
    // GET ALL WORKFLOWS
    // =========================
    public List<WorkflowResponseDto> getAllWorkflows() {
        return workflowRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
    // =========================
// GET WORKFLOW BY REQUEST ID
// =========================
    public List<WorkflowStageResponseDto> getWorkflowByRequestId(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        RequestType requestType = request.getRequestType();

        if (requestType == null || requestType.getWorkflow() == null) {
            return List.of();
        }
        System.out.println("Request: " + request);
        System.out.println("RequestType: " + request.getRequestType());
        System.out.println("Workflow: " + request.getRequestType().getWorkflow());

        Workflow workflow = requestType.getWorkflow();

        return workflow.getStages()
                .stream()
                .map(this::toStageResponseDto)
                .collect(Collectors.toList());
    }

    // =========================
    // CURRENT STAGE
    // =========================
    public WorkflowStage getCurrentStage(Request request) {
        if (request == null) return null;
        return request.getCurrentStage();
    }

    // =========================
    // NEXT STAGE
    // =========================
    public WorkflowStage getNextStage(Request request) {
        if (request == null || request.getWorkflow() == null) return null;
        return request.getNextStage();
    }

    // =========================
    // MOVE TO NEXT STAGE
    // =========================
    public boolean moveToNextStage(Request request) {
        if (request == null) return false;

        if (!request.isLastStage()) {
            request.moveToNextStage();
            return true;
        }

        return false;
    }

    // =========================
    // GET ALL STAGES
    // =========================
    public List<WorkflowStage> getAllStages(Request request) {

        if (request == null || request.getWorkflow() == null
                || request.getWorkflow().getStages() == null) {
            return List.of();
        }

        return request.getWorkflow().getStages();
    }

    // =========================
    // DELETE WORKFLOW
    // =========================
    public void deleteWorkflow(Long id) {

        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        workflowRepository.delete(workflow);
    }

    // =========================
    // UPDATE WORKFLOW
    // =========================
    @Transactional
    public WorkflowResponseDto updateWorkflow(Long id, WorkflowDto workflowDto) {

        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        // ← FIXED: fetch RequestType from DB using code
        RequestType requestType = requestTypeRepository
                .findByCode(workflowDto.getRequestTypeCode())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid request type: " + workflowDto.getRequestTypeCode()));

        workflow.setName(workflowDto.getName());
        workflow.setRequestType(requestType); // ← properly linked entity

        workflow.getStages().clear();

        if (workflowDto.getStages() != null) {
            for (WorkflowStageDto stageDto : workflowDto.getStages()) {

                WorkflowStage stage = new WorkflowStage();
                stage.setStageOrder(stageDto.getStageOrder());
                stage.setStageName(stageDto.getStageName());
                stage.setApproverRole(stageDto.getApproverRole());

                if (stageDto.getAssignedUserIds() != null && !stageDto.getAssignedUserIds().isEmpty()) {
                    Set<User> users = new HashSet<>(
                            userRepository.findAllById(stageDto.getAssignedUserIds())
                    );
                    stage.setAssignedUsers(users);
                }

                workflow.addStage(stage);
            }
        }

        Workflow saved = workflowRepository.save(workflow);
        return toResponseDto(saved);
    }

    // =========================
    // DTO MAPPER
    // =========================
    private WorkflowResponseDto toResponseDto(Workflow workflow) {

        List<WorkflowStageResponseDto> stageDtos = workflow.getStages()
                .stream()
                .map(this::toStageResponseDto)
                .collect(Collectors.toList());

        return WorkflowResponseDto.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .requestTypeName(workflow.getRequestType() != null ? workflow.getRequestType().getName() : null)
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