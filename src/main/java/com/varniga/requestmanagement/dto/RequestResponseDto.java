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
    private Integer stageNumber;       // Current approval stage
    private String approvalComment;    // Comment from approver
}