package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.service.EscalationService;
import com.varniga.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 2 – Priority & Escalation endpoints.
 *
 * Base path : /priority
 * Auth      : Basic Auth (same as every other controller in this project)
 *
 * Endpoints
 * ─────────────────────────────────────────────────────────────────────────
 *  GET  /priority/requests          → all requests ordered by priorityScore DESC
 *  POST /priority/escalate/run      → manually trigger the escalation job (admin/testing)
 */
@RestController
@RequestMapping("/priority")
@RequiredArgsConstructor
public class PriorityController {

    private final RequestService    requestService;
    private final EscalationService escalationService;

    /**
     * Returns every request sorted by priorityScore descending.
     * Highest-priority (most urgent + longest pending) requests appear first.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<RequestResponseDto>> getRequestsByPriority() {
        return ResponseEntity.ok(requestService.getAllRequestsByPriority());
    }

    /**
     * Manually fires the escalation job.
     * Useful for testing without waiting an hour, or for admin intervention.
     * In production you may want to restrict this to ADMIN role only.
     */
    @PostMapping("/escalate/run")
    public ResponseEntity<String> triggerEscalation() {
        escalationService.runEscalationCheck();
        return ResponseEntity.ok("Escalation check completed. Check logs for details.");
    }
}