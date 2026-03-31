package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.ApprovalHistory;
import com.varniga.requestmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByRequestOrderByStageOrderAsc(Request request);

    List<ApprovalHistory> findByRequestAndStageOrder(Request request, Integer stageOrder);
}