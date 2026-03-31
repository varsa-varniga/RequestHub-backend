package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.RequestType;
import com.varniga.requestmanagement.enums.Urgency;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PriorityService {

    private static final int WEIGHT_HARDWARE = 5;
    private static final int WEIGHT_SOFTWARE = 3;
    private static final int WEIGHT_DEFAULT  = 1;

    private static final int WEIGHT_HIGH   = 5;
    private static final int WEIGHT_MEDIUM = 3;
    private static final int WEIGHT_LOW    = 1;

    public int calculatePriorityScore(Request request) {

        int typeScore = resolveTypeScore(request.getRequestType());
        int urgencyScore = resolveUrgencyScore(request.getUrgency());
        int timeScore = resolveTimeScore(request.getCreatedAt());

        return typeScore + urgencyScore + timeScore;
    }

    private int resolveTypeScore(RequestType requestType) {

        if (requestType == null || requestType.getName() == null) {
            return WEIGHT_DEFAULT;
        }

        return switch (requestType.getName().toUpperCase()) {
            case "HARDWARE" -> WEIGHT_HARDWARE;
            case "SOFTWARE" -> WEIGHT_SOFTWARE;
            default -> WEIGHT_DEFAULT;
        };
    }

    private int resolveUrgencyScore(Urgency urgency) {

        if (urgency == null) return WEIGHT_LOW;

        return switch (urgency) {
            case HIGH -> WEIGHT_HIGH;
            case MEDIUM -> WEIGHT_MEDIUM;
            case LOW -> WEIGHT_LOW;
        };
    }

    private int resolveTimeScore(LocalDateTime createdAt) {

        if (createdAt == null) return 0;

        long hours = Duration.between(createdAt, LocalDateTime.now()).toHours();
        return (int) Math.max(0, hours);
    }
}