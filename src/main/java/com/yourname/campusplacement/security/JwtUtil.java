package com.yourname.campusplacement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Handles creating and validating JWT tokens.
 *
 * A JWT has 3 parts: header.payload.signature
 *  - Header: algorithm used (HS256)
 *  - Payload: claims — here, the user's email (subject) and role
 *  - Signature: HMAC of header+payload using our secret key, proving
 *    the token wasn't tampered with.
 *
 * M-11: Updated to JJWT 0.12.6 non-deprecated API.
 * M-12: Signing key is cached via @PostConstruct instead of being
 *        rebuilt on every call (previously called on every request).
 *        Also explicitly uses UTF-8 encoding to avoid platform-default issues.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /** Cached signing key — computed once at startup, reused for every JWT operation. */
    private SecretKey signingKey;

    @PostConstruct
    private void init() {
        // M-12: Build key once and cache; getBytes() with explicit UTF-8 charset
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        // M-11: JJWT 0.12.x fluent API (replaces deprecated setSubject, signWith(key, alg))
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String email) {
        return extractEmail(token).equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        // M-11: JJWT 0.12.x parser API (replaces deprecated parserBuilder/parseClaimsJws)
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
