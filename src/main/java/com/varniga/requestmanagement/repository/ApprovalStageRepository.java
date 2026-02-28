package com.varniga.requestmanagement.repository;



import com.varniga.requestmanagement.entity.ApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalStageRepository extends JpaRepository<ApprovalStage, Long> {
    // Optional: find all stages by request id
    // List<ApprovalStage> findByRequestId(Long requestId);
}