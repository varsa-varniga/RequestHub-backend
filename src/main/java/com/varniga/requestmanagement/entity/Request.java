package com.varniga.requestmanagement.entity;

import com.varniga.requestmanagement.enums.Urgency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", length = 20)
    private Urgency urgency;

    // ─────────────────────────────────────────────
    // WORKFLOW FIELDS
    // ─────────────────────────────────────────────

    private Integer priorityScore;

    @Builder.Default
    private Boolean escalated = false;

    private String priorityLevel;

    private LocalDateTime slaDeadline;

    // optional fallback/debug only
    @Builder.Default
    private Integer currentStageOrder = 1;

    // ─────────────────────────────────────────────
    // RELATIONS
    // ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id")
    private WorkflowStage currentStage;

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

    // ─────────────────────────────────────────────
    // CHILD TABLES
    // ─────────────────────────────────────────────

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalHistory> approvalHistory = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalStage> approvalStages = new ArrayList<>();

    // ─────────────────────────────────────────────
    // WORKFLOW LOGIC
    // ─────────────────────────────────────────────

    public WorkflowStage getNextStage() {
        if (workflow == null || workflow.getStages() == null || currentStage == null) return null;

        int nextOrder = currentStage.getStageOrder() + 1;

        return workflow.getStages().stream()
                .filter(stage -> stage.getStageOrder().equals(nextOrder))
                .findFirst()
                .orElse(null);
    }

    public boolean isLastStage() {
        if (workflow == null || workflow.getStages() == null || currentStage == null) {
            return true;
        }

        int maxStage = workflow.getStages().stream()
                .mapToInt(WorkflowStage::getStageOrder)
                .max()
                .orElse(currentStage.getStageOrder());

        return currentStage.getStageOrder() >= maxStage;
    }

    public void moveToNextStage() {
        WorkflowStage next = getNextStage();

        if (next != null) {
            this.currentStage = next;
            this.currentStageOrder = next.getStageOrder();
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    public List<User> getAssignedUsersForCurrentStage() {
        if (currentStage == null || currentStage.getAssignedUsers() == null) {
            return List.of();
        }
        return List.copyOf(currentStage.getAssignedUsers());
    }

    public List<User> getAssignedUsersForNextStage() {
        WorkflowStage next = getNextStage();

        if (next == null || next.getAssignedUsers() == null) {
            return List.of();
        }
        return List.copyOf(next.getAssignedUsers());
    }
}