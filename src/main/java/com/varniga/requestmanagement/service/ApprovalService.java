package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.ApprovalStage;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.RequestStatus;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.repository.ApprovalStageRepository;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.repository.RequestStatusRepository;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalStageRepository approvalStageRepository;
    private final RequestRepository requestRepository;
    private final RequestStatusRepository statusRepository;
    private final UserRepository userRepository;

    // Approve / Reject a request
    public RequestResponseDto decide(Long requestId,
                                     Decision decision,
                                     String comment,
                                     String adminEmail) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        ApprovalStage stage = approvalStageRepository
                .findByRequestAndStageNumber(request, request.getCurrentStage())
                .orElseThrow(() -> new RuntimeException("Approval stage not found"));

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        stage.setDecision(decision);
        stage.setComment(comment);
        stage.setDecidedAt(LocalDateTime.now());
        stage.setApprovedBy(admin);

        approvalStageRepository.save(stage);

        // Update request status
        RequestStatus newStatus;
        if (decision == Decision.APPROVED) {
            newStatus = statusRepository.findByName("APPROVED")
                    .orElseThrow(() -> new RuntimeException("APPROVED status not found"));
        } else {
            newStatus = statusRepository.findByName("REJECTED")
                    .orElseThrow(() -> new RuntimeException("REJECTED status not found"));
        }
        request.setStatus(newStatus);
        requestRepository.save(request);

        // Return updated request DTO
        return mapToDto(request);
    }

    private RequestResponseDto mapToDto(Request request) {
        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requestType(request.getRequestType())
                .urgency(request.getUrgency())
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .build();
    }
}