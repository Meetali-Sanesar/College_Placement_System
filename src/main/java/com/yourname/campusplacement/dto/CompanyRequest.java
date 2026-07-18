package com.yourname.campusplacement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String name;

    private String website;

    private String industry;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    private String contactPhone;
    private String location;
}
