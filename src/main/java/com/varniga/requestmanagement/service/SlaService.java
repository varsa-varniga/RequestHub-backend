package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.SlaPolicy;
import com.varniga.requestmanagement.enums.Urgency;
import com.varniga.requestmanagement.entity.RequestType;
import com.varniga.requestmanagement.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    // ─────────────────────────────────────────────
    // MAIN METHOD (STRING BASED - BACKWARD COMPATIBLE)
    // ─────────────────────────────────────────────
    public LocalDateTime calculateDeadline(String requestType, String urgency) {

        LocalDateTime now = LocalDateTime.now();

        long hours = resolveHoursFromDb(requestType, urgency);

        return now.plusHours(hours);
    }

    // ─────────────────────────────────────────────
    // ENUM BASED VERSION (FUTURE SAFE)
    // ─────────────────────────────────────────────
    public LocalDateTime calculateDeadline(RequestType requestType, Urgency urgency) {

        LocalDateTime now = LocalDateTime.now();

        String type = requestType != null ? requestType.getName() : null;
        String urg = urgency != null ? urgency.name() : null;

        long hours = resolveHoursFromDb(type, urg);

        return now.plusHours(hours);
    }

    // ─────────────────────────────────────────────
    // CORE LOGIC (DB FIRST, FALLBACK SECOND)
    // ─────────────────────────────────────────────
    private long resolveHoursFromDb(String requestType, String urgency) {
        System.out.println("🔥 SLA DEBUG HIT: " + requestType + " | " + urgency);

        if (requestType == null || urgency == null) {
            long fallback = getDefaultHours();
            System.out.println("SLA fallback used = " + fallback + " hours");
            return fallback;
        }

        try {
            Urgency urgEnum = Urgency.valueOf(urgency.toUpperCase());

            long hours = slaPolicyRepository
                    .findByRequestTypeAndUrgency(requestType.toUpperCase(), urgEnum)
                    .map(SlaPolicy::getSlaHours)
                    .orElse(getDefaultHours());
            System.out.println("SLA POLICY FROM DB = " + hours);


            System.out.println(" SLA resolved hours = " + hours +
                    " | requestType=" + requestType +
                    " | urgency=" + urgency);

            return hours;

        } catch (Exception e) {
            long fallback = getDefaultHours();
            System.out.println(" SLA error, fallback used = " + fallback);
            return fallback;
        }
    }

    // ─────────────────────────────────────────────
    // FALLBACK RULE (SAFE BACKUP)
    // ─────────────────────────────────────────────
    private long getDefaultHours() {
        return 72;
    }

    // ─────────────────────────────────────────────
    // FUTURE ESCALATION SUPPORT
    // ─────────────────────────────────────────────
    public LocalDateTime extendDeadline(LocalDateTime currentDeadline, long extraHours) {
        if (currentDeadline == null) {
            return LocalDateTime.now().plusHours(extraHours);
        }
        return currentDeadline.plusHours(extraHours);
    }
}