package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.enums.Urgency;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Phase 2 – Smart Priority Engine
 *
 * Calculates a numeric priorityScore for a Request based on:
 *   - requestType  : HARDWARE=5, SOFTWARE=3, anything else=1
 *   - urgency      : HIGH=5,     MEDIUM=3,   LOW=1
 *   - time pending : +1 per hour elapsed since createdAt (drives escalation urgency)
 *
 * Architecture note:
 *   Called by RequestService (on creation) and ApprovalService (after every decision)
 *   so the score stays fresh throughout the lifecycle.
 */
@Service
public class PriorityService {

    // ── requestType weights ──────────────────────────────────────────────────
    private static final int WEIGHT_HARDWARE = 5;
    private static final int WEIGHT_SOFTWARE = 3;
    private static final int WEIGHT_DEFAULT  = 1;

    // ── urgency weights ──────────────────────────────────────────────────────
    private static final int WEIGHT_HIGH   = 5;
    private static final int WEIGHT_MEDIUM = 3;
    private static final int WEIGHT_LOW    = 1;

    /**
     * Compute and return the priority score.
     * Does NOT mutate the request — the caller is responsible for persisting the value.
     *
     * @param request the Request whose score is being calculated
     * @return total integer priority score  (higher = more urgent)
     */
    public int calculatePriorityScore(Request request) {
        int typeScore    = resolveTypeScore(request.getRequestType());
        int urgencyScore = resolveUrgencyScore(String.valueOf(request.getUrgency()));
        int timeScore    = resolveTimeScore(request.getCreatedAt());

        return typeScore + urgencyScore + timeScore;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private int resolveTypeScore(String requestType) {
        if (requestType == null) return WEIGHT_DEFAULT;
        return switch (requestType.toUpperCase()) {
            case "HARDWARE" -> WEIGHT_HARDWARE;
            case "SOFTWARE" -> WEIGHT_SOFTWARE;
            default         -> WEIGHT_DEFAULT;
        };
    }

    /**
     * Accepts the urgency stored as an Urgency enum.
     * The Request entity keeps urgency as a String in the DB; callers must ensure
     * it has already been converted to the enum before reaching this method.
     */
    private int resolveUrgencyScore(String urgency) {
        if (urgency == null) return WEIGHT_LOW;
        try {
            return switch (Urgency.valueOf(urgency.toUpperCase())) {
                case HIGH   -> WEIGHT_HIGH;
                case MEDIUM -> WEIGHT_MEDIUM;
                case LOW    -> WEIGHT_LOW;
            };
        } catch (IllegalArgumentException e) {
            return WEIGHT_LOW; // unknown value — treat as lowest priority
        }
    }

    /**
     * +1 point for every full hour the request has been pending.
     * If createdAt is null (e.g. unsaved entity in tests), returns 0.
     */
    private int resolveTimeScore(LocalDateTime createdAt) {
        if (createdAt == null) return 0;
        long hours = Duration.between(createdAt, LocalDateTime.now()).toHours();
        return (int) Math.max(0, hours); // guard against clock skew / negative
    }
}