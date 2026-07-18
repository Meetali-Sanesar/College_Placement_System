package com.yourname.campusplacement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one: each student profile belongs to exactly one user account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String college;
    private String branch;
    private Integer graduationYear;
    private String phone;
    private Double cgpa;

    @Column(length = 1000)
    private String skills;

    private String resumeUrl; // path/filename of uploaded resume
}
