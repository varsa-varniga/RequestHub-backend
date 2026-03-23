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
    private String requestType;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stage_order ASC")
    private List<WorkflowStage> stages = new ArrayList<>();

    public void addStage(WorkflowStage stage) {
        stage.setWorkflow(this);
        this.stages.add(stage);
    }
}