package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    // ─────────────────────────────────────────────
    // BASIC
    // ─────────────────────────────────────────────

    List<Request> findByCreatedBy(User user);

    List<Request> findAllByOrderByPriorityScoreDesc();

    // ─────────────────────────────────────────────
    // SLA ESCALATION (FIXED)
    // ─────────────────────────────────────────────

    @Query("""
        SELECT r FROM Request r
        WHERE r.slaDeadline < :now
          AND r.escalated = false
          AND r.status.name = :statusName
    """)
    List<Request> findSlaBreached(
            @Param("now") LocalDateTime now,
            @Param("statusName") String statusName
    );


    @Query("""
SELECT r FROM Request r
JOIN FETCH r.currentStage cs
LEFT JOIN FETCH cs.assignedUsers
WHERE r.status.name <> 'RESOLVED'
""")
    List<Request> findAllActiveRequestsWithStages();

    // ─────────────────────────────────────────────
    // ✅ MY TASKS (CURRENT STAGE BASED - MAIN LOGIC)
    // ─────────────────────────────────────────────

    List<Request> findByCurrentStage_AssignedUsers_Email(String email);

    List<Request> findByCurrentStage_AssignedUsers_EmailAndStatus_Name(
            String email,
            String status
    );

    List<Request> findDistinctByWorkflow_Stages_AssignedUsers_Email(String email);
    List<Request> findDistinctByWorkflow_Stages_AssignedUsers_EmailAndStatus_Name(String email, String status);
    @Query("SELECT r FROM Request r WHERE r.status.name != 'RESOLVED'")
    List<Request> findAllActiveRequests();

    // ─────────────────────────────────────────────
    // OPTIONAL: HISTORY / AUDIT (ONLY IF NEEDED)
    // ─────────────────────────────────────────────

    List<Request> findByCreatedByAndDeletedFalse(User user);

    List<Request> findAllByDeletedFalseOrderByPriorityScoreDesc();

    List<Request> findByCurrentStage_AssignedUsers_EmailAndDeletedFalse(String email);

    List<Request> findByCurrentStage_AssignedUsers_EmailAndStatus_NameAndDeletedFalse(
            String email,
            String status
    );

    List<Request> findDistinctByWorkflow_Stages_AssignedUsers_EmailAndDeletedFalse(String email);

    List<Request> findDistinctByWorkflow_Stages_AssignedUsers_EmailAndStatus_NameAndDeletedFalse(
            String email,
            String status
    );
}