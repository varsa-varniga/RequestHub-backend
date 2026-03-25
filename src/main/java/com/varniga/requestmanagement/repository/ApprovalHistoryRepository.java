package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.ApprovalHistory;
import com.varniga.requestmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    // existing — keep as-is
    List<ApprovalHistory> findByRequestOrderByStageOrderAsc(Request request);

    List<ApprovalHistory> findByRequestAndStageOrder(Request request, Integer stageOrder);

    // ✅ FIX 4: used by ApprovalHistoryService.getLatestHistory()
    // sorts by decidedAt (actual field name in ApprovalHistory entity)
    Optional<ApprovalHistory> findTopByRequestOrderByDecidedAtDesc(Request request);
}