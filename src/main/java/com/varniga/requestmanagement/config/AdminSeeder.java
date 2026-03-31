package com.varniga.requestmanagement.config;

import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.enums.Role;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1) // Optional: run before other seeders if needed
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@company.com";
        if (!userRepository.findByEmailIgnoreCase(adminEmail).isPresent()) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            System.out.println("✅ First admin created: " + adminEmail);
        }
    }
}