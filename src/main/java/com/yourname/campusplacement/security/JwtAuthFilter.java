package com.yourname.campusplacement.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per incoming HTTP request, BEFORE Spring's normal auth filter.
 *
 * Job: read the "Authorization: Bearer <token>" header, validate the JWT,
 * and if valid, register the user as authenticated for this request in
 * Spring Security's SecurityContext — so downstream @PreAuthorize checks
 * and controller code know who's calling and what role they have.
 *
 * H-10 fix: exceptions are now categorised and logged (not silently swallowed).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token — let the request through; Security will reject it if the endpoint requires auth
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenValid(token, email)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException ex) {
            // Expected — token expired; request will be rejected by Security if endpoint requires auth
            log.debug("Expired JWT for request [{}]: {}", request.getRequestURI(), ex.getMessage());
        } catch (JwtException ex) {
            // Malformed, unsupported, or tampered token
            log.warn("Invalid JWT token for request [{}]: {}", request.getRequestURI(), ex.getMessage());
        } catch (Exception ex) {
            // Unexpected error in the filter chain — log it so we can debug
            log.error("Unexpected error in JwtAuthFilter for request [{}]", request.getRequestURI(), ex);
        }

        filterChain.doFilter(request, response);
    }
}
