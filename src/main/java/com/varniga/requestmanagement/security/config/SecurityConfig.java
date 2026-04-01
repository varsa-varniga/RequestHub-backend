package com.varniga.requestmanagement.security.config;

import com.varniga.requestmanagement.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ── AUTH PROVIDER ─────────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // ── SECURITY FILTER CHAIN ─────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers("/users").permitAll()

                        // Workflows (ADMIN only)
                        .requestMatchers("/workflows/**").hasAuthority("ADMIN")

                        // Requests module
                        .requestMatchers("/requests/**")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER")

                        // Approvals module
                        .requestMatchers("/approvals/**")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER", "IT", "COMPLIANCE")

                        // Delete request (user module)
                        .requestMatchers(HttpMethod.DELETE, "/user/requests/**")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER")

                        // User module
                        .requestMatchers("/user/**")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER")

                        // fallback
                        .anyRequest().authenticated()
                )

                // Basic Auth enabled
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // ── PASSWORD ENCODER ─────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}