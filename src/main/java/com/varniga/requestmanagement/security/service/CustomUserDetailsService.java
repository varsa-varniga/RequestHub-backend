package com.varniga.requestmanagement.security.service;

import com.varniga.requestmanagement.entity.User;
import com.varniga.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String normalizedEmail = user.getEmail().toLowerCase();

        return org.springframework.security.core.userdetails.User.builder()
                .username(normalizedEmail)
                .password(user.getPassword())
                .authorities(
                        Collections.singletonList(
                                new SimpleGrantedAuthority(user.getRole().name()) // no ROLE_ prefix
                        )
                )
                .build();
    }
}