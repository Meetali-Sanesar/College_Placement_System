package com.yourname.campusplacement.repository;

import com.yourname.campusplacement.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    /** Admin search: find students by name or email (case-insensitive) */
    Page<StudentProfile> findByUserFullNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
            String name, String email, Pageable pageable);
}
