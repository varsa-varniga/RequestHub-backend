package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.entity.WorkflowStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final WorkflowService workflowService;

    public void validateApprover(Request request, User user) {

        WorkflowStage stage = workflowService.getCurrentStage(request);

        if (stage == null) {
            throw new RuntimeException("Request has no pending stage.");
        }

        // 1️⃣ Cannot approve own request
        if (request.getCreatedBy() != null && request.getCreatedBy().equals(user)) {
            throw new RuntimeException("You cannot approve your own request.");
        }

        // 2️⃣ Role check — user's role must match the stage's required role
        if (stage.getApproverRole() != null && !user.getRole().equals(stage.getApproverRole())) {
            throw new RuntimeException(
                    "Your role '" + user.getRole() + "' cannot approve this stage. Required role: "
                            + stage.getApproverRole()
            );
        }

        // 3️⃣ If specific users are assigned, user must be in that list
        boolean hasAssignedUsers = stage.getAssignedUsers() != null
                && !stage.getAssignedUsers().isEmpty();

        if (hasAssignedUsers && !stage.getAssignedUsers().contains(user)) {
            throw new RuntimeException(
                    "You are not assigned to approve this stage."
            );
        }
    }
}