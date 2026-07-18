package com.yourname.campusplacement.repository;

import com.yourname.campusplacement.entity.DriveStatus;
import com.yourname.campusplacement.entity.PlacementDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {

    Page<PlacementDrive> findByStatus(DriveStatus status, Pageable pageable);

    long countByStatus(DriveStatus status);

    Page<PlacementDrive> findByCompanyId(Long companyId, Pageable pageable);

    /** Full list for cascade delete — no pagination */
    @Query("SELECT d FROM PlacementDrive d WHERE d.company.id = :companyId")
    List<PlacementDrive> findAllByCompanyId(@Param("companyId") Long companyId);

    /** Search by role keyword */
    Page<PlacementDrive> findByRoleContainingIgnoreCase(String role, Pageable pageable);

    /** Search by role keyword + status */
    Page<PlacementDrive> findByRoleContainingIgnoreCaseAndStatus(
            String role, DriveStatus status, Pageable pageable);
}
