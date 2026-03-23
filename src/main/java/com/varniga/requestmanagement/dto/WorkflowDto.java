package com.varniga.requestmanagement.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDto {
    private String name;
    private String requestType;
    private List<WorkflowStageDto> stages;
}