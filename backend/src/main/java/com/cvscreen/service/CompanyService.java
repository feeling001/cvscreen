package com.cvscreen.service;

import com.cvscreen.dto.CompanyDTO;
import com.cvscreen.entity.Company;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    
    /**
     * NEW: Get all companies with pagination and sorting
     * Supports sorting by name and applicationCount
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> getAllCompaniesPaginated(Pageable pageable) {
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by applicationCount (computed field)
        if ("applicationCount".equals(sortProperty)) {
            return getAllCompaniesWithCustomSort(null, pageable);
        }
        
        // Standard sorting (e.g., by name)
        return companyRepository.findAll(pageable).map(this::convertToDTO);
    }
    
    /**
     * NEW: Search companies with pagination and sorting
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> searchCompaniesPaginated(String name, Pageable pageable) {
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by applicationCount (computed field)
        if ("applicationCount".equals(sortProperty)) {
            return getAllCompaniesWithCustomSort(name, pageable);
        }
        
        // Standard search and sorting
        List<Company> companies = companyRepository.findByNameContainingIgnoreCase(name);
        
        // Apply sorting manually if needed
        boolean ascending = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().isAscending() 
            : true;
        
        if ("name".equals(sortProperty)) {
            if (ascending) {
                companies.sort(Comparator.comparing(Company::getName, String.CASE_INSENSITIVE_ORDER));
            } else {
                companies.sort(Comparator.comparing(Company::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            }
        }
        
        List<CompanyDTO> dtos = companies.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        List<CompanyDTO> pageContent = start < dtos.size() ? dtos.subList(start, end) : List.of();
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    /**
     * Custom method to handle sorting by applicationCount (computed field)
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> getAllCompaniesWithCustomSort(String searchTerm, Pageable pageable) {
        // Get all companies matching the search (without pagination for sorting)
        List<Company> allCompanies;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            allCompanies = companyRepository.findByNameContainingIgnoreCase(searchTerm);
        } else {
            allCompanies = companyRepository.findAll();
        }
        
        // Convert to DTOs (which calculates applicationCount)
        List<CompanyDTO> dtos = allCompanies.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // Apply custom sorting
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : "applicationCount";
        boolean ascending = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().isAscending() 
            : false; // Default DESC for applicationCount
        
        if ("applicationCount".equals(sortProperty)) {
            // Sort by application count
            if (ascending) {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getApplicationCount() != null ? dto.getApplicationCount() : 0,
                    Comparator.naturalOrder()
                ));
            } else {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getApplicationCount() != null ? dto.getApplicationCount() : 0,
                    Comparator.reverseOrder()
                ));
            }
        } else if ("name".equals(sortProperty)) {
            // Sort by name
            if (ascending) {
                dtos.sort(Comparator.comparing(CompanyDTO::getName, String.CASE_INSENSITIVE_ORDER));
            } else {
                dtos.sort(Comparator.comparing(CompanyDTO::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            }
        }
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        List<CompanyDTO> pageContent = start < dtos.size() ? dtos.subList(start, end) : List.of();
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return convertToDTO(company);
    }
    
    @Transactional(readOnly = true)
    public List<CompanyDTO> searchCompanies(String name) {
        return companyRepository.findByNameContainingIgnoreCase(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public CompanyDTO createCompany(String name, String notes) {
        Company company = new Company();
        company.setName(name);
        company.setNotes(notes);
        
        company = companyRepository.save(company);
        return convertToDTO(company);
    }
    
    @Transactional
    public CompanyDTO updateCompany(Long id, String name, String notes) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        
        company.setName(name);
        company.setNotes(notes);
        
        company = companyRepository.save(company);
        return convertToDTO(company);
    }
    
    @Transactional
    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }
    
    @Transactional
    public CompanyDTO mergeCompanies(Long targetCompanyId, List<Long> companyIdsToMerge, String mergedNotes) {
        log.info("Merging companies {} into target company {}", companyIdsToMerge, targetCompanyId);
        
        // Get target company
        Company targetCompany = companyRepository.findById(targetCompanyId)
            .orElseThrow(() -> new ResourceNotFoundException("Target company not found with id: " + targetCompanyId));
        
        // Update notes
        targetCompany.setNotes(mergedNotes);
        
        // Merge all applications from other companies to target
        for (Long companyId : companyIdsToMerge) {
            if (companyId.equals(targetCompanyId)) {
                continue; // Skip target company
            }
            
            Company companyToMerge = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company to merge not found with id: " + companyId));
            
            // Transfer all applications to target company
            applicationRepository.updateCompanyForApplications(companyId, targetCompanyId);
            
            log.info("Transferred {} applications from company {} to {}", 
                    companyToMerge.getApplications().size(), companyId, targetCompanyId);
            
            // Delete the merged company
            companyRepository.deleteById(companyId);
        }
        
        // Save and return updated target company
        targetCompany = companyRepository.save(targetCompany);
        return convertToDTO(targetCompany);
    }
    
    public Company findOrCreateCompany(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return companyRepository.findByName(name)
            .orElseGet(() -> {
                Company newCompany = new Company();
                newCompany.setName(name);
                return companyRepository.save(newCompany);
            });
    }
    
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setNotes(company.getNotes());
        dto.setApplicationCount(company.getApplications() != null ? company.getApplications().size() : 0);
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}
