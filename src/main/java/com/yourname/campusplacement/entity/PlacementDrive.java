package com.yourname.campusplacement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A placement drive organised by the Placement Cell on behalf of a Company.
 * The Placement Cell (Admin) creates and manages these — students apply to them.
 * Replaces the old Job entity which was incorrectly tied to a RecruiterProfile.
 */
@Entity
@Table(name = "placement_drives")
@Getter
@Setter
@NoArgsConstructor
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The company conducting this drive */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Job role being offered, e.g., "Software Engineer", "Business Analyst" */
    @Column(nullable = false)
    private String role;

    @Column(length = 3000, nullable = false)
    private String description;

    /** Minimum CGPA required; null means no restriction */
    private Double eligibilityCgpa;

    /**
     * Comma-separated branch codes eligible, e.g., "CSE,ECE,IT".
     * Null or empty means all branches are eligible.
     */
    @Column(length = 500)
    private String eligibleBranches;

    private LocalDate driveDate;
    private LocalDate deadline;

    /** Package offered in LPA (Lakhs Per Annum) */
    private Double packageLpa;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriveStatus status = DriveStatus.OPEN;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
