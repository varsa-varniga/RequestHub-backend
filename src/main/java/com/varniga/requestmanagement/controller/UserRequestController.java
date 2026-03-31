package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.CreateRequestDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.dto.WorkflowStageDto;
import com.varniga.requestmanagement.dto.WorkflowStageResponseDto;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.service.RequestService;
import com.varniga.requestmanagement.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/requests")
@RequiredArgsConstructor
public class UserRequestController {

    private final RequestService requestService;
    @Autowired
    private WorkflowService workflowService;
    private final RequestRepository requestRepository;

    // ── CREATE NEW REQUEST ─────────────────────────────────────────────
    @PostMapping
    public RequestResponseDto createRequest(@RequestBody CreateRequestDto dto,
                                            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }

        return requestService.createRequest(dto, userDetails.getUsername());
    }

    // ── GET ALL REQUESTS OF LOGGED-IN USER ────────────────────────────
    @GetMapping
    public List<RequestResponseDto> getMyRequests(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        return requestService.getUserRequests(userDetails.getUsername());
    }

    //GET REQUEST WORKFLOW
    @GetMapping("/{id}/workflow")
    public ResponseEntity<List<WorkflowStageResponseDto>> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflowByRequestId(id));
    }


    // ── GET ALL REQUESTS SORTED BY PRIORITY ───────────────────────────
    @GetMapping("/priority")
    public List<RequestResponseDto> getRequestsByPriority(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        return requestService.getAllRequestsByPriority();
    }


}