package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.enums.NotificationType;
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

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository statusRepository;
    private final WorkflowService workflowService;
    private final AuthorizationService authorizationService;
    private final ApprovalHistoryService historyService;

    private final PriorityService priorityService;
    private final NotificationService notificationService;

    @Transactional
    public RequestResponseDto decide(Long requestId,
                                     Decision decision,
                                     String comment,
                                     String userEmail) {

        // 1️⃣ Fetch request
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 2️⃣ Guard
        String currentStatus = request.getStatus() != null ? request.getStatus().getName() : "";
        if (currentStatus.equals("APPROVED") || currentStatus.equals("REJECTED")) {
            throw new RuntimeException("Request is already " + currentStatus);
        }

        // 3️⃣ Fetch user
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4️⃣ Validate
        authorizationService.validateApprover(request, user);

        WorkflowStage currentStage = workflowService.getCurrentStage(request);

        // 5️⃣ Save history
        historyService.saveHistory(request, currentStage, user, decision, comment);

        // 🔔 NOTIFICATIONS

        if (decision == Decision.APPROVED) {

            boolean hasNext = workflowService.moveToNextStage(request);

            if (hasNext) {
                // 🔥 Notify next stage approvers
                WorkflowStage nextStage = workflowService.getCurrentStage(request);

                if (nextStage != null && nextStage.getAssignedUsers() != null) {
                    for (User nextUser : nextStage.getAssignedUsers()) {
                        notificationService.createNotification(
                                nextUser.getId(),
                                "A ticket is awaiting your approval: " + request.getTitle(),
                                NotificationType.TICKET_ASSIGNED
                        );
                    }
                }

            } else {
                // 🔥 Final approval
                RequestStatus approved = statusRepository.findByName("APPROVED")
                        .orElseThrow(() -> new RuntimeException("APPROVED status not found"));

                request.setStatus(approved);

                // 🔔 Notify creator
                notificationService.createNotification(
                        request.getCreatedBy().getId(),
                        "Your ticket has been APPROVED: " + request.getTitle(),
                        NotificationType.APPROVED
                );
            }

        } else {
            // 🔥 Rejected
            RequestStatus rejected = statusRepository.findByName("REJECTED")
                    .orElseThrow(() -> new RuntimeException("REJECTED status not found"));

            request.setStatus(rejected);
            request.setCurrentStageOrder(1);

            // 🔔 Notify creator
            notificationService.createNotification(
                    request.getCreatedBy().getId(),
                    "Your ticket has been REJECTED: " + request.getTitle(),
                    NotificationType.REJECTED
            );
        }

        // 🔁 Update priority
        request.setPriorityScore(priorityService.calculatePriorityScore(request));

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
        request.setEscalated(false);

        request.setPriorityScore(priorityService.calculatePriorityScore(request));

        requestRepository.save(request);

        // 🔔 Notify first stage approvers
        WorkflowStage firstStage = workflowService.getCurrentStage(request);

        if (firstStage != null && firstStage.getAssignedUsers() != null) {
            for (User approver : firstStage.getAssignedUsers()) {
                notificationService.createNotification(
                        approver.getId(),
                        "A ticket has been resubmitted: " + request.getTitle(),
                        NotificationType.TICKET_ASSIGNED
                );
            }
        }

        return mapToDto(request);
    }

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
                .requestTypeCode(request.getRequestType() != null ? request.getRequestType().getCode() : null)
                .requestTypeName(request.getRequestType() != null ? request.getRequestType().getName() : null)
                .urgency(request.getUrgency() != null ? request.getUrgency().name() : null)
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .currentStageOrder(stage != null ? stage.getStageOrder() : null)
                .approvalComment(latest != null ? latest.getComment() : null)
                .assignedTo(assignedEmails)
                .priorityScore(request.getPriorityScore())
                .slaDeadline(request.getSlaDeadline())
                .escalated(request.isEscalated())
                .build();
    }
}