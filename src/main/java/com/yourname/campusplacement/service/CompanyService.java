package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.CompanyRequest;
import com.yourname.campusplacement.dto.CompanyResponse;
import com.yourname.campusplacement.entity.Company;
import com.yourname.campusplacement.exception.BadRequestException;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Page<CompanyResponse> getAllCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return companyRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<CompanyResponse> searchCompanies(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return companyRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toResponse);
    }

    public CompanyResponse getCompanyById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = new Company();
        mapRequestToEntity(request, company);
        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = findOrThrow(id);
        mapRequestToEntity(request, company);
        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    // --- helpers ---

    private void mapRequestToEntity(CompanyRequest request, Company company) {
        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setDescription(request.getDescription());
        company.setContactEmail(request.getContactEmail());
        company.setContactPhone(request.getContactPhone());
        company.setLocation(request.getLocation());
    }

    private Company findOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    public CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getWebsite(),
                company.getIndustry(),
                company.getDescription(),
                company.getContactEmail(),
                company.getContactPhone(),
                company.getLocation(),
                company.getCreatedAt()
        );
    }
}
