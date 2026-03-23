package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.ApprovalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalHistoryService {

    private final ApprovalHistoryRepository repository;

    public void saveHistory(Request request,
                            WorkflowStage stage,
                            User user,
                            Decision decision,
                            String comment) {

        ApprovalHistory history = ApprovalHistory.builder()
                .request(request)
                .stageOrder(stage.getStageOrder())
                .stageName(stage.getStageName())
                .approverRole(stage.getApproverRole())
                .decision(decision)
                .comment(comment)
                .approvedBy(user)
                .decidedAt(LocalDateTime.now())
                .build();

        repository.save(history);
    }

    // ✅ THIS WAS MISSING — NOW ADDED
    public ApprovalHistory getLatestHistory(Request request) {

        List<ApprovalHistory> historyList =
                repository.findByRequestOrderByStageOrderAsc(request);

        if (historyList.isEmpty()) {
            return null;
        }


        return historyList.get(historyList.size() - 1);
    }
}