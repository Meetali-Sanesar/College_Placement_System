package com.yourname.campusplacement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores the result of a resume analysis request.
 *
 * NOTE: This entity is part of the AI Resume Analyzer feature which is currently
 * in STUB/BETA mode. The service returns realistic dummy data.
 * This architecture is ready to be connected to OpenAI or another AI API.
 *
 * Fields store the analysis output in a denormalized form (JSON strings or
 * comma-separated values) so no extra join tables are needed for MVP.
 */
@Entity
@Table(name = "resume_analyses")
@Getter
@Setter
@NoArgsConstructor
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    /** Original filename of the uploaded resume */
    private String resumeFileName;

    /** The job description pasted by the student for analysis */
    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    /** ATS match score, 0–100 */
    private Integer atsScore;

    /** Comma-separated list of matching skills found */
    @Column(columnDefinition = "TEXT")
    private String matchingSkills;

    /** Comma-separated list of missing keywords */
    @Column(columnDefinition = "TEXT")
    private String missingKeywords;

    /** Newline-separated list of improvement suggestions */
    @Column(columnDefinition = "TEXT")
    private String suggestions;

    /** Newline-separated project improvement suggestions */
    @Column(columnDefinition = "TEXT")
    private String projectSuggestions;

    /** Comma-separated list of skills to learn */
    @Column(columnDefinition = "TEXT")
    private String skillsToLearn;

    /** Section-wise feedback in key:value format (newline-separated) */
    @Column(columnDefinition = "TEXT")
    private String sectionFeedback;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
}
