package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    // ── existing methods (keep as-is) ────────────────────────────────────────
    List<Request> findByCreatedBy(User user);

    // ── PHASE 2: sorted by priority score (highest first) ────────────────────
    List<Request> findAllByOrderByPriorityScoreDesc();

    // ── PHASE 2: used by EscalationService every hour ────────────────────────
    // Finds PENDING requests whose SLA deadline has passed and haven't been escalated yet.
    @Query("""
            SELECT r FROM Request r
            WHERE r.slaDeadline < :now
              AND r.escalated  = false
              AND r.status.name = :statusName
            """)
    List<Request> findBySlaDeadlineBeforeAndEscalatedFalseAndStatusName(
            @Param("statusName") String statusName,
            @Param("now")        LocalDateTime now
    );
}