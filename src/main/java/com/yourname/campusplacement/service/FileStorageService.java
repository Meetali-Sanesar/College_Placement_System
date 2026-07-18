package com.yourname.campusplacement.service;

import com.yourname.campusplacement.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles secure storage and deletion of student resume files.
 *
 * Security measures applied:
 *  1. File size validated in application code (not just multipart config).
 *  2. Magic-bytes (PDF header) validation — extension alone is not enough.
 *  3. Path traversal prevention: the resolved target path is verified to
 *     remain inside the designated upload directory.
 *  4. Old resume deleted on re-upload to prevent disk accumulation.
 *  5. UUID-based stored filename — original filename is never used on disk.
 */
@Service
public class FileStorageService {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    /** PDF magic bytes: %PDF */
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Stores the uploaded resume on disk and returns the public URL path.
     * Pass the student's existing resumeUrl so the old file can be deleted.
     */
    public String storeResume(MultipartFile file, Long studentId, String existingResumeUrl) {
        // --- 1. Null / empty guard ---
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        // --- 2. Size check (application-level, not just servlet config) ---
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Resume file must not exceed 5 MB");
        }

        // --- 3. Extension check ---
        String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF resumes are allowed");
        }

        // --- 4. Path traversal guard ---
        String cleanedName = originalFilename.replace("/", "").replace("\\", "").replace("..", "");
        if (!cleanedName.equals(originalFilename)) {
            throw new BadRequestException("Invalid filename");
        }

        // --- 5. MIME / magic-bytes validation ---
        validatePdfMagicBytes(file);

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // --- 6. Generate safe UUID filename ---
            String storedFilename = "student_" + studentId + "_" + UUID.randomUUID() + ".pdf";
            Path targetPath = uploadPath.resolve(storedFilename).normalize();

            // --- 7. Confirm target stays inside upload directory ---
            if (!targetPath.startsWith(uploadPath)) {
                throw new BadRequestException("Cannot store file outside designated directory");
            }

            // --- 8. Delete old resume before writing new one (prevents disk accumulation) ---
            deleteOldResumeIfPresent(existingResumeUrl, uploadPath);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/resumes/" + storedFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store resume file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validates that the uploaded file starts with the PDF magic bytes (%PDF).
     * A renamed .html → .pdf file will fail this check.
     */
    private void validatePdfMagicBytes(MultipartFile file) {
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new BadRequestException("File is too small to be a valid PDF");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not read uploaded file", ex);
        }

        if (header[0] != PDF_MAGIC[0] || header[1] != PDF_MAGIC[1]
                || header[2] != PDF_MAGIC[2] || header[3] != PDF_MAGIC[3]) {
            throw new BadRequestException("Uploaded file is not a valid PDF");
        }
    }

    /** Deletes the old resume from disk (best-effort — logs if not found). */
    private void deleteOldResumeIfPresent(String existingResumeUrl, Path uploadPath) {
        if (existingResumeUrl == null || existingResumeUrl.isBlank()) return;
        try {
            // existingResumeUrl is like "/uploads/resumes/student_1_<uuid>.pdf"
            String filename = Paths.get(existingResumeUrl).getFileName().toString();
            Path oldFile = uploadPath.resolve(filename).normalize();
            if (oldFile.startsWith(uploadPath)) {
                Files.deleteIfExists(oldFile);
            }
        } catch (IOException ex) {
            // Non-fatal — log but continue with the new upload
            org.slf4j.LoggerFactory.getLogger(FileStorageService.class)
                    .warn("Could not delete old resume: {}", ex.getMessage());
        }
    }
}
