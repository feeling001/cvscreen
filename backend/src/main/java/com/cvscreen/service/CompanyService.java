package com.cvscreen.service;

import com.cvscreen.dto.CompanyDTO;
import com.cvscreen.entity.Company;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    
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
