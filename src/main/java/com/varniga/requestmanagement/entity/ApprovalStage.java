package com.varniga.requestmanagement.entity;

import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "approval_stages")
public class ApprovalStage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer stageNumber;

    @Enumerated(EnumType.STRING)
    private Role approverRole;

    @Enumerated(EnumType.STRING)
    private Decision decision = Decision.PENDING;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime decidedAt;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
}

