package com.yourname.campusplacement.dto;

import com.yourname.campusplacement.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    @NotNull
    private ApplicationStatus status;
}
