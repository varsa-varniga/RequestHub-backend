package com.varniga.requestmanagement.entity;

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

    private String urgency;

    private Integer priorityScore;

    private Boolean escalated = false;

    private String priorityLevel; // P1, P2, P3

    private Integer currentStageOrder = 1; // tracks workflow progress

    private LocalDateTime slaDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo; // optional, can track who is currently approving

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

    // --------------------------
    // Helper Methods for Workflow
    // --------------------------

    /** Check if this request is in the last workflow stage */
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

    /** Move request to next stage, if exists */
    public void moveToNextStage() {
        if (getNextStage() != null) {
            currentStageOrder++;
        }
    }

    /** Get current workflow stage object */
    public WorkflowStage getCurrentStage() {
        if (workflow == null || workflow.getStages() == null) return null;

        return workflow.getStages().stream()
                .filter(stage -> stage.getStageOrder().equals(currentStageOrder))
                .findFirst()
                .orElse(null);
    }

    /** Get next workflow stage object */
    public WorkflowStage getNextStage() {
        if (workflow == null || workflow.getStages() == null) return null;

        int nextOrder = currentStageOrder + 1;

        return workflow.getStages().stream()
                .filter(stage -> stage.getStageOrder().equals(nextOrder))
                .findFirst()
                .orElse(null);
    }

    /** Get all users assigned to the current stage */
    public List<User> getAssignedUsersForCurrentStage() {
        WorkflowStage stage = getCurrentStage();
        return stage != null && stage.getAssignedUsers() != null ?
                List.copyOf(stage.getAssignedUsers()) : List.of();
    }

    /** Get all users assigned to the next stage */
    public List<User> getAssignedUsersForNextStage() {
        WorkflowStage stage = getNextStage();
        return stage != null && stage.getAssignedUsers() != null ?
                List.copyOf(stage.getAssignedUsers()) : List.of();
    }


}