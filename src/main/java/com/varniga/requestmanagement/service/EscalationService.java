package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.enums.NotificationType;
import com.varniga.requestmanagement.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationService {
    private final NotificationService notificationService;

    private final RequestRepository requestRepository;

    public EscalationService(RequestRepository requestRepository, NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    // ─────────────────────────────
    // 50% WARNING
    // ─────────────────────────────
    public void onFiftyPercent(Request request) {

        // ❗ if already at last stage → skip
        if (request.isLastStage()) {
            return;
        }

        List<User> approvers = request.getAssignedUsersForCurrentStage();

        if (!approvers.isEmpty()) {
            User first = approvers.get(0);

            System.out.println("⚠️ 50% SLA warning sent to: " + first.getName());
        }
    }

    // ─────────────────────────────
    // 75% AUTO ESCALATION (NEW)
    // ─────────────────────────────
    public void onSeventyFivePercent(Request request) {

        if (!request.isLastStage()) {

            request.moveToNextStage();

            System.out.println("⚡ 75% → Moved to NEXT STAGE: " + request.getId());

            List<User> users = request.getAssignedUsersForCurrentStage();

            for (User user : users) {

                notificationService.createNotification(
                        user.getId(),
                        "⚡ Request " + request.getId() + " assigned to you (75% escalation)",
                        NotificationType.SLA_ESCALATION
                );

                System.out.println("🔔 Notified next approver: " + user.getName());
            }

        } else {
            escalateToAdmin(request);
        }

        requestRepository.save(request);
    }

    // ─────────────────────────────
    // 100% ADMIN ESCALATION (MODIFIED)
    // ─────────────────────────────
    public void onSlaBreached(Request request) {

        System.out.println("🚨 SLA BREACH EVENT: " + request.getId());

        escalateToAdmin(request);

        requestRepository.save(request);
    }

    // ─────────────────────────────
    // EXISTING METHOD (LEFT SAFE)
    // ─────────────────────────────
    private void handleEscalation(Request request) {

        if (!request.isLastStage()) {

            request.moveToNextStage();

            System.out.println("🔁 Moved to next stage: " + request.getId());
        }
        else {
            escalateToAdmin(request);
        }

        requestRepository.save(request);
    }

    // ─────────────────────────────
    // OLD CHECK METHOD (UNCHANGED)
    // ─────────────────────────────
    @Transactional
    public void runEscalationCheck() {

        List<Request> requests = requestRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Request request : requests) {

            if (request.getSlaDeadline() == null || request.getCreatedAt() == null) {
                continue;
            }

            long totalDuration = java.time.Duration.between(
                    request.getCreatedAt(),
                    request.getSlaDeadline()
            ).toMillis();

            long elapsed = java.time.Duration.between(
                    request.getCreatedAt(),
                    now
            ).toMillis();

            if (totalDuration <= 0) continue;

            double percent = (elapsed * 100.0) / totalDuration;

            System.out.println("ID: " + request.getId() + " | SLA: " + percent + "%");

            if (percent >= 50 && !request.isHalfTimeWarned()) {

                request.setHalfTimeWarned(true);
                onFiftyPercent(request);
            }

            if (percent >= 100 && !request.isSlaBreachedHandled()) {

                request.setSlaBreachedHandled(true);
                onSlaBreached(request);
            }
        }

        requestRepository.saveAll(requests);
    }

    // ─────────────────────────────
    // FINAL ADMIN ESCALATION
    // ─────────────────────────────
    private void escalateToAdmin(Request request) {

        System.out.println("👑 FINAL ESCALATION TO ADMIN: " + request.getId());

        // MOVE to admin stage if not already there
        while (!request.isLastStage()) {
            request.moveToNextStage();
        }

        List<User> admins = request.getAssignedUsersForCurrentStage();

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "🚨 Request " + request.getId() + " breached SLA and escalated to ADMIN",
                    NotificationType.SLA_BREACH
            );
        }
    }
}