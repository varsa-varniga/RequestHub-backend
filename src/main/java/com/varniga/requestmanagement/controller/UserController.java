package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.CreateUserDto;
import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users") // anyone can hit this endpoint to create a user
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public User createUser(@RequestBody CreateUserDto dto) {

        if (dto.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole()) // now supports all roles
                .active(true)
                .build();

        return userRepository.save(user);
    }
}