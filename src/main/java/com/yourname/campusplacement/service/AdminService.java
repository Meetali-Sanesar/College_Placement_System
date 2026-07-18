package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.AdminStatsResponse;
import com.yourname.campusplacement.dto.AdminStudentResponse;
import com.yourname.campusplacement.dto.CompanyResponse;
import com.yourname.campusplacement.dto.PlacementDriveResponse;
import com.yourname.campusplacement.entity.ApplicationStatus;
import com.yourname.campusplacement.entity.DriveStatus;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin (Placement Cell) service.
 * Provides student management, company management, drive management,
 * and dashboard statistics.
 */
@Service
public class AdminService {

    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final PlacementDriveService driveService;

    public AdminService(StudentProfileRepository studentProfileRepository,
                        CompanyRepository companyRepository,
                        PlacementDriveRepository driveRepository,
                        ApplicationRepository applicationRepository,
                        UserRepository userRepository,
                        CompanyService companyService,
                        PlacementDriveService driveService) {
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.driveService = driveService;
    }

    // ==================== STUDENT MANAGEMENT ====================

    public Page<AdminStudentResponse> getAllStudents(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword != null && !keyword.isBlank()) {
            return studentProfileRepository.findByUserFullNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
                    keyword, keyword, pageable).map(this::toStudentResponse);
        }
        return studentProfileRepository.findAll(pageable).map(this::toStudentResponse);
    }

    /**
     * Cascade delete: student applications → student profile → user account.
     * All in one transaction so partial failure leaves the DB consistent.
     */
    @Transactional
    public void deleteStudent(Long studentProfileId) {
        var profile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        applicationRepository.deleteByStudentId(profile.getId());
        studentProfileRepository.delete(profile);
        userRepository.delete(profile.getUser());
    }

    // ==================== COMPANY MANAGEMENT (delegated to CompanyService) ====================

    public Page<CompanyResponse> getAllCompanies(String keyword, int page, int size) {
        if (keyword != null && !keyword.isBlank()) {
            return companyService.searchCompanies(keyword, page, size);
        }
        return companyService.getAllCompanies(page, size);
    }

    // ==================== DRIVE MANAGEMENT (delegated to PlacementDriveService) ====================

    public Page<PlacementDriveResponse> getAllDrives(String keyword, int page, int size) {
        return driveService.getAllDrives(keyword, page, size);
    }

    // ==================== STATISTICS ====================

    public AdminStatsResponse getStats() {
        long totalStudents     = studentProfileRepository.count();
        long totalCompanies    = companyRepository.count();
        long totalDrives       = driveRepository.count();
        long openDrives        = driveRepository.countByStatus(DriveStatus.OPEN);
        long totalApplications = applicationRepository.count();
        long shortlisted       = applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED);
        long selected          = applicationRepository.countByStatus(ApplicationStatus.SELECTED);

        return new AdminStatsResponse(totalStudents, totalCompanies, totalDrives, openDrives,
                totalApplications, shortlisted, selected);
    }

    // ==================== HELPERS ====================

    private AdminStudentResponse toStudentResponse(com.yourname.campusplacement.entity.StudentProfile p) {
        return new AdminStudentResponse(
                p.getId(),
                p.getUser().getFullName(),
                p.getUser().getEmail(),
                p.getCollege(),
                p.getBranch(),
                p.getGraduationYear(),
                p.getCgpa()
        );
    }
}
