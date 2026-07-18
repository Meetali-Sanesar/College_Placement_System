package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.PlacementDriveRequest;
import com.yourname.campusplacement.dto.PlacementDriveResponse;
import com.yourname.campusplacement.entity.Company;
import com.yourname.campusplacement.entity.DriveStatus;
import com.yourname.campusplacement.entity.PlacementDrive;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.ApplicationRepository;
import com.yourname.campusplacement.repository.CompanyRepository;
import com.yourname.campusplacement.repository.PlacementDriveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlacementDriveService {

    private final PlacementDriveRepository driveRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;

    public PlacementDriveService(PlacementDriveRepository driveRepository,
                                  CompanyRepository companyRepository,
                                  ApplicationRepository applicationRepository) {
        this.driveRepository = driveRepository;
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
    }

    /** Students and guests can browse all OPEN drives */
    public Page<PlacementDriveResponse> getOpenDrives(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return driveRepository.findByStatus(DriveStatus.OPEN, pageable).map(this::toResponse);
    }

    /** Admin can see all drives with optional keyword filter */
    public Page<PlacementDriveResponse> getAllDrives(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword != null && !keyword.isBlank()) {
            return driveRepository.findByRoleContainingIgnoreCase(keyword, pageable).map(this::toResponse);
        }
        return driveRepository.findAll(pageable).map(this::toResponse);
    }

    public PlacementDriveResponse getDriveById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public PlacementDriveResponse createDrive(PlacementDriveRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        PlacementDrive drive = new PlacementDrive();
        mapRequestToEntity(request, drive, company);
        return toResponse(driveRepository.save(drive));
    }

    @Transactional
    public PlacementDriveResponse updateDrive(Long id, PlacementDriveRequest request) {
        PlacementDrive drive = findOrThrow(id);
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));
        mapRequestToEntity(request, drive, company);
        return toResponse(driveRepository.save(drive));
    }

    @Transactional
    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Placement drive not found with id: " + id);
        }
        // Delete all applications for this drive before deleting the drive
        applicationRepository.deleteByDriveId(id);
        driveRepository.deleteById(id);
    }

    // --- helpers ---

    private void mapRequestToEntity(PlacementDriveRequest request, PlacementDrive drive, Company company) {
        drive.setCompany(company);
        drive.setRole(request.getRole());
        drive.setDescription(request.getDescription());
        drive.setEligibilityCgpa(request.getEligibilityCgpa());
        drive.setEligibleBranches(request.getEligibleBranches());
        drive.setDriveDate(request.getDriveDate());
        drive.setDeadline(request.getDeadline());
        drive.setPackageLpa(request.getPackageLpa());
        drive.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            drive.setStatus(request.getStatus());
        }
    }

    private PlacementDrive findOrThrow(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + id));
    }

    public PlacementDriveResponse toResponse(PlacementDrive drive) {
        return new PlacementDriveResponse(
                drive.getId(),
                drive.getCompany().getId(),
                drive.getCompany().getName(),
                drive.getRole(),
                drive.getDescription(),
                drive.getEligibilityCgpa(),
                drive.getEligibleBranches(),
                drive.getDriveDate(),
                drive.getDeadline(),
                drive.getPackageLpa(),
                drive.getLocation(),
                drive.getStatus(),
                drive.getCreatedAt()
        );
    }
}
