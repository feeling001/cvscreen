package com.cvscreen.service;

import com.cvscreen.dto.*;
import com.cvscreen.entity.Candidate;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationCommentRepository;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationCommentRepository commentRepository;
    private final ApplicationCommentService commentService;
    
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getAllCandidatesPaginated(Pageable pageable) {
        return candidateRepository.findAll(pageable).map(this::convertToDTO);
    }
    
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
        
        // Get all comments for this candidate across all applications
        dto.setAllComments(commentRepository.findByCandidateIdWithDetails(id).stream()
            .map(comment -> commentService.convertCommentToDTO(comment, false))
            .collect(Collectors.toList()));
        
        return dto;
    }
    
    @Transactional(readOnly = true)
    public Page<CandidateDTO> searchCandidatesPaginated(String searchTerm, Pageable pageable) {
        return candidateRepository.searchByName(searchTerm, pageable).map(this::convertToDTO);
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
        candidate.setContractType(request.getContractType());
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
        candidate.setContractType(request.getContractType());
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
    
    @Transactional
    public CandidateDTO mergeCandidates(Long targetCandidateId, List<Long> candidateIdsToMerge, String mergedGlobalNotes) {
        log.info("Merging candidates {} into target candidate {}", candidateIdsToMerge, targetCandidateId);
        
        // Get target candidate
        Candidate targetCandidate = candidateRepository.findById(targetCandidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Target candidate not found with id: " + targetCandidateId));
        
        // Update global notes
        targetCandidate.setGlobalNotes(mergedGlobalNotes);
        
        // Merge all applications from other candidates to target
        for (Long candidateId : candidateIdsToMerge) {
            if (candidateId.equals(targetCandidateId)) {
                continue; // Skip target candidate
            }
            
            Candidate candidateToMerge = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate to merge not found with id: " + candidateId));
            
            // Transfer all applications to target candidate
            applicationRepository.updateCandidateForApplications(candidateId, targetCandidateId);
            
            log.info("Transferred {} applications from candidate {} to {}", 
                    candidateToMerge.getApplications().size(), candidateId, targetCandidateId);
            
            // Delete the merged candidate
            candidateRepository.deleteById(candidateId);
        }
        
        // Save and return updated target candidate
        targetCandidate = candidateRepository.save(targetCandidate);
        return getCandidateById(targetCandidateId);
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
    
    public Candidate findOrCreateCandidateWithContractType(String firstName, String lastName, String contractType) {
        return candidateRepository.findByFirstNameAndLastName(firstName, lastName)
            .map(candidate -> {
                // Update contract type if provided and not already set
                if (contractType != null && !contractType.isEmpty() && candidate.getContractType() == null) {
                    candidate.setContractType(contractType);
                    return candidateRepository.save(candidate);
                }
                return candidate;
            })
            .orElseGet(() -> {
                Candidate newCandidate = new Candidate();
                newCandidate.setFirstName(firstName);
                newCandidate.setLastName(lastName);
                newCandidate.setContractType(contractType);
                return candidateRepository.save(newCandidate);
            });
    }
    
    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setFullName(candidate.getFullName());
        dto.setContractType(candidate.getContractType());
        dto.setGlobalNotes(candidate.getGlobalNotes());
        dto.setCreatedAt(candidate.getCreatedAt());
        dto.setUpdatedAt(candidate.getUpdatedAt());
        dto.setApplicationCount(candidate.getApplications() != null ? candidate.getApplications().size() : 0);
        
        // Get review statistics for this candidate
        long reviewCount = commentRepository.countByCandidateId(candidate.getId());
        dto.setReviewCount(reviewCount);
        
        Double averageRating = commentRepository.getAverageRatingByCandidateId(candidate.getId());
        dto.setAverageRating(averageRating);
        
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
