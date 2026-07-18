package com.yourname.campusplacement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String website;
    private String industry;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String location;
    private LocalDateTime createdAt;
}
