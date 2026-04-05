package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.ApprovalHistory;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.entity.WorkflowStage;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.ApprovalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the audit trail stored in ApprovalHistory.
 *
 */
@Service
@RequiredArgsConstructor
public class ApprovalHistoryService {

    private final ApprovalHistoryRepository historyRepository;


    /**
     * Persist an approval / escalation event.
     *
     * @param request  the request being acted on
     * @param stage    current (or target) workflow stage; may be null for edge cases
     * @param actor    the user who acted; NULL when the action is system-generated (escalation)
     * @param decision outcome of the action
     * @param comment  human-readable description
     */
    public void saveHistory(Request request,
                            WorkflowStage stage,
                            User actor,          // nullable — null = system action
                            Decision decision,
                            String comment) {

        ApprovalHistory history = ApprovalHistory.builder()
                .request(request)
                // ✅ FIX 1: entity has stageOrder + stageName, NOT .stage()
                .stageOrder(stage != null ? stage.getStageOrder() : null)
                .stageName(stage != null ? stage.getStageName() : null)
                // ✅ FIX 2: entity has approvedBy, NOT .actedBy()
                .approvedBy(actor)
                .decision(decision)
                .comment(comment)
                // ✅ FIX 3: entity has decidedAt, NOT .actedAt()
                .decidedAt(LocalDateTime.now())
                // mark escalation events explicitly
                .escalatedAction(decision == Decision.ESCALATED)
                .build();

        historyRepository.save(history);
    }

    /**
     * Return the most recent history entry for a request, or null if none exist.
     */
    public List<ApprovalHistory> getHistoryByRequest(Request request) {
        return historyRepository.findByRequestOrderByCreatedAtAsc(request);
    }
    public ApprovalHistory getLatestHistory(Request request) {
        // ✅ FIX 4: sort by decidedAt (actual field), not actedAt
        return historyRepository
                .findTopByRequestOrderByDecidedAtDesc(request)
                .orElse(null);
    }
}