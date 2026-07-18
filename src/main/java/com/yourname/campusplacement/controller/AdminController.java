package com.yourname.campusplacement.controller;

import com.yourname.campusplacement.dto.*;
import com.yourname.campusplacement.service.AdminService;
import com.yourname.campusplacement.service.ApplicationService;
import com.yourname.campusplacement.service.CompanyService;
import com.yourname.campusplacement.service.PlacementDriveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin (Placement Cell) REST controller.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final CompanyService companyService;
    private final PlacementDriveService driveService;
    private final ApplicationService applicationService;

    public AdminController(AdminService adminService,
                           CompanyService companyService,
                           PlacementDriveService driveService,
                           ApplicationService applicationService) {
        this.adminService = adminService;
        this.companyService = companyService;
        this.driveService = driveService;
        this.applicationService = applicationService;
    }

    // ==================== DASHBOARD STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ==================== STUDENT MANAGEMENT ====================

    @GetMapping("/students")
    public ResponseEntity<Page<AdminStudentResponse>> getAllStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllStudents(keyword, page, size));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<MessageResponse> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok(new MessageResponse("Student removed successfully"));
    }

    // ==================== COMPANY MANAGEMENT ====================

    @GetMapping("/companies")
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllCompanies(keyword, page, size));
    }

    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(request));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<MessageResponse> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(new MessageResponse("Company deleted successfully"));
    }

    // ==================== PLACEMENT DRIVE MANAGEMENT ====================

    @GetMapping("/drives")
    public ResponseEntity<Page<PlacementDriveResponse>> getAllDrives(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(driveService.getAllDrives(keyword, page, size));
    }

    @PostMapping("/drives")
    public ResponseEntity<PlacementDriveResponse> createDrive(@Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driveService.createDrive(request));
    }

    @PutMapping("/drives/{id}")
    public ResponseEntity<PlacementDriveResponse> updateDrive(
            @PathVariable Long id, @Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.ok(driveService.updateDrive(id, request));
    }

    @DeleteMapping("/drives/{id}")
    public ResponseEntity<MessageResponse> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.ok(new MessageResponse("Placement drive deleted successfully"));
    }

    // ==================== APPLICATION MANAGEMENT ====================

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(applicationService.getAllApplications(page, size));
    }

    @GetMapping("/applications/drive/{driveId}")
    public ResponseEntity<Page<ApplicationResponse>> getApplicantsForDrive(
            @PathVariable Long driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(applicationService.getApplicantsForDrive(driveId, page, size));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }
}
