package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.entity.RequestType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDto {
    private String name;
    private String requestTypeCode;
    private List<WorkflowStageDto> stages;
}