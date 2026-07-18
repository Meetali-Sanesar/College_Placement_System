package com.yourname.campusplacement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for the AI Resume Analyzer.
 *
 * Badge: "AI Feature (Beta)" — this feature is under development.
 * Currently returns realistic dummy/sample data.
 * Ready to be connected to OpenAI, Gemini, or any AI API.
 */
@Getter
@AllArgsConstructor
public class ResumeAnalysisResponse {

    /** Whether this is real AI output or stub data */
    private boolean isAiPowered;

    /** Badge label to display in the UI */
    private String badge;

    private Long analysisId;
    private String resumeFileName;

    /** Estimated ATS match score, 0–100 */
    private int atsScore;

    /** Skills found in both resume and job description */
    private List<String> matchingSkills;

    /** Keywords from the job description missing in the resume */
    private List<String> missingKeywords;

    /** General resume improvement suggestions */
    private List<String> suggestions;

    /** Project-specific improvement suggestions */
    private List<String> projectSuggestions;

    /** Skills the student should learn to improve match */
    private List<String> skillsToLearn;

    /** Section-wise feedback map: section name → feedback text */
    private List<SectionFeedback> sectionFeedback;

    private LocalDateTime analyzedAt;

    @Getter
    @AllArgsConstructor
    public static class SectionFeedback {
        private String section;
        private String feedback;
    }
}
