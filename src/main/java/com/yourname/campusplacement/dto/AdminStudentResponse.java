package com.yourname.campusplacement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStudentResponse {
    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String branch;
    private Integer graduationYear;
    private Double cgpa;
}
