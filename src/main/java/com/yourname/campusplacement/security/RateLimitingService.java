package com.yourname.campusplacement.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * M-14: Simple in-memory rate limiting to prevent brute force attacks on Auth endpoints.
 * In a real distributed system, use Redis / Bucket4j / Resilience4j.
 */
@Service
public class RateLimitingService {

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockouts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 mins

    public boolean isAllowed(String ip) {
        Long lockoutTime = lockouts.get(ip);
        if (lockoutTime != null) {
            if (System.currentTimeMillis() < lockoutTime) {
                return false; // Still locked out
            } else {
                // Lockout expired, reset
                lockouts.remove(ip);
                attempts.remove(ip);
            }
        }

        int currentAttempts = attempts.getOrDefault(ip, 0);
        if (currentAttempts >= MAX_ATTEMPTS) {
            lockouts.put(ip, System.currentTimeMillis() + LOCKOUT_DURATION_MS);
            return false;
        }

        attempts.put(ip, currentAttempts + 1);
        return true;
    }

    public void reset(String ip) {
        attempts.remove(ip);
        lockouts.remove(ip);
    }
}
