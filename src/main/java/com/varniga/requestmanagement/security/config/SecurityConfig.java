package com.varniga.requestmanagement.security.config;
import jakarta.servlet.http.HttpServletResponse;

import com.varniga.requestmanagement.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC AUTH ENDPOINTS (IMPORTANT FIX)
                        .requestMatchers("/auth/**").permitAll()

                        // ✅ PUBLIC SYSTEM ENDPOINTS (IMPORTANT FIX)
                        .requestMatchers("/", "/health", "/error", "/error/**").permitAll()

                        // ✅ Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔐 AUTHENTICATED ENDPOINTS
                        .requestMatchers("/auth/me", "/auth/logout").authenticated()

                        // 🔐 ROLE-BASED
                        .requestMatchers("/workflows/**").hasAuthority("ADMIN")
                        .requestMatchers("/requests/**").hasAnyAuthority("USER", "ADMIN", "MANAGER")
                        .requestMatchers("/approvals/**")
                        .hasAnyAuthority("USER", "ADMIN", "MANAGER", "IT", "COMPLIANCE")
                        .requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN", "MANAGER")

                        // 🔒 EVERYTHING ELSE
                        .anyRequest().authenticated()
                )

                // 🔥 HANDLE AUTH ERRORS PROPERLY (VERY IMPORTANT)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                // ✅ JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ FIXED CORS CONFIG (IMPORTANT)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🔥 IMPORTANT: Replace with your actual Vercel frontend URL
        config.setAllowedOrigins(List.of(
                "https://request-hub-frontend.vercel.app"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true); // required for JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}