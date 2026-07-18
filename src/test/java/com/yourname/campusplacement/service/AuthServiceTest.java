package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.RegisterRequest;
import com.yourname.campusplacement.entity.Role;
import com.yourname.campusplacement.entity.User;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.repository.RecruiterProfileRepository;
import com.yourname.campusplacement.repository.StudentProfileRepository;
import com.yourname.campusplacement.repository.UserRepository;
import com.yourname.campusplacement.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * L-13: Unit tests for AuthService critical paths.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock RecruiterProfileRepository recruiterProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private RegisterRequest studentRequest;

    @BeforeEach
    void setUp() {
        studentRequest = new RegisterRequest();
        studentRequest.setFullName("Test Student");
        studentRequest.setEmail("test@student.com");
        studentRequest.setPassword("Test@1234");
        studentRequest.setRole(Role.STUDENT);
    }

    @Test
    @DisplayName("register() - success creates user and student profile")
    void register_successCreatesUserAndProfile() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@student.com");
        savedUser.setFullName("Test Student");
        savedUser.setRole(Role.STUDENT);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock.jwt.token");

        var response = authService.register(studentRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        verify(studentProfileRepository, times(1)).save(any());
        verify(recruiterProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - duplicate email throws BadRequestException")
    void register_duplicateEmailThrows() {
        when(userRepository.existsByEmail("test@student.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(studentRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - ADMIN role attempt throws BadRequestException")
    void register_adminRoleThrows() {
        studentRequest.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(studentRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - recruiter role creates recruiter profile (not student profile)")
    void register_recruiterRoleCreatesRecruiterProfile() {
        studentRequest.setRole(Role.RECRUITER);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setRole(Role.RECRUITER);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token");

        authService.register(studentRequest);

        verify(recruiterProfileRepository, times(1)).save(any());
        verify(studentProfileRepository, never()).save(any());
    }
}
