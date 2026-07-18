package com.yourname.campusplacement.controller;

import com.yourname.campusplacement.dto.ApplicationResponse;
import com.yourname.campusplacement.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Student applies to a placement drive */
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/apply/{driveId}")
    public ResponseEntity<ApplicationResponse> apply(Authentication auth, @PathVariable Long driveId) {
        return ResponseEntity.ok(applicationService.applyToDrive(auth.getName(), driveId));
    }

    /** Student views their own application history */
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getMyApplications(auth.getName(), page, size));
    }
}
