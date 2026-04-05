package com.varniga.requestmanagement.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ── KEY ──────────────────────────────────────────────────────
    private SecretKey getSigningKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── GENERATE ACCESS TOKEN ────────────────────────────────────
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Store the single authority (role) as a claim
        String role = userDetails.getAuthorities()
                .iterator().next().getAuthority();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── EXTRACT ALL CLAIMS ───────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── EXTRACT USERNAME (email) ─────────────────────────────────
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── EXTRACT ROLE ─────────────────────────────────────────────
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ── VALIDATE TOKEN ───────────────────────────────────────────
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}