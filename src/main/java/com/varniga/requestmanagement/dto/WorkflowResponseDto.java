package com.varniga.requestmanagement.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowResponseDto {
    private Long id;
    private String name;
    private String requestTypeName;
    private List<WorkflowStageResponseDto> stages;
}