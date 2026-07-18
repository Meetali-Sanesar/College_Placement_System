package com.yourname.campusplacement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.yourname.campusplacement.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles secure storage and deletion of student resume files using Cloudinary.
 *
 * Security measures applied:
 *  1. File size validated in application code (not just multipart config).
 *  2. Magic-bytes (PDF header) validation — extension alone is not enough.
 *  3. Old resume deleted on re-upload to prevent cloud storage accumulation.
 */
@Service
public class FileStorageService {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    /** PDF magic bytes: %PDF */
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Stores the uploaded resume on Cloudinary and returns the secure public URL.
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

        // --- 4. MIME / magic-bytes validation ---
        validatePdfMagicBytes(file);

        try {
            // --- 5. Delete old resume before writing new one (prevents cloud accumulation) ---
            deleteOldResumeIfPresent(existingResumeUrl);

            // --- 6. Upload to Cloudinary ---
            String publicId = "resumes/student_" + studentId + "_" + UUID.randomUUID().toString().substring(0, 8);
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "raw", // Use raw for non-image files like PDFs
                    "format", "pdf"
            ));

            return uploadResult.get("secure_url").toString();

        } catch (IOException ex) {
            throw new RuntimeException("Failed to upload resume to cloud storage: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validates that the uploaded file starts with the PDF magic bytes (%PDF).
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

    /** Deletes the old resume from Cloudinary (best-effort). */
    private void deleteOldResumeIfPresent(String existingResumeUrl) {
        if (existingResumeUrl == null || existingResumeUrl.isBlank()) return;
        try {
            // Extract public ID from Cloudinary URL
            // Example URL: https://res.cloudinary.com/cloudname/raw/upload/v123456/resumes/student_1_abcd.pdf
            // We need: resumes/student_1_abcd.pdf
            String[] parts = existingResumeUrl.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1]; // v123456/resumes/student_1_abcd.pdf
                String[] pathParts = path.split("/", 2);
                if (pathParts.length > 1) {
                    String publicIdWithExtension = pathParts[1];
                    // Cloudinary raw files often keep the extension in the public_id, but it's best to supply it
                    cloudinary.uploader().destroy(publicIdWithExtension, ObjectUtils.asMap("resource_type", "raw"));
                }
            }
        } catch (Exception ex) {
            // Non-fatal — log but continue with the new upload
            org.slf4j.LoggerFactory.getLogger(FileStorageService.class)
                    .warn("Could not delete old resume from Cloudinary: {}", ex.getMessage());
        }
    }
}
