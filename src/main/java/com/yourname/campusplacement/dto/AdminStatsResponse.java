package com.yourname.campusplacement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalStudents;
    private long totalCompanies;
    private long totalDrives;
    private long openDrives;
    private long totalApplications;
    private long shortlisted;
    private long selected;
}
