package com.yourname.campusplacement.config;

import com.yourname.campusplacement.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central security configuration.
 *
 * Two roles: STUDENT and ADMIN (Placement Cell).
 * No RECRUITER role.
 *
 * Key rules:
 *  - STATELESS JWT auth — no server-side sessions.
 *  - CSRF disabled (not needed for stateless REST).
 *  - /uploads/** NOT public — resumes contain personal data.
 *  - Security headers: XSS, clickjacking, referrer policy.
 *  - CORS restricted to explicit origins.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .headers(headers -> headers
                .contentTypeOptions(x -> {})
                .frameOptions(f -> f.deny())
                .referrerPolicy(rp -> rp.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )

            .authorizeHttpRequests(auth -> auth
                // --- Public: auth endpoints ---
                .requestMatchers("/api/auth/**").permitAll()

                // --- Public: browse drives and companies (read-only) ---
                .requestMatchers(HttpMethod.GET, "/api/drives", "/api/drives/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/companies", "/api/companies/{id}").permitAll()

                // --- Public: static front-end files ---
                .requestMatchers("/", "/*.html", "/css/**", "/js/**",
                                  "/student/**", "/admin/**").permitAll()

                // --- Student endpoints ---
                .requestMatchers("/api/students/**").hasRole("STUDENT")
                .requestMatchers("/api/applications/apply/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/applications/my").hasRole("STUDENT")
                .requestMatchers("/api/resume-analysis/**").hasRole("STUDENT")

                // --- Admin only ---
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/companies").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/companies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/drives").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/drives/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/drives/**").hasRole("ADMIN")
                .requestMatchers("/api/drives/all").hasRole("ADMIN")

                // --- Resume download: authenticated only ---
                .requestMatchers("/uploads/**").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:8080",
            "http://localhost:3000",
            "https://your-production-domain.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
