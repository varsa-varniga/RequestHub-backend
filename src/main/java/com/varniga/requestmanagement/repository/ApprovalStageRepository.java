package com.varniga.requestmanagement.repository;
import com.varniga.requestmanagement.entity.ApprovalStage;
import com.varniga.requestmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface ApprovalStageRepository extends JpaRepository<ApprovalStage, Long> {
    Optional<ApprovalStage> findByRequestAndStageNumber(Request request, Integer stageNumber);

    List<ApprovalStage> findByRequest(Request request);
}

