package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.ApprovalDecisionDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.service.ApprovalService;
import com.varniga.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApproverController {

    private final RequestService requestService;
    private final ApprovalService approvalService;

    // View all requests
    @GetMapping("/requests")
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    // Approve or Reject
    @PostMapping("/requests/{id}/decision")
    public ResponseEntity<RequestResponseDto> decide(
            @PathVariable Long id,
            @RequestBody ApprovalDecisionDto dto,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) throw new RuntimeException("User not authenticated");

        return ResponseEntity.ok(approvalService.decide(
                id, dto.getDecision(), dto.getComment(), user.getUsername()
        ));
    }

    // Resubmit a rejected request
    @PostMapping("/requests/{id}/resubmit")
    public ResponseEntity<RequestResponseDto> resubmit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) throw new RuntimeException("User not authenticated");

        return ResponseEntity.ok(approvalService.resubmit(id, user.getUsername()));
    }
    @GetMapping("/test")
    public String test() {
        return "working";
    }

    @GetMapping("/my")
    public ResponseEntity<List<RequestResponseDto>> getMyApprovals(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }

        return ResponseEntity.ok(
                requestService.getMyApprovals(user.getUsername(), status)
        );
    }
}