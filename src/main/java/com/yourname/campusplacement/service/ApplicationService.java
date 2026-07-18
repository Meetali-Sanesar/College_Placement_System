package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.ApplicationResponse;
import com.yourname.campusplacement.entity.*;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PlacementDriveRepository driveRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                               PlacementDriveRepository driveRepository,
                               StudentProfileRepository studentProfileRepository,
                               UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.driveRepository = driveRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Student applies to a placement drive.
     * Checks:
     *  1. Drive is OPEN
     *  2. Deadline has not passed
     *  3. Student has not already applied
     *  4. Student has uploaded a resume
     *  5. Eligibility: CGPA and branch
     */
    @Transactional
    public ApplicationResponse applyToDrive(String email, Long driveId) {
        StudentProfile student = getStudentProfile(email);
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found"));

        if (drive.getStatus() == DriveStatus.CLOSED) {
            throw new BadRequestException("This placement drive is no longer accepting applications");
        }
        if (drive.getStatus() == DriveStatus.UPCOMING) {
            throw new BadRequestException("This placement drive is not yet open for applications");
        }
        if (drive.getDeadline() != null && drive.getDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("The application deadline for this drive has passed");
        }

        applicationRepository.findByDriveIdAndStudentId(driveId, student.getId()).ifPresent(a -> {
            throw new BadRequestException("You have already applied to this placement drive");
        });

        if (student.getResumeUrl() == null) {
            throw new BadRequestException("Please upload your resume before applying");
        }

        // Eligibility: CGPA check
        if (drive.getEligibilityCgpa() != null) {
            double studentCgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;
            if (studentCgpa < drive.getEligibilityCgpa()) {
                throw new BadRequestException(
                        "You do not meet the minimum CGPA requirement of " + drive.getEligibilityCgpa());
            }
        }

        // Eligibility: Branch check
        if (drive.getEligibleBranches() != null && !drive.getEligibleBranches().isBlank()) {
            String studentBranch = student.getBranch() != null ? student.getBranch().trim().toUpperCase() : "";
            boolean branchEligible = List.of(drive.getEligibleBranches().toUpperCase().split(","))
                    .stream()
                    .map(String::trim)
                    .anyMatch(b -> b.equals(studentBranch));
            if (!branchEligible) {
                throw new BadRequestException(
                        "Your branch is not eligible for this drive. Eligible branches: " + drive.getEligibleBranches());
            }
        }

        Application application = new Application();
        application.setDrive(drive);
        application.setStudent(student);
        applicationRepository.save(application);

        return toResponse(application);
    }

    /** Student views their own application history */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(String email, int page, int size) {
        StudentProfile student = getStudentProfile(email);
        List<ApplicationResponse> list = applicationRepository
                .findByStudentId(student.getId())
                .stream().map(this::toResponse).toList();
        int start = Math.min(page * size, list.size());
        int end   = Math.min(start + size, list.size());
        return new PageImpl<>(list.subList(start, end), PageRequest.of(page, size), list.size());
    }

    /** Admin views all applicants for a specific drive */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicantsForDrive(Long driveId, int page, int size) {
        if (!driveRepository.existsById(driveId)) {
            throw new ResourceNotFoundException("Placement drive not found");
        }
        List<ApplicationResponse> list = applicationRepository.findByDriveId(driveId)
                .stream().map(this::toResponse).toList();
        int start = Math.min(page * size, list.size());
        int end   = Math.min(start + size, list.size());
        return new PageImpl<>(list.subList(start, end), PageRequest.of(page, size), list.size());
    }

    /** Admin views all applications across all drives */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(int page, int size) {
        var pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("appliedAt").descending());
        return applicationRepository.findAll(pageable).map(this::toResponse);
    }

    /** Admin updates the status of an application */
    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        application.setStatus(newStatus);
        return toResponse(applicationRepository.save(application));
    }

    // --- helpers ---

    private StudentProfile getStudentProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getDrive().getId(),
                app.getDrive().getRole(),
                app.getDrive().getCompany().getName(),
                app.getStudent().getId(),
                app.getStudent().getUser().getFullName(),
                app.getStudent().getResumeUrl(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}
