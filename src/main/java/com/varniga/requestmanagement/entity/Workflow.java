package com.varniga.requestmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workflows")
public class Workflow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id", nullable = false)
    private RequestType requestType;

    // ─────────────────────────────────────────────
    // WORKFLOW → STAGES (CONFIG DATA)
    // ─────────────────────────────────────────────
    @OneToMany(
            mappedBy = "workflow",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("stage_order ASC")
    private List<WorkflowStage> stages = new ArrayList<>();

    // ─────────────────────────────────────────────
    // WORKFLOW → REQUESTS (BUSINESS DATA)
    // ─────────────────────────────────────────────
    @OneToMany(mappedBy = "workflow")
    private List<Request> requests = new ArrayList<>();

    // ─────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────

    public void addStage(WorkflowStage stage) {
        if (stage == null) return;
        stage.setWorkflow(this);
        this.stages.add(stage);
    }

    public void removeStage(WorkflowStage stage) {
        if (stage == null) return;
        stage.setWorkflow(null);
        this.stages.remove(stage);
    }

    public void addRequest(Request request) {
        if (request == null) return;
        request.setWorkflow(this);

        if (!this.requests.contains(request)) {
            this.requests.add(request);
        }
    }

    public void removeRequest(Request request) {
        if (request == null) return;
        request.setWorkflow(null);
        this.requests.remove(request);
    }
}