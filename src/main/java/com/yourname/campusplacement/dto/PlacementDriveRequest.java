package com.yourname.campusplacement.dto;

import com.yourname.campusplacement.entity.DriveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlacementDriveRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Job role is required")
    private String role;

    @NotBlank(message = "Description is required")
    private String description;

    /** Minimum CGPA required; leave null for no restriction */
    private Double eligibilityCgpa;

    /**
     * Comma-separated branch codes, e.g., "CSE,ECE,IT".
     * Leave null or blank for all branches.
     */
    private String eligibleBranches;

    private LocalDate driveDate;
    private LocalDate deadline;

    @Positive(message = "Package must be a positive number")
    private Double packageLpa;

    private String location;

    private DriveStatus status = DriveStatus.OPEN;
}
