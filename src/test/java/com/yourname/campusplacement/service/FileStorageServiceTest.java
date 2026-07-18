package com.yourname.campusplacement.service;

import com.yourname.campusplacement.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L-13: Unit tests for FileStorageService covering security fixes C-3, M-1, M-2.
 */
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    /** PDF magic header bytes: %PDF */
    private static final byte[] VALID_PDF_HEADER = {0x25, 0x50, 0x44, 0x46, 0x2D};
    private static final byte[] INVALID_HEADER   = {0x3C, 0x68, 0x74, 0x6D, 0x6C}; // <html

    @BeforeEach
    void setUp() throws Exception {
        fileStorageService = new FileStorageService();
        // Inject the temp directory as the upload directory via reflection
        Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, tempDir.toString());
    }

    @Test
    @DisplayName("storeResume() - valid PDF stored successfully")
    void storeResume_validPdf_success() {
        byte[] content = buildPdf();
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", content);

        String url = fileStorageService.storeResume(file, 1L, null);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/resumes/"));
        assertTrue(url.endsWith(".pdf"));
    }

    @Test
    @DisplayName("storeResume() - rejects empty file (M-1)")
    void storeResume_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeResume(file, 1L, null));
    }

    @Test
    @DisplayName("storeResume() - rejects file over 5MB (M-1)")
    void storeResume_tooLarge_throws() {
        byte[] large = new byte[5 * 1024 * 1024 + 1];
        // Put valid PDF header so only size check fails
        System.arraycopy(VALID_PDF_HEADER, 0, large, 0, VALID_PDF_HEADER.length);
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", large);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeResume(file, 1L, null));
    }

    @Test
    @DisplayName("storeResume() - rejects non-PDF extension")
    void storeResume_wrongExtension_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.exe", "application/octet-stream", buildPdf());

        assertThrows(BadRequestException.class, () -> fileStorageService.storeResume(file, 1L, null));
    }

    @Test
    @DisplayName("storeResume() - rejects HTML disguised as PDF (M-2 magic bytes check)")
    void storeResume_htmlDisguisedAsPdf_throws() {
        // File has .pdf extension but HTML content — extension-only check would pass, magic-bytes check must catch it
        MockMultipartFile file = new MockMultipartFile(
                "file", "malicious.pdf", "application/pdf", INVALID_HEADER);

        assertThrows(BadRequestException.class, () -> fileStorageService.storeResume(file, 1L, null));
    }

    @Test
    @DisplayName("storeResume() - rejects path traversal filename (C-3)")
    void storeResume_pathTraversal_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/cron.d/evil.pdf", "application/pdf", buildPdf());

        assertThrows(BadRequestException.class, () -> fileStorageService.storeResume(file, 1L, null));
    }

    /** Returns a minimal valid PDF byte array (just the header for test purposes). */
    private byte[] buildPdf() {
        byte[] content = new byte[100];
        System.arraycopy(VALID_PDF_HEADER, 0, content, 0, VALID_PDF_HEADER.length);
        return content;
    }
}
