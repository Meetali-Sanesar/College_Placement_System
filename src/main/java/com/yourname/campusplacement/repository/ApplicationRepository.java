package com.yourname.campusplacement.repository;

import com.yourname.campusplacement.entity.Application;
import com.yourname.campusplacement.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);

    List<Application> findByDriveId(Long driveId);

    Optional<Application> findByDriveIdAndStudentId(Long driveId, Long studentId);

    long countByStatus(ApplicationStatus status);

    /** Delete all applications for a drive (used before deleting a drive) */
    @Transactional
    void deleteByDriveId(Long driveId);

    /** Delete all applications of a student (used before deleting a student) */
    @Transactional
    void deleteByStudentId(Long studentId);
}
