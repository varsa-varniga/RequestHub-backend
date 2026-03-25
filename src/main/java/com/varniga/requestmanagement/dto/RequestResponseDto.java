package com.varniga.requestmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO returned by RequestService and ApprovalService.
 *
 * PHASE 2 additions (marked ← NEW):
 *   - priorityScore  : calculated by PriorityService
 *   - slaDeadline    : calculated by SlaService at creation time
 *   - escalated      : set to true by EscalationService after SLA breach
 */
@Data
@Builder
public class RequestResponseDto {

    private Long    id;
    private String  title;
    private String  description;
    private String  requestType;
    private String  urgency;         // emitted as "LOW" / "MEDIUM" / "HIGH"
    private String  status;
    private String  createdBy;
    private Integer stageNumber;
    private String  approvalComment;
    private String  assignedTo;

    // ── PHASE 2 fields ────────────────────────────────────────────────────────
    private Integer       priorityScore;  // ← NEW
    private LocalDateTime slaDeadline;    // ← NEW
    private Boolean       escalated;      // ← NEW
}