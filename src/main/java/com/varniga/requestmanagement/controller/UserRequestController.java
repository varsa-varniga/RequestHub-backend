package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.CreateRequestDto;
import com.varniga.requestmanagement.dto.RequestResponseDto;
import com.varniga.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/requests")
@RequiredArgsConstructor
public class UserRequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto createRequest(@RequestBody CreateRequestDto dto,
                                            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }

        return requestService.createRequest(
                dto.getTitle(),
                dto.getDescription(),
                dto.getType(),
                dto.getUrgency(),
                userDetails.getUsername()
        );
    }

    @GetMapping
    public List<RequestResponseDto> getMyRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return requestService.getUserRequests(userDetails.getUsername());
    }
}