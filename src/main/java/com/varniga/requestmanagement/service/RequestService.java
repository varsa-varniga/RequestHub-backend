package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.ApprovalStage;
import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.RequestStatus;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.enums.Role;
import com.varniga.requestmanagement.repository.ApprovalStageRepository;
import com.varniga.requestmanagement.repository.RequestRepository;
import com.varniga.requestmanagement.repository.RequestStatusRepository;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository statusRepository;
    private final ApprovalStageRepository approvalStageRepository;

    // Create a new request
    public RequestResponseDto createRequest(String title,
                                            String description,
                                            String type,
                                            String urgency,
                                            String userEmail) {

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RequestStatus pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new RuntimeException("Status not found"));

        Request request = Request.builder()
                .title(title)
                .description(description)
                .requestType(type)
                .urgency(urgency)
                .createdBy(user)
                .status(pendingStatus)
                .currentStage(1)
                .build();

        Request saved = requestRepository.save(request);

        // Create initial approval stage
        ApprovalStage stage = ApprovalStage.builder()
                .stageNumber(1)
                .approverRole(Role.ADMIN)
                .request(saved)
                .build();

        approvalStageRepository.save(stage);

        return mapToDto(saved);
    }

    // Fetch all requests for a logged-in user
    public List<RequestResponseDto> getUserRequests(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return requestRepository.findByCreatedBy(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Fetch all requests (for admin)
    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Mapper method to convert Request -> RequestResponseDto
    private RequestResponseDto mapToDto(Request request) {
        ApprovalStage currentStage = approvalStageRepository
                .findByRequestAndStageNumber(request, request.getCurrentStage())
                .orElse(null);

        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requestType(request.getRequestType())
                .urgency(request.getUrgency())
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .stageNumber(currentStage != null ? currentStage.getStageNumber() : null)
                .approvalComment(currentStage != null ? currentStage.getComment() : null)
                .build();
    }
}