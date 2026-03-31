package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.ApprovalHistory;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.entity.WorkflowStage;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.enums.NotificationType;
import com.varniga.requestmanagement.repository.RequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 2 – Auto-Escalation Engine
 *
 * Runs every hour via @Scheduled.
 * For every PENDING request whose SLA deadline has passed and has not already
 * been escalated, it:
 *   1. Moves the request to the next workflow stage (if one exists).
 *   2. Marks escalated = true so the job doesn't fire again for this request.
 *   3. Writes an ESCALATED entry to ApprovalHistory for full audit trail.
 *
 * Architecture note:
 *   Depends on RequestRepository, ApprovalHistoryService, and PriorityService.
 *   Does NOT touch WorkflowService.moveToNextStage() to avoid duplicating the
 *   approval-path logic — escalation deliberately uses the raw helper on the
 *   entity so the approval counter is not affected.
 *
 * Enable scheduling in your main class:
 *   @EnableScheduling on @SpringBootApplication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final RequestRepository      requestRepository;
    private final ApprovalHistoryService historyService;
    private final PriorityService        priorityService;
    private final NotificationService notificationService;

    /**
     * Scheduled job – runs every hour (3 600 000 ms).
     * Adjust cron expression if you need a different cadence.
     */
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void runEscalationCheck() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[EscalationService] Running SLA check at {}", now);

        List<Request> overdueRequests =
                requestRepository.findSlaBreached(now, "PENDING");

        if (overdueRequests.isEmpty()) {
            log.info("[EscalationService] No overdue requests found.");
            return;
        }

        log.warn("[EscalationService] {} request(s) breached SLA — escalating.", overdueRequests.size());

        for (Request request : overdueRequests) {
            escalate(request);
        }
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void escalate(Request request) {
        log.warn("[EscalationService] Escalating request id={} title='{}'",
                request.getId(), request.getTitle());

        WorkflowStage currentStage = request.getCurrentStage();
        WorkflowStage nextStage    = request.getNextStage();

        // 🔔 SLA BREACH notification to current assignee
        if (request.getAssignedTo() != null) {
            notificationService.createNotification(
                    request.getAssignedTo().getId(),
                    "SLA BREACHED: Immediate action required - " + request.getTitle(),
                    NotificationType.SLA_ALERT
            );
        }

        // Move to next stage if exists
        if (nextStage != null) {
            request.moveToNextStage();

            log.info("[EscalationService] Request {} moved from stage {} to stage {}",
                    request.getId(),
                    currentStage != null ? currentStage.getStageOrder() : "?",
                    nextStage.getStageOrder());

            // 🔔 Notify NEXT stage approvers
            if (nextStage.getAssignedUsers() != null) {
                for (User user : nextStage.getAssignedUsers()) {
                    notificationService.createNotification(
                            user.getId(),
                            "Escalated ticket requires your attention: " + request.getTitle(),
                            NotificationType.TICKET_ASSIGNED
                    );
                }
            }

        } else {
            log.info("[EscalationService] Request {} already at last stage",
                    request.getId());
        }

        // Mark escalated
        request.setEscalated(true);

        // Refresh priority
        request.setPriorityScore(priorityService.calculatePriorityScore(request));

        requestRepository.save(request);

        // 🧾 Audit log
        WorkflowStage stageToLog = nextStage != null ? nextStage : currentStage;

        historyService.saveHistory(
                request,
                stageToLog,
                null,
                Decision.ESCALATED,
                "Auto-escalated by system: SLA breached at " + request.getSlaDeadline()
        );

        // 🔔 Notify CREATOR also (important UX)
        if (request.getCreatedBy() != null) {
            notificationService.createNotification(
                    request.getCreatedBy().getId(),
                    "Your ticket has been escalated due to SLA breach: " + request.getTitle(),
                    NotificationType.SLA_ALERT
            );
        }
    }
}