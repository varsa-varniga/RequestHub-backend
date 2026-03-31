package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.*;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ================= CREATE USER =================
    @PostMapping
    public UserResponseDto createUser(@RequestBody CreateUserDto dto) {

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);

        return mapToDto(saved);
    }

    // ================= GET ALL USERS =================
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive) // ✔ only active users (important fix)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ================= GET USER BY ID =================
    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(user);
    }

    // ================= UPDATE USER =================
    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id,
                                      @RequestBody UpdateUserDto dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getActive() != null) user.setActive(dto.getActive());

        User updated = userRepository.save(user);

        return mapToDto(updated);
    }

    // ================= DELETE USER (FIXED - SOFT DELETE) =================
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✔ Soft delete instead of hard delete (FIX FOR FK ERROR)
        user.setActive(false);
        userRepository.save(user);

        return "User deactivated successfully";
    }

    // ================= MAPPER =================
    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}