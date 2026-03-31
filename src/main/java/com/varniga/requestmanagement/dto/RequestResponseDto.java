package com.varniga.requestmanagement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestResponseDto {

    private Long id;

    private String title;
    private String description;

    private String requestType;
    private String urgency;

    private String status;

    private String createdBy;

    // 🔥 Workflow visibility
    private Integer stageNumber;
    private String approvalComment;

    // 🔥 ADD THIS (important for admin + tracking)
    private String assignedTo;
}