package com.varniga.requestmanagement.entity;

import com.varniga.requestmanagement.enums.Urgency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "requests")
public class Request extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String requestType;

    // ── PHASE 2 CHANGE ────────────────────────────────────────────────────────
    // urgency was String → now mapped as an @Enumerated column.
    // DB column stays VARCHAR so existing rows are not broken;
    // Hibernate stores the enum name ("LOW", "MEDIUM", "HIGH").
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", length = 20)
    private Urgency urgency;
    // ─────────────────────────────────────────────────────────────────────────

    // ── PHASE 2 FIELDS (already present in your entity — no structural change) ─
    private Integer  priorityScore;          // set by PriorityService
    private Boolean  escalated = false;      // set by EscalationService
    private String   priorityLevel;          // P1/P2/P3 — optional label
    private LocalDateTime slaDeadline;       // set by SlaService
    // ─────────────────────────────────────────────────────────────────────────

    private Integer currentStageOrder = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalHistory> approvalHistory;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // ── Workflow helpers (unchanged from your original) ───────────────────────

    public boolean isLastStage() {
        if (workflow == null || workflow.getStages() == null || workflow.getStages().isEmpty()) {
            return true;
        }
        int maxStage = workflow.getStages().stream()
                .mapToInt(WorkflowStage::getStageOrder)
                .max()
                .orElse(currentStageOrder);
        return currentStageOrder >= maxStage;
    }

    public void moveToNextStage() {
        if (getNextStage() != null) {
            currentStageOrder++;
        }
    }

    public WorkflowStage getCurrentStage() {
        if (workflow == null || workflow.getStages() == null) return null;
        return workflow.getStages().stream()
                .filter(stage -> stage.getStageOrder().equals(currentStageOrder))
                .findFirst()
                .orElse(null);
    }

    public WorkflowStage getNextStage() {
        if (workflow == null || workflow.getStages() == null) return null;
        int nextOrder = currentStageOrder + 1;
        return workflow.getStages().stream()
                .filter(stage -> stage.getStageOrder().equals(nextOrder))
                .findFirst()
                .orElse(null);
    }

    public List<User> getAssignedUsersForCurrentStage() {
        WorkflowStage stage = getCurrentStage();
        return stage != null && stage.getAssignedUsers() != null
                ? List.copyOf(stage.getAssignedUsers()) : List.of();
    }

    public List<User> getAssignedUsersForNextStage() {
        WorkflowStage stage = getNextStage();
        return stage != null && stage.getAssignedUsers() != null
                ? List.copyOf(stage.getAssignedUsers()) : List.of();
    }
}