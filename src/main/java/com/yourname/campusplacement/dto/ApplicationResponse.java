package com.yourname.campusplacement.dto;

import com.yourname.campusplacement.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long driveId;
    private String driveRole;
    private String companyName;
    private Long studentId;
    private String studentName;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
