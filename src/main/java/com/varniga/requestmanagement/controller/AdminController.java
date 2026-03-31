package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.ApprovalDecisionDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.RequestStatus;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.repository.RequestStatusRepository;
import com.varniga.requestmanagement.repository.UserRepository;
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
    private final RequestRepository requestRepository;
    private final RequestStatusRepository statusRepository;
    private final UserRepository userRepository;

    // ✅ 1. View all requests
    @GetMapping("/requests")
    public List<RequestResponseDto> getAllRequests() {
        return requestService.getAllRequests();
    }

    // ✅ 2. Force approve / reject (bypass workflow)
    @PostMapping("/requests/{id}/force-decision")
    public RequestResponseDto forceDecision(@PathVariable Long id,
                                            @RequestBody ApprovalDecisionDto dto,
                                            @AuthenticationPrincipal UserDetails adminUser) {

        if (adminUser == null) {
            throw new RuntimeException("Admin not authenticated");
        }

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        RequestStatus status;

        if (dto.getDecision() == Decision.APPROVED) {
            status = statusRepository.findByName("APPROVED")
                    .orElseThrow(() -> new RuntimeException("Status not found"));
        } else {
            status = statusRepository.findByName("REJECTED")
                    .orElseThrow(() -> new RuntimeException("Status not found"));
        }

        request.setStatus(status);

        requestRepository.save(request);

        return requestService.getAllRequests()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    // ✅ 3. Update request (basic fields)
    @PutMapping("/requests/{id}")
    public RequestResponseDto updateRequest(@PathVariable Long id,
                                            @RequestBody Request updatedRequest) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setTitle(updatedRequest.getTitle());
        request.setDescription(updatedRequest.getDescription());
        request.setUrgency(updatedRequest.getUrgency());

        requestRepository.save(request);

        return requestService.getAllRequests()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    // ✅ 4. Reassign request to another user
    @PutMapping("/requests/{id}/assign/{userId}")
    public RequestResponseDto assignRequest(@PathVariable Long id,
                                            @PathVariable Long userId) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        request.setAssignedTo(user);

        requestRepository.save(request);

        return requestService.getAllRequests()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}