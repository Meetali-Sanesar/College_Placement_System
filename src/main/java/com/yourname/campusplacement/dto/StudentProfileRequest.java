package com.yourname.campusplacement.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentProfileRequest {

    private String college;

    private String branch;

    private Integer graduationYear;

    private String phone;

    private Double cgpa;

    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;
}
