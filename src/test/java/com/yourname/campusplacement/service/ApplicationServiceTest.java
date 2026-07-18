package com.yourname.campusplacement.service;

import com.yourname.campusplacement.entity.*;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * L-13: Unit tests for ApplicationService critical paths.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock JobRepository jobRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ApplicationService applicationService;

    private User user;
    private StudentProfile student;
    private Job openJob;
    private Job closedJob;
    private Job expiredJob;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("student@test.com");

        student = new StudentProfile();
        student.setId(1L);
        student.setUser(user);
        student.setResumeUrl("/uploads/resumes/test.pdf");

        RecruiterProfile recruiter = new RecruiterProfile();
        User recruiterUser = new User();
        recruiterUser.setEmail("recruiter@test.com");
        recruiter.setUser(recruiterUser);

        openJob = new Job();
        openJob.setId(10L);
        openJob.setStatus(JobStatus.OPEN);
        openJob.setDeadline(LocalDate.now().plusDays(10));
        openJob.setRecruiter(recruiter);

        closedJob = new Job();
        closedJob.setId(11L);
        closedJob.setStatus(JobStatus.CLOSED);
        closedJob.setRecruiter(recruiter);

        expiredJob = new Job();
        expiredJob.setId(12L);
        expiredJob.setStatus(JobStatus.OPEN);
        // Deadline in the past — H-7 fix
        expiredJob.setDeadline(LocalDate.now().minusDays(1));
        expiredJob.setRecruiter(recruiter);
    }

    @Test
    @DisplayName("applyToJob() - success when job is open and not expired")
    void applyToJob_success() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.findByJobIdAndStudentId(10L, 1L)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> applicationService.applyToJob("student@test.com", 10L));
        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("applyToJob() - throws when job is CLOSED")
    void applyToJob_closedJobThrows() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(11L)).thenReturn(Optional.of(closedJob));

        assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("student@test.com", 11L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyToJob() - throws when deadline has passed (H-7)")
    void applyToJob_expiredDeadlineThrows() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(12L)).thenReturn(Optional.of(expiredJob));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("student@test.com", 12L));
        assertTrue(ex.getMessage().contains("deadline"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyToJob() - throws on duplicate application")
    void applyToJob_duplicateThrows() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.findByJobIdAndStudentId(10L, 1L))
                .thenReturn(Optional.of(new Application()));

        assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("student@test.com", 10L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyToJob() - throws when student has no resume")
    void applyToJob_noResumeThrows() {
        student.setResumeUrl(null);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.findByJobIdAndStudentId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("student@test.com", 10L));
    }
}
