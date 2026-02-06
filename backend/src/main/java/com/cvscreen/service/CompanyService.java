package com.cvscreen.service;

import com.cvscreen.dto.CompanyDTO;
import com.cvscreen.entity.Company;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    
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
