package com.yourname.campusplacement.repository;

import com.yourname.campusplacement.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    /** Get all analyses for a specific student, most recent first */
    List<ResumeAnalysis> findByStudentIdOrderByAnalyzedAtDesc(Long studentId);
}
