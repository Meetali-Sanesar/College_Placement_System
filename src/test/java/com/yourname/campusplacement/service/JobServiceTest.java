package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.JobRequest;
import com.yourname.campusplacement.dto.JobResponse;
import com.yourname.campusplacement.entity.*;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.ApplicationRepository;
import com.yourname.campusplacement.repository.JobRepository;
import com.yourname.campusplacement.repository.RecruiterProfileRepository;
import com.yourname.campusplacement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private JobService jobService;

    private User recruiterUser;
    private RecruiterProfile recruiterProfile;
    private Job job;

    @BeforeEach
    void setUp() {
        recruiterUser = new User();
        recruiterUser.setId(1L);
        recruiterUser.setEmail("recruiter@test.com");
        recruiterUser.setRole(Role.RECRUITER);

        recruiterProfile = new RecruiterProfile();
        recruiterProfile.setId(10L);
        recruiterProfile.setUser(recruiterUser);
        recruiterProfile.setCompanyName("Tech Corp");

        job = new Job();
        job.setId(100L);
        job.setRecruiter(recruiterProfile);
        job.setTitle("Software Engineer");
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setPostedAt(LocalDateTime.now());
    }

    @Test
    void createJob_Success() {
        JobRequest request = new JobRequest();
        request.setTitle("Developer");
        request.setDescription("Desc");
        request.setLocation("NY");
        request.setSalary(100000.0);
        request.setJobType(JobType.FULL_TIME);
        request.setDeadline(LocalDate.now().plusDays(30));

        when(userRepository.findByEmail("recruiter@test.com")).thenReturn(Optional.of(recruiterUser));
        when(recruiterProfileRepository.findByUserId(1L)).thenReturn(Optional.of(recruiterProfile));
        
        Job savedJob = new Job();
        savedJob.setId(101L);
        savedJob.setRecruiter(recruiterProfile);
        savedJob.setTitle("Developer");
        savedJob.setJobType(JobType.FULL_TIME);
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        JobResponse response = jobService.createJob("recruiter@test.com", request);

        assertNotNull(response);
        assertEquals("Developer", response.getTitle());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void updateJob_NotOwner_ThrowsBadRequest() {
        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        RecruiterProfile otherRecruiter = new RecruiterProfile();
        otherRecruiter.setUser(otherUser);
        job.setRecruiter(otherRecruiter);

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        
        JobRequest request = new JobRequest();
        request.setTitle("Dev");
        request.setDescription("Desc");
        request.setLocation("NY");
        request.setSalary(100000.0);
        request.setJobType(JobType.FULL_TIME);
        request.setDeadline(LocalDate.now());
        
        assertThrows(BadRequestException.class, () -> jobService.updateJob("recruiter@test.com", 100L, request));
    }

    @Test
    void deleteJob_Admin_Success() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        
        jobService.deleteJob("admin@test.com", 100L, true);
        
        verify(applicationRepository).deleteByJobId(100L);
        verify(jobRepository).delete(job);
    }
}
