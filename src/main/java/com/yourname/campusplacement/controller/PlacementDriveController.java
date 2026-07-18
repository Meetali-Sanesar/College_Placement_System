package com.yourname.campusplacement.controller;

import com.yourname.campusplacement.dto.MessageResponse;
import com.yourname.campusplacement.dto.PlacementDriveRequest;
import com.yourname.campusplacement.dto.PlacementDriveResponse;
import com.yourname.campusplacement.service.PlacementDriveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Placement Drive endpoints.
 *
 * Public/Student:
 *  - GET /api/drives          → browse OPEN drives (with optional keyword search)
 *  - GET /api/drives/{id}     → view a specific drive
 *
 * Admin only:
 *  - GET /api/drives/all      → all drives (any status, with keyword filter)
 *  - POST /api/drives         → create a drive
 *  - PUT /api/drives/{id}     → update a drive
 *  - DELETE /api/drives/{id}  → delete a drive (cascades to applications)
 */
@RestController
@RequestMapping("/api/drives")
public class PlacementDriveController {

    private final PlacementDriveService driveService;

    public PlacementDriveController(PlacementDriveService driveService) {
        this.driveService = driveService;
    }

    /** Public: browse all OPEN drives (students and guests) */
    @GetMapping
    public ResponseEntity<Page<PlacementDriveResponse>> getOpenDrives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(driveService.getOpenDrives(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlacementDriveResponse> getDrive(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveById(id));
    }

    /** Admin: all drives regardless of status, with optional keyword filter */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<PlacementDriveResponse>> getAllDrives(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(driveService.getAllDrives(keyword, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlacementDriveResponse> createDrive(@Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driveService.createDrive(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PlacementDriveResponse> updateDrive(
            @PathVariable Long id, @Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.ok(driveService.updateDrive(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.ok(new MessageResponse("Placement drive deleted successfully"));
    }
}
