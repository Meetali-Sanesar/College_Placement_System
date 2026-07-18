package com.yourname.campusplacement.dto;

import com.yourname.campusplacement.entity.DriveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PlacementDriveResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String role;
    private String description;
    private Double eligibilityCgpa;
    private String eligibleBranches;
    private LocalDate driveDate;
    private LocalDate deadline;
    private Double packageLpa;
    private String location;
    private DriveStatus status;
    private LocalDateTime createdAt;
}
