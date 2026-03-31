package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository statusRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowService workflowService;
    private final ApprovalHistoryService historyService;

    // ✅ CREATE REQUEST WITH DYNAMIC WORKFLOW
    public RequestResponseDto createRequest(String title,
                                            String description,
                                            String type,
                                            String urgency,
                                            String userEmail) {

        // 1️⃣ Fetch user
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Fetch PENDING status
        RequestStatus pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new RuntimeException("Status not found"));

        // 3️⃣ Fetch workflow matching request type
        Workflow workflow = workflowRepository.findByRequestType(type)
                .orElseThrow(() -> new RuntimeException("No workflow configured for request type: " + type));

        // 4️⃣ Get first stage
        WorkflowStage firstStage = workflow.getStages().stream()
                .filter(s -> s.getStageOrder() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No stage configured for first order"));

        // 5️⃣ Assign first user (optional: pick first from assignedUsers)
        User assignedTo = firstStage.getAssignedUsers() != null && !firstStage.getAssignedUsers().isEmpty()
                ? firstStage.getAssignedUsers().iterator().next()
                : null;

        // 6️⃣ Create request
        Request request = Request.builder()
                .title(title)
                .description(description)
                .requestType(type)
                .urgency(urgency)
                .createdBy(user)
                .status(pendingStatus)
                .workflow(workflow)
                .currentStageOrder(1)
                .assignedTo(assignedTo)
                .build();

        Request saved = requestRepository.save(request);

        // 7️⃣ Create initial approval history
        historyService.saveHistory(saved, firstStage, user, Decision.PENDING, "Request created");

        return mapToDto(saved);
    }

    // ✅ FETCH USER REQUESTS
    public List<RequestResponseDto> getUserRequests(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return requestRepository.findByCreatedBy(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ FETCH ALL REQUESTS
    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ MAP REQUEST → DTO
    private RequestResponseDto mapToDto(Request request) {
        WorkflowStage stage = workflowService.getCurrentStage(request);
        ApprovalHistory latest = historyService.getLatestHistory(request);

        String assignedEmails = stage != null && stage.getAssignedUsers() != null
                ? stage.getAssignedUsers().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", "))
                : null;

        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requestType(request.getRequestType())
                .urgency(request.getUrgency())
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .stageNumber(stage != null ? stage.getStageOrder() : null)
                .approvalComment(latest != null ? latest.getComment() : null)
                .assignedTo(assignedEmails)
                .build();
    }
}