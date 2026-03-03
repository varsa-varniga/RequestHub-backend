package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.enums.Decision;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalDecisionDto {
    private Decision decision; // APPROVED or REJECTED
    private String comment;
}