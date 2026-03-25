package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.repository.RequestStatusRepository;
import com.varniga.requestmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final RequestRepository       requestRepository;
    private final UserRepository          userRepository;
    private final RequestStatusRepository statusRepository;
    private final WorkflowService         workflowService;
    private final AuthorizationService    authorizationService;
    private final ApprovalHistoryService  historyService;

    // ── PHASE 2: inject PriorityService ──────────────────────────────────────
    private final PriorityService priorityService; // ← NEW
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public RequestResponseDto decide(Long requestId,
                                     Decision decision,
                                     String comment,
                                     String userEmail) {

        // 1️⃣ Fetch request
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 2️⃣ Guard: already terminal
        String currentStatus = request.getStatus() != null ? request.getStatus().getName() : "";
        if (currentStatus.equals("APPROVED") || currentStatus.equals("REJECTED")) {
            throw new RuntimeException("Request is already " + currentStatus + " and cannot be acted on.");
        }

        // 3️⃣ Fetch approver
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4️⃣ Validate approver against current stage
        authorizationService.validateApprover(request, user);

        WorkflowStage stage = workflowService.getCurrentStage(request);

        // 5️⃣ Persist history entry
        historyService.saveHistory(request, stage, user, decision, comment);

        // 6️⃣ Handle decision
        if (decision == Decision.APPROVED) {
            boolean hasNext = workflowService.moveToNextStage(request);
            if (!hasNext) {
                RequestStatus approved = statusRepository.findByName("APPROVED")
                        .orElseThrow(() -> new RuntimeException("APPROVED status not found"));
                request.setStatus(approved);
            }
            // else stays PENDING, advanced to next stage
        } else {
            RequestStatus rejected = statusRepository.findByName("REJECTED")
                    .orElseThrow(() -> new RuntimeException("REJECTED status not found"));
            request.setStatus(rejected);
            request.setCurrentStageOrder(1);
        }

        // ── PHASE 2: recalculate priorityScore after every decision ──────────
        // The time-pending component grows with each passing hour, so
        // refreshing here keeps the score accurate for priority-ordered listings.
        request.setPriorityScore(priorityService.calculatePriorityScore(request)); // ← NEW
        // ─────────────────────────────────────────────────────────────────────

        requestRepository.save(request);
        return mapToDto(request);
    }

    @Transactional
    public RequestResponseDto resubmit(Long requestId, String userEmail) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCreatedBy().getEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("Only the request creator can resubmit.");
        }

        if (!request.getStatus().getName().equals("REJECTED")) {
            throw new RuntimeException("Only rejected requests can be resubmitted.");
        }

        RequestStatus pending = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new RuntimeException("PENDING status not found"));

        request.setStatus(pending);
        request.setCurrentStageOrder(1);

        // ── PHASE 2: reset escalation flag + refresh score on resubmit ───────
        request.setEscalated(false);                                          // ← NEW
        request.setPriorityScore(priorityService.calculatePriorityScore(request)); // ← NEW
        // ─────────────────────────────────────────────────────────────────────

        requestRepository.save(request);
        return mapToDto(request);
    }

    // ── mapToDto (urgency now Enum, Phase 2 fields added) ────────────────────
    private RequestResponseDto mapToDto(Request request) {
        WorkflowStage stage    = workflowService.getCurrentStage(request);
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
                .urgency(request.getUrgency() != null ? request.getUrgency().name() : null) // ← PHASE 2
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .stageNumber(stage != null ? stage.getStageOrder() : null)
                .approvalComment(latest != null ? latest.getComment() : null)
                .assignedTo(assignedEmails)
                // ── PHASE 2: new DTO fields ───────────────────────────────────
                .priorityScore(request.getPriorityScore())
                .slaDeadline(request.getSlaDeadline())
                .escalated(request.getEscalated())
                .build();
    }
}