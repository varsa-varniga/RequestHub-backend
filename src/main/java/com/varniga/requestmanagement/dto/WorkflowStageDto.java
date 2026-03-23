package com.varniga.requestmanagement.dto;

import com.varniga.requestmanagement.enums.Role;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStageDto {
    private Integer stageOrder;
    private String stageName;
    private Role approverRole;
    private Set<Long> assignedUserIds;
}