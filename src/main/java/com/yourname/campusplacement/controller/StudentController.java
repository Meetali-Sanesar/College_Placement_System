package com.yourname.campusplacement.controller;

import com.yourname.campusplacement.dto.MessageResponse;
import com.yourname.campusplacement.dto.StudentProfileRequest;
import com.yourname.campusplacement.entity.StudentProfile;
import com.yourname.campusplacement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfile> getProfile(Authentication auth) {
        return ResponseEntity.ok(studentService.getProfileByEmail(auth.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfile> updateProfile(
            Authentication auth, @Valid @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(studentService.updateProfile(auth.getName(), request));
    }

    /** Upload resume PDF — validates file type and size */
    @PostMapping("/resume")
    public ResponseEntity<StudentProfile> uploadResume(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentService.uploadResume(auth.getName(), file));
    }
}
