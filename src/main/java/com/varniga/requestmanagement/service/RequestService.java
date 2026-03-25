package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.entity.*;
import com.varniga.requestmanagement.enums.Decision;
import com.varniga.requestmanagement.enums.Urgency;
import com.varniga.requestmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository      requestRepository;
    private final UserRepository         userRepository;
    private final RequestStatusRepository statusRepository;
    private final WorkflowRepository     workflowRepository;
    private final WorkflowService        workflowService;
    private final ApprovalHistoryService historyService;

    // ── PHASE 2: inject new services ─────────────────────────────────────────
    private final PriorityService priorityService;  // ← NEW
    private final SlaService      slaService;       // ← NEW
    // ─────────────────────────────────────────────────────────────────────────

    // ✅ CREATE REQUEST WITH DYNAMIC WORKFLOW
    public RequestResponseDto createRequest(String title,
                                            String description,
                                            String type,
                                            String urgencyStr,   // still accepted as String from controller
                                            String userEmail) {

        // 1️⃣ Fetch user
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Fetch PENDING status
        RequestStatus pendingStatus = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new RuntimeException("Status not found"));

        // 3️⃣ Fetch workflow matching request type
        Workflow workflow = workflowRepository.findByRequestType(type)
                .orElseThrow(() -> new RuntimeException("No workflow configured for request type: " + type));

        // 4️⃣ Get first stage
        WorkflowStage firstStage = workflow.getStages().stream()
                .filter(s -> s.getStageOrder() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No stage configured for first order"));

        // 5️⃣ Assign first approver
        User assignedTo = firstStage.getAssignedUsers() != null && !firstStage.getAssignedUsers().isEmpty()
                ? firstStage.getAssignedUsers().iterator().next()
                : null;

        // ── PHASE 2: convert urgency String → Enum ───────────────────────────
        Urgency urgency;
        try {
            urgency = Urgency.valueOf(urgencyStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Invalid urgency value: " + urgencyStr
                    + ". Accepted values: LOW, MEDIUM, HIGH");
        }
        // ─────────────────────────────────────────────────────────────────────

        // 6️⃣ Build request (urgency now stored as Enum)
        Request request = Request.builder()
                .title(title)
                .description(description)
                .requestType(type)
                .urgency(urgency)                                    // ← PHASE 2: Enum
                .createdBy(user)
                .status(pendingStatus)
                .workflow(workflow)
                .currentStageOrder(1)
                .assignedTo(assignedTo)
                .escalated(false)                                    // ← PHASE 2
                .build();

        // ── PHASE 2: set SLA deadline before first save ──────────────────────
        // urgency.name() gives the String the SlaService expects ("HIGH" etc.)
        request.setSlaDeadline(
                slaService.calculateDeadline(type, urgency.name())  // ← PHASE 2
        );
        // ─────────────────────────────────────────────────────────────────────

        // First save to get the generated id + createdAt from BaseEntity
        Request saved = requestRepository.save(request);

        // ── PHASE 2: calculate priorityScore after save (needs createdAt) ────
        saved.setPriorityScore(priorityService.calculatePriorityScore(saved)); // ← PHASE 2
        saved = requestRepository.save(saved);
        // ─────────────────────────────────────────────────────────────────────

        // 7️⃣ Create initial approval history
        historyService.saveHistory(saved, firstStage, user, Decision.PENDING, "Request created");

        return mapToDto(saved);
    }

    // ✅ FETCH USER REQUESTS (unchanged)
    public List<RequestResponseDto> getUserRequests(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return requestRepository.findByCreatedBy(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ FETCH ALL REQUESTS (unchanged)
    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── PHASE 2: NEW – fetch requests sorted by priority score descending ────
    public List<RequestResponseDto> getAllRequestsByPriority() {
        return requestRepository.findAllByOrderByPriorityScoreDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    // ─────────────────────────────────────────────────────────────────────────

    // ✅ MAP REQUEST → DTO  (urgency now serialised from Enum)
    private RequestResponseDto mapToDto(Request request) {
        WorkflowStage stage  = workflowService.getCurrentStage(request);
        ApprovalHistory latest = historyService.getLatestHistory(request);

        String assignedEmails = stage != null && stage.getAssignedUsers() != null
                ? stage.getAssignedUsers().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", "))
                : null;

        return RequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .requestType(request.getRequestType())
                // ── PHASE 2: urgency is Enum → emit its name string ──────────
                .urgency(request.getUrgency() != null ? request.getUrgency().name() : null)
                .status(request.getStatus() != null ? request.getStatus().getName() : "UNKNOWN")
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : "UNKNOWN")
                .stageNumber(stage != null ? stage.getStageOrder() : null)
                .approvalComment(latest != null ? latest.getComment() : null)
                .assignedTo(assignedEmails)
                // ── PHASE 2: new DTO fields ───────────────────────────────────
                .priorityScore(request.getPriorityScore())
                .slaDeadline(request.getSlaDeadline())
                .escalated(request.getEscalated())
                .build();
    }
}