package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaMonitoringService {

    private final RequestRepository requestRepository;
    private final EscalationService escalationService;

    public SlaMonitoringService(RequestRepository requestRepository,
                                EscalationService escalationService) {
        this.requestRepository = requestRepository;
        this.escalationService = escalationService;
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void monitorSla() {

        List<Request> activeRequests = requestRepository.findAllActiveRequestsWithStages();

        if (activeRequests == null || activeRequests.isEmpty()) {
            return;
        }

        for (Request request : activeRequests) {
            evaluate(request);
        }
    }

    private void evaluate(Request request) {

        LocalDateTime start = request.getCreatedAt();
        LocalDateTime deadline = request.getSlaDeadline();
        LocalDateTime now = LocalDateTime.now();

        if (start == null || deadline == null) {
            System.out.println("❌ SLA SKIP NULL DATA ID: " + request.getId());
            return;
        }

//        if (start.isAfter(now)) {
//            System.out.println("⚠️ INVALID START TIME ID: " + request.getId());
//            return;
//        }

        long totalMinutes = Math.abs(Duration.between(start, deadline).toMinutes());

        if (totalMinutes <= 0) {
            System.out.println("❌ INVALID SLA WINDOW ID: " + request.getId());
            return;
        }

        long elapsedMinutes = Math.abs(Duration.between(start, now).toMinutes());

        double progress = (elapsedMinutes * 100.0) / totalMinutes;

        System.out.println("ID: " + request.getId() + " | SLA: " + progress + "%");

        // ─────────────────────────────
        // 50% WARNING
        // ─────────────────────────────
        if (!request.isHalfTimeWarned() && progress >= 50) {

            System.out.println("⚠️ 50% SLA WARNING: " + request.getId());

            request.setHalfTimeWarned(true);
            escalationService.onFiftyPercent(request);
        }

        // ─────────────────────────────
        // 75% AUTO ESCALATION
        // ─────────────────────────────
        if (!request.isEscalated() && progress >= 75 && progress < 100) {

            System.out.println("⚡ 75% AUTO ESCALATION: " + request.getId());

            request.setEscalated(true);
            escalationService.onSeventyFivePercent(request);
        }

        // ─────────────────────────────
        // 100% ADMIN ESCALATION
        // ─────────────────────────────
        if (!request.isSlaBreachedHandled() && progress >= 100) {

            System.out.println("🚨 SLA BREACH: " + request.getId());

            request.setSlaBreachedHandled(true);
            escalationService.onSlaBreached(request);
        }

        requestRepository.save(request);
    }
}