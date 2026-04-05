package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.*;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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

        Optional<User> existingUserOpt = userRepository.findByEmailIgnoreCase(dto.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // 🔥 CASE 1: Soft-deleted user → REACTIVATE
            if (!existingUser.isActive()) {
                existingUser.setActive(true);
                existingUser.setName(dto.getName());
                existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                existingUser.setRole(dto.getRole());

                User saved = userRepository.save(existingUser);
                return mapToDto(saved);
            }

            // 🔥 CASE 2: Already active user exists
            throw new RuntimeException("User already exists with this email");
        }

        // 🔥 CASE 3: New user
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

    //==========================CHANGE PASSWORD====================//
    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Long id,
                                 @RequestBody ChangePasswordDto dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // set new password (BCrypt hashed)
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.save(user);

        return "Password changed successfully";
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