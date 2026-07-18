package com.yourname.campusplacement.controller;

import com.yourname.campusplacement.dto.ResumeAnalysisRequest;
import com.yourname.campusplacement.dto.ResumeAnalysisResponse;
import com.yourname.campusplacement.service.ResumeAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI Resume Analyzer Controller.
 *
 * Badge: "🤖 AI Feature (Beta)" — under development.
 *
 * POST /api/resume-analysis/analyze
 *  Accepts: resume PDF (multipart) + job description (form field)
 *  Returns: full analysis response with ATS score, gaps, and suggestions
 *
 * GET /api/resume-analysis/my
 *  Returns: student's past analysis history
 */
@RestController
@RequestMapping("/api/resume-analysis")
@PreAuthorize("hasRole('STUDENT')")
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeAnalysisController(ResumeAnalysisService resumeAnalysisService) {
        this.resumeAnalysisService = resumeAnalysisService;
    }

    /**
     * Upload resume PDF + paste job description → get analysis.
     * Uses multipart/form-data to accept both file and JSON/text fields.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            Authentication auth,
            @RequestPart("resume") MultipartFile resumeFile,
            @RequestPart("jobDescription") String jobDescription) {

        ResumeAnalysisRequest request = new ResumeAnalysisRequest();
        request.setJobDescription(jobDescription);

        return ResponseEntity.ok(resumeAnalysisService.analyze(auth.getName(), resumeFile, request));
    }

    /** View past analyses for this student */
    @GetMapping("/my")
    public ResponseEntity<List<ResumeAnalysisResponse>> getMyAnalyses(Authentication auth) {
        return ResponseEntity.ok(resumeAnalysisService.getMyAnalyses(auth.getName()));
    }
}
