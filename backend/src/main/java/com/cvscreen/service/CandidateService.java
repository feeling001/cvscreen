package com.cvscreen.service;

import com.cvscreen.dto.*;
import com.cvscreen.entity.Candidate;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    
    @Transactional(readOnly = true)
    public List<CandidateDTO> getAllCandidates() {
        return candidateRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CandidateDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findByIdWithApplications(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
        
        CandidateDTO dto = convertToDTO(candidate);
        dto.setApplications(candidate.getApplications().stream()
            .map(this::convertToApplicationSummary)
            .collect(Collectors.toList()));
        
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<CandidateDTO> searchCandidates(String searchTerm) {
        return candidateRepository.searchByName(searchTerm).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public CandidateDTO createCandidate(CreateCandidateRequest request) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setGlobalNotes(request.getGlobalNotes());
        
        candidate = candidateRepository.save(candidate);
        return convertToDTO(candidate);
    }
    
    @Transactional
    public CandidateDTO updateCandidate(Long id, CreateCandidateRequest request) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
        
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setGlobalNotes(request.getGlobalNotes());
        
        candidate = candidateRepository.save(candidate);
        return convertToDTO(candidate);
    }
    
    @Transactional
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }
    
    public Candidate findOrCreateCandidate(String firstName, String lastName) {
        return candidateRepository.findByFirstNameAndLastName(firstName, lastName)
            .orElseGet(() -> {
                Candidate newCandidate = new Candidate();
                newCandidate.setFirstName(firstName);
                newCandidate.setLastName(lastName);
                return candidateRepository.save(newCandidate);
            });
    }
    
    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setFullName(candidate.getFullName());
        dto.setGlobalNotes(candidate.getGlobalNotes());
        dto.setCreatedAt(candidate.getCreatedAt());
        dto.setUpdatedAt(candidate.getUpdatedAt());
        dto.setApplicationCount(candidate.getApplications() != null ? candidate.getApplications().size() : 0);
        return dto;
    }
    
    private ApplicationSummaryDTO convertToApplicationSummary(com.cvscreen.entity.Application application) {
        ApplicationSummaryDTO dto = new ApplicationSummaryDTO();
        dto.setId(application.getId());
        dto.setJobReference(application.getJob() != null ? application.getJob().getReference() : null);
        dto.setJobTitle(application.getJob() != null ? application.getJob().getTitle() : null);
        dto.setRoleCategory(application.getRoleCategory());
        dto.setCompanyName(application.getCompany() != null ? application.getCompany().getName() : null);
        dto.setDailyRate(application.getDailyRate());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus().name());
        dto.setConclusion(application.getConclusion());
        return dto;
    }
}
