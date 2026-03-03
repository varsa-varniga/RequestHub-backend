package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.ApprovalDecisionDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.service.ApprovalService;
import com.varniga.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RequestService requestService;
    private final ApprovalService approvalService;

    // View all requests
    @GetMapping("/requests")
    public List<RequestResponseDto> getAllRequests() {
        return requestService.getAllRequests();
    }

    // Approve / Reject a request
    @PostMapping("/requests/{id}/decision")
    public RequestResponseDto decide(@PathVariable Long id,
                                     @RequestBody ApprovalDecisionDto dto,
                                     @AuthenticationPrincipal UserDetails adminUser) {

        // Returns updated request DTO
        return approvalService.decide(id, dto.getDecision(), dto.getComment(), adminUser.getUsername());
    }
}