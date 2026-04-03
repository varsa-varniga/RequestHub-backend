package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.AuthResponseDto;
import com.varniga.requestmanagement.dto.LoginRequestDto;
import com.varniga.requestmanagement.dto.RefreshTokenRequestDto;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.repository.UserRepository;
import com.varniga.requestmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // POST /auth/login — public
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /auth/refresh — public
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    // POST /auth/logout — authenticated
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequestDto request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    // GET /auth/me — authenticated
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
}