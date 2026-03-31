package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.enums.Urgency;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Phase 2 – SLA Deadline Calculator
 *
 * SLA matrix:
 *
 *   Type       | Urgency       | Deadline
 *   -----------|---------------|----------
 *   HARDWARE   | HIGH          | +24 h
 *   HARDWARE   | MEDIUM / LOW  | +48 h
 *   SOFTWARE   | HIGH          | +12 h
 *   SOFTWARE   | MEDIUM / LOW  | +24 h
 *   (default)  | any           | +72 h
 *
 * Architecture note:
 *   Called once during request creation inside RequestService.
 *   The returned LocalDateTime is persisted on Request.slaDeadline.
 *   EscalationService then reads slaDeadline to trigger auto-escalation.
 */
@Service
public class SlaService {

    // ── SLA hours by type + urgency ──────────────────────────────────────────
    private static final int HARDWARE_HIGH_HOURS    = 24;
    private static final int HARDWARE_DEFAULT_HOURS = 48;

    private static final int SOFTWARE_HIGH_HOURS    = 12;
    private static final int SOFTWARE_DEFAULT_HOURS = 24;

    private static final int DEFAULT_HOURS = 72;

    /**
     * Calculate SLA deadline from now.
     *
     * @param requestType the type string stored on the Request (e.g. "HARDWARE")
     * @param urgency     the urgency string stored on the Request (e.g. "HIGH")
     * @return deadline as an absolute LocalDateTime
     */
    public LocalDateTime calculateDeadline(String requestType, String urgency) {
        LocalDateTime now = LocalDateTime.now();
        long hours = resolveHours(requestType, urgency);
        return now.plusHours(hours);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private long resolveHours(String requestType, String urgency) {
        boolean isHigh = isHighUrgency(urgency);

        if (requestType == null) return DEFAULT_HOURS;

        return switch (requestType.toUpperCase()) {
            case "HARDWARE" -> isHigh ? HARDWARE_HIGH_HOURS : HARDWARE_DEFAULT_HOURS;
            case "SOFTWARE" -> isHigh ? SOFTWARE_HIGH_HOURS : SOFTWARE_DEFAULT_HOURS;
            default         -> DEFAULT_HOURS;
        };
    }

    private boolean isHighUrgency(String urgency) {
        if (urgency == null) return false;
        try {
            return Urgency.valueOf(urgency.toUpperCase()) == Urgency.HIGH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}