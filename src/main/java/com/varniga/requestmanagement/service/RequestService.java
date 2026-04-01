package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.CommentDto;
import com.varniga.requestmanagement.dto.CreateRequestDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.enums.NotificationType;
import com.varniga.requestmanagement.enums.Urgency;
import com.varniga.requestmanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository statusRepository;
    private final WorkflowRepository workflowRepository;
    private final RequestTypeRepository requestTypeRepository;

    private final ApprovalHistoryService historyService;
    private final PriorityService priorityService;
    private final NotificationService notificationService;
    private final SlaService slaService;

    // =========================
    // CREATE REQUEST (FIXED)
    // =========================
    public RequestResponseDto createRequest(CreateRequestDto dto, String userEmail) {

        // 1. USER
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. REQUEST TYPE (FIXED)
        RequestType requestType = requestTypeRepository.findByCode(dto.getRequestTypeCode())
                .orElseThrow(() -> new RuntimeException("Invalid request type"));

        // 3. STATUS
        RequestStatus pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new RuntimeException("Status not found"));

        // 4. WORKFLOW
        Workflow workflow = workflowRepository
                .findByRequestType_Code(dto.getRequestTypeCode())
                .orElseThrow(() -> new RuntimeException("No workflow for request type"));

        // 5. FIRST STAGE
        WorkflowStage firstStage = workflow.getStages().stream()
                .filter(s -> s.getStageOrder() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No first stage configured"));

        // 6. URGENCY
        Urgency urgency;
        try {
            urgency = Urgency.valueOf(dto.getUrgency().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid urgency value");
        }

        // 7. ASSIGNED USER
        User assignedTo = (firstStage.getAssignedUsers() != null &&
                !firstStage.getAssignedUsers().isEmpty())
                ? firstStage.getAssignedUsers().iterator().next()
                : null;

        // 8. BUILD REQUEST
        Request request = Request.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .requestType(requestType)
                .urgency(urgency)
                .createdBy(user)
                .status(pendingStatus)
                .workflow(workflow)
                .currentStage(firstStage)
                .currentStageOrder(1)
                .assignedTo(assignedTo)
                .escalated(false)
                .build();

        request.setStageEnteredAt(LocalDateTime.now());
        request.setHalfTimeWarned(false);
        request.setSlaBreachedHandled(false);

        // 9. SLA
        request.setSlaDeadline(
                slaService.calculateDeadline(requestType.getCode(), urgency.name())
        );



        // 10. SAVE
        Request saved = requestRepository.save(request);

        // 11. PRIORITY
        saved.setPriorityScore(priorityService.calculatePriorityScore(saved));
        saved = requestRepository.save(saved);

        // 12. NOTIFICATIONS
        if (assignedTo != null) {
            notificationService.createNotification(
                    assignedTo.getId(),
                    "New ticket assigned: " + saved.getTitle(),
                    NotificationType.TICKET_ASSIGNED
            );
        }

        notificationService.createNotification(
                user.getId(),
                "Ticket created: " + saved.getTitle(),
                NotificationType.TICKET_CREATED
        );

        // 13. HISTORY
        historyService.saveHistory(
                saved,
                firstStage,
                user,
                Decision.PENDING,
                "Request created"
        );

        return mapToDto(saved);
    }

    // =========================
    // USER REQUESTS
    // =========================
    public List<RequestResponseDto> getUserRequests(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return requestRepository.findByCreatedByAndDeletedFalse(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // =========================
    // ALL REQUESTS
    // =========================
    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    //========================
    //MY APPROVALS
    //========================

    public List<RequestResponseDto> getMyApprovals(String email, String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) {
            return requestRepository
                    .findDistinctByWorkflow_Stages_AssignedUsers_Email(email)
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }

        return requestRepository
                .findDistinctByWorkflow_Stages_AssignedUsers_EmailAndStatus_Name(email, status)
                .stream()
                .map(this::mapToDto)
                .toList();
    }



    // =========================
    // PRIORITY SORTED
    // =========================
    public List<RequestResponseDto> getAllRequestsByPriority() {
        return requestRepository.findAllByOrderByPriorityScoreDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    //===================
    //move to next stage
    //===================
    @Transactional
    public void moveToNextStage(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.moveToNextStage();

        request.setStageEnteredAt(LocalDateTime.now());
        request.setHalfTimeWarned(false);
        request.setSlaBreachedHandled(false);

        requestRepository.save(request);
    }

    //==========delete request============//
    public void deleteRequest(Long id, String username) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getCreatedBy() == null ||
                !request.getCreatedBy().getEmail().equals(username)) {
            throw new RuntimeException("Not allowed");
        }

        request.setDeleted(true);
        requestRepository.save(request);
    }

    // =========================
    // MAPPER
    // =========================
    private RequestResponseDto mapToDto(Request request) {

        WorkflowStage stage = request.getCurrentStage();
        ApprovalHistory latest = historyService.getLatestHistory(request);
        List<CommentDto> comments = historyService.getHistoryByRequest(request)
                .stream()
                .map(h -> CommentDto.builder()
                        .author(h.getApprovedBy() != null ? h.getApprovedBy().getEmail() : "SYSTEM")
                        .message(h.getComment())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        String assignedEmails = (stage != null && stage.getAssignedUsers() != null)
                ? stage.getAssignedUsers().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", "))
                : null;

        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requestTypeCode(request.getRequestType() != null ? request.getRequestType().getCode() : null)
                .requestTypeName(request.getRequestType() != null ? request.getRequestType().getName() : null)
                .urgency(request.getUrgency() != null ? request.getUrgency().name() : null)
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .currentStageOrder(stage != null ? stage.getStageOrder() : null)
                .currentStageName(stage != null ? stage.getStageName() : null)
                .approvalComment(latest != null ? latest.getComment() : null)
                .comments(comments)
                .assignedTo(assignedEmails)
                .priorityScore(request.getPriorityScore())
                .slaDeadline(request.getSlaDeadline())
                .escalated(request.isEscalated())
                .build();
    }
}