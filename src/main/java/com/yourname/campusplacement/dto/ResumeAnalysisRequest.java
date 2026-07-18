package com.yourname.campusplacement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for the AI Resume Analyzer endpoint.
 *
 * NOTE: This feature is currently in BETA / stub mode.
 * The service returns realistic dummy data until an AI API is integrated.
 */
@Getter
@Setter
public class ResumeAnalysisRequest {

    /**
     * The job description to match the resume against.
     * The resume PDF is uploaded as a multipart file alongside this DTO.
     */
    @NotBlank(message = "Job description is required")
    private String jobDescription;
}
