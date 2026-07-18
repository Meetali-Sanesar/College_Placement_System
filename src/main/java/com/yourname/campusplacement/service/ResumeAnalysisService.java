package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.ResumeAnalysisRequest;
import com.yourname.campusplacement.dto.ResumeAnalysisResponse;
import com.yourname.campusplacement.entity.ResumeAnalysis;
import com.yourname.campusplacement.entity.StudentProfile;
import com.yourname.campusplacement.entity.User;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.ResumeAnalysisRepository;
import com.yourname.campusplacement.repository.StudentProfileRepository;
import com.yourname.campusplacement.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * ============================================================
 * AI RESUME ANALYZER — STUB IMPLEMENTATION (Beta)
 * ============================================================
 * This service is architected and ready for real AI integration.
 *
 * TO CONNECT A REAL AI API:
 *  1. Add your API key to application.properties (e.g., openai.api-key=${OPENAI_KEY})
 *  2. Inject a RestTemplate / WebClient here
 *  3. Replace the generateDummyAnalysis() call with a real API call
 *  4. Parse the AI response into the ResumeAnalysisResponse DTO
 *
 * Current behaviour: returns realistic dummy/sample analysis so the
 * frontend works end-to-end while AI integration is pending.
 * ============================================================
 */
@Service
public class ResumeAnalysisService {

    private static final long MAX_RESUME_SIZE = 5L * 1024 * 1024; // 5 MB

    private final ResumeAnalysisRepository analysisRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public ResumeAnalysisService(ResumeAnalysisRepository analysisRepository,
                                  StudentProfileRepository studentProfileRepository,
                                  UserRepository userRepository) {
        this.analysisRepository = analysisRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Accepts a resume PDF and a job description, runs analysis (currently stub),
     * persists the result, and returns the analysis response.
     */
    @Transactional
    public ResumeAnalysisResponse analyze(String email, MultipartFile resumeFile,
                                           ResumeAnalysisRequest request) {
        // Validate the uploaded file
        validateResumeFile(resumeFile);

        StudentProfile student = getStudentProfile(email);

        // Extract text from the uploaded PDF
        String resumeText = extractTextFromPdf(resumeFile);

        // Generate analysis — using dynamic text-based analysis
        // TODO: Replace with real AI call when integrating OpenAI/Gemini
        ResumeAnalysisResponse dummyAnalysis = performTextBasedAnalysis(
                resumeText,
                student,
                Objects.requireNonNull(resumeFile.getOriginalFilename()),
                request.getJobDescription()
        );

        // Persist the analysis to DB
        ResumeAnalysis entity = new ResumeAnalysis();
        entity.setStudent(student);
        entity.setResumeFileName(resumeFile.getOriginalFilename());
        entity.setJobDescription(request.getJobDescription());
        entity.setAtsScore(dummyAnalysis.getAtsScore());
        entity.setMatchingSkills(String.join(", ", dummyAnalysis.getMatchingSkills()));
        entity.setMissingKeywords(String.join(", ", dummyAnalysis.getMissingKeywords()));
        entity.setSuggestions(String.join("\n", dummyAnalysis.getSuggestions()));
        entity.setProjectSuggestions(String.join("\n", dummyAnalysis.getProjectSuggestions()));
        entity.setSkillsToLearn(String.join(", ", dummyAnalysis.getSkillsToLearn()));
        entity.setSectionFeedback(buildSectionFeedbackString(dummyAnalysis.getSectionFeedback()));
        ResumeAnalysis saved = analysisRepository.save(entity);

        // Return response with the persisted ID
        return new ResumeAnalysisResponse(
                false,                              // isAiPowered — false until real AI is connected
                "🤖 AI Feature (Beta)",
                saved.getId(),
                resumeFile.getOriginalFilename(),
                dummyAnalysis.getAtsScore(),
                dummyAnalysis.getMatchingSkills(),
                dummyAnalysis.getMissingKeywords(),
                dummyAnalysis.getSuggestions(),
                dummyAnalysis.getProjectSuggestions(),
                dummyAnalysis.getSkillsToLearn(),
                dummyAnalysis.getSectionFeedback(),
                saved.getAnalyzedAt()
        );
    }

    /** Get a student's past analyses */
    @Transactional(readOnly = true)
    public List<ResumeAnalysisResponse> getMyAnalyses(String email) {
        StudentProfile student = getStudentProfile(email);
        return analysisRepository.findByStudentIdOrderByAnalyzedAtDesc(student.getId())
                .stream()
                .map(this::entityToResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // STUB: Generates dynamic analysis based on text matching
    // Replace this method body with a real AI API call
    // ----------------------------------------------------------------
    private String extractTextFromPdf(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            System.err.println("Failed to parse PDF: " + e.getMessage());
            return "";
        }
    }

    private ResumeAnalysisResponse performTextBasedAnalysis(String resumeText, StudentProfile student, String fileName, String jobDescription) {
        String[] jobWords = jobDescription.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        Set<String> jdKeywords = new HashSet<>(Arrays.asList(jobWords));
        
        // Some common tech keywords to check against if they appear in JD
        List<String> commonTech = Arrays.asList("java", "python", "react", "angular", "spring", "node", "sql", "mysql", "docker", "aws", "git", "kubernetes", "c++", "c#", "javascript", "html", "css", "linux", "agile", "mongodb", "rest", "backend", "frontend", "fullstack", "cloud", "api");
        
        List<String> requiredSkills = new ArrayList<>();
        for (String tech : commonTech) {
            if (jdKeywords.contains(tech)) {
                requiredSkills.add(tech);
            }
        }
        
        if (requiredSkills.isEmpty()) {
            requiredSkills.addAll(Arrays.asList("communication", "problem-solving", "teamwork", "leadership")); // defaults
        }

        String combinedSkillsStr = (resumeText + " " + (student.getSkills() != null ? student.getSkills() : "")).toLowerCase();
        List<String> matchingSkills = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();
        
        for (String req : requiredSkills) {
            if (combinedSkillsStr.contains(req)) {
                matchingSkills.add(req);
            } else {
                missingKeywords.add(req);
            }
        }
        
        int atsScore = 40;
        if (!requiredSkills.isEmpty()) {
            atsScore += (int)(((double)matchingSkills.size() / requiredSkills.size()) * 40);
        }
        
        // Add some points for having a good CGPA
        if (student.getCgpa() != null && student.getCgpa() >= 8.0) {
            atsScore += 10;
        } else if (student.getCgpa() != null && student.getCgpa() >= 7.0) {
            atsScore += 5;
        }
        
        // Cap score between 40 and 98
        if (atsScore > 98) atsScore = 98;
        if (atsScore < 40) atsScore = 40;
        
        List<String> suggestions = new ArrayList<>();
        if (!missingKeywords.isEmpty()) {
            suggestions.add("Consider adding these keywords to your resume if you have experience: " + String.join(", ", missingKeywords));
        } else {
            suggestions.add("Great job! Your skills align perfectly with the key requirements.");
        }
        suggestions.add("Use action verbs to start each bullet point (Developed, Designed, Implemented...).");
        suggestions.add("Quantify project impact wherever possible.");
        
        List<String> projectSuggestions = new ArrayList<>();
        projectSuggestions.add("Add a live demo link or GitHub URL for each project.");
        if (jdKeywords.contains("backend") || jdKeywords.contains("api") || jdKeywords.contains("database")) {
             projectSuggestions.add("Highlight the database schema and API design in your backend projects.");
        }
        if (jdKeywords.contains("frontend") || jdKeywords.contains("ui") || jdKeywords.contains("react")) {
             projectSuggestions.add("Include details about state management and UI responsiveness in your frontend projects.");
        }
        
        List<String> skillsToLearn = new ArrayList<>(missingKeywords);
        if (skillsToLearn.isEmpty()) {
            skillsToLearn.add("System Design basics");
            skillsToLearn.add("Advanced Cloud Concepts (AWS/GCP)");
        }
        
        List<ResumeAnalysisResponse.SectionFeedback> sectionFeedback = new ArrayList<>();
        sectionFeedback.add(new ResumeAnalysisResponse.SectionFeedback("Summary", "Tailor your summary to specifically mention your interest in " + (matchingSkills.isEmpty() ? "this role" : matchingSkills.get(0)) + "."));
        sectionFeedback.add(new ResumeAnalysisResponse.SectionFeedback("Skills", "You have " + matchingSkills.size() + " skills directly matching the job description out of " + requiredSkills.size() + " identified keywords."));
        sectionFeedback.add(new ResumeAnalysisResponse.SectionFeedback("Experience", "Ensure you clearly demonstrate how you used your matching skills in real projects."));
        
        return new ResumeAnalysisResponse(
                false,
                "🤖 Text-Based Analysis",
                null,
                fileName,
                atsScore,       // ATS Score
                matchingSkills,
                missingKeywords,
                suggestions,
                projectSuggestions,
                skillsToLearn,
                sectionFeedback,
                null
        );
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a resume PDF file");
        }
        if (file.getSize() > MAX_RESUME_SIZE) {
            throw new BadRequestException("Resume file must not exceed 5 MB");
        }
        String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are accepted for resume analysis");
        }
    }

    private StudentProfile getStudentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private String buildSectionFeedbackString(List<ResumeAnalysisResponse.SectionFeedback> feedbackList) {
        if (feedbackList == null) return null;
        StringBuilder sb = new StringBuilder();
        for (ResumeAnalysisResponse.SectionFeedback sf : feedbackList) {
            sb.append(sf.getSection()).append(": ").append(sf.getFeedback()).append("\n");
        }
        return sb.toString().trim();
    }

    private ResumeAnalysisResponse entityToResponse(ResumeAnalysis entity) {
        List<String> matchingSkills = entity.getMatchingSkills() != null
                ? Arrays.asList(entity.getMatchingSkills().split(",\\s*")) : List.of();
        List<String> missingKeywords = entity.getMissingKeywords() != null
                ? Arrays.asList(entity.getMissingKeywords().split(",\\s*")) : List.of();
        List<String> suggestions = entity.getSuggestions() != null
                ? Arrays.asList(entity.getSuggestions().split("\n")) : List.of();
        List<String> projectSuggestions = entity.getProjectSuggestions() != null
                ? Arrays.asList(entity.getProjectSuggestions().split("\n")) : List.of();
        List<String> skillsToLearn = entity.getSkillsToLearn() != null
                ? Arrays.asList(entity.getSkillsToLearn().split(",\\s*")) : List.of();

        List<ResumeAnalysisResponse.SectionFeedback> sectionFeedback = parseSectionFeedback(entity.getSectionFeedback());

        return new ResumeAnalysisResponse(
                false,
                "🤖 AI Feature (Beta)",
                entity.getId(),
                entity.getResumeFileName(),
                entity.getAtsScore() != null ? entity.getAtsScore() : 0,
                matchingSkills,
                missingKeywords,
                suggestions,
                projectSuggestions,
                skillsToLearn,
                sectionFeedback,
                entity.getAnalyzedAt()
        );
    }

    private List<ResumeAnalysisResponse.SectionFeedback> parseSectionFeedback(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("\n"))
                .map(line -> {
                    int colonIdx = line.indexOf(':');
                    if (colonIdx < 0) return null;
                    String section = line.substring(0, colonIdx).trim();
                    String feedback = line.substring(colonIdx + 1).trim();
                    return new ResumeAnalysisResponse.SectionFeedback(section, feedback);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
