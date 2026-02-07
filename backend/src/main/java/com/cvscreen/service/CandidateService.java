package com.cvscreen.service;

import com.cvscreen.dto.*;
import com.cvscreen.entity.Candidate;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationCommentRepository;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
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
    
    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();
    
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getAllCandidatesPaginated(Pageable pageable) {
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by computed fields (applicationCount, reviewCount, averageRating)
        if ("applicationCount".equals(sortProperty) || 
            "reviewCount".equals(sortProperty) || 
            "averageRating".equals(sortProperty)) {
            return getAllCandidatesWithCustomSort(null, pageable);
        }
        
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
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by computed fields
        if ("applicationCount".equals(sortProperty) || 
            "reviewCount".equals(sortProperty) || 
            "averageRating".equals(sortProperty)) {
            return getAllCandidatesWithCustomSort(searchTerm, pageable);
        }
        
        return candidateRepository.searchByName(searchTerm, pageable).map(this::convertToDTO);
    }
    
    /**
     * Custom method to handle sorting by computed fields (applicationCount, reviewCount, averageRating)
     */
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getAllCandidatesWithCustomSort(String searchTerm, Pageable pageable) {
        // Get all candidates matching the search (without pagination for sorting)
        List<Candidate> allCandidates;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            allCandidates = candidateRepository.searchByName(searchTerm);
        } else {
            allCandidates = candidateRepository.findAll();
        }
        
        // Convert to DTOs (which calculates the computed fields)
        List<CandidateDTO> dtos = allCandidates.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // Apply custom sorting
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : "lastName";
        boolean ascending = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().isAscending() 
            : true;
        
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
        } else if ("reviewCount".equals(sortProperty)) {
            // Sort by review count
            if (ascending) {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getReviewCount() != null ? dto.getReviewCount() : 0L,
                    Comparator.naturalOrder()
                ));
            } else {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getReviewCount() != null ? dto.getReviewCount() : 0L,
                    Comparator.reverseOrder()
                ));
            }
        } else if ("averageRating".equals(sortProperty)) {
            // Sort by average rating
            if (ascending) {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getAverageRating() != null ? dto.getAverageRating() : 0.0,
                    Comparator.naturalOrder()
                ));
            } else {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getAverageRating() != null ? dto.getAverageRating() : 0.0,
                    Comparator.reverseOrder()
                ));
            }
        }
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        List<CandidateDTO> pageContent = dtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    @Transactional(readOnly = true)
    public List<CandidateDTO> searchCandidates(String searchTerm) {
        return candidateRepository.searchByName(searchTerm).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * NEW: Find potential duplicate candidates based on name similarity
     * Uses Levenshtein distance algorithm
     * 
     * @param threshold Similarity threshold (0.0 to 1.0), default 0.85 means 85% similar
     * @return List of duplicate pairs
     */
    @Transactional(readOnly = true)
    public List<CandidateDuplicateDTO> findPotentialDuplicates(double threshold) {
        log.info("Searching for duplicate candidates with threshold: {}", threshold);
        
        List<Candidate> allCandidates = candidateRepository.findAll();
        List<CandidateDuplicateDTO> duplicates = new ArrayList<>();
        
        // Compare each candidate with every other candidate
        for (int i = 0; i < allCandidates.size(); i++) {
            Candidate candidate1 = allCandidates.get(i);
            String name1 = candidate1.getFullName().toLowerCase().trim();
            
            for (int j = i + 1; j < allCandidates.size(); j++) {
                Candidate candidate2 = allCandidates.get(j);
                String name2 = candidate2.getFullName().toLowerCase().trim();
                
                // Calculate Levenshtein distance
                int distance = LEVENSHTEIN.apply(name1, name2);
                
                // Calculate similarity ratio (0.0 to 1.0)
                int maxLength = Math.max(name1.length(), name2.length());
                double similarityScore = 1.0 - ((double) distance / maxLength);
                
                // If similarity is above threshold, consider it a potential duplicate
                if (similarityScore >= threshold) {
                    CandidateDuplicateDTO duplicate = new CandidateDuplicateDTO();
                    duplicate.setCandidate1(convertToDTO(candidate1));
                    duplicate.setCandidate2(convertToDTO(candidate2));
                    duplicate.setSimilarityScore(similarityScore);
                    duplicate.setLevenshteinDistance(distance);
                    duplicate.setMatchReason(String.format("Name similarity: %.0f%%", similarityScore * 100));
                    
                    duplicates.add(duplicate);
                    
                    log.debug("Found duplicate: '{}' ≈ '{}' ({}%)", 
                            name1, name2, (int)(similarityScore * 100));
                }
            }
        }
        
        // Sort by similarity score (highest first)
        duplicates.sort(Comparator.comparing(CandidateDuplicateDTO::getSimilarityScore).reversed());
        
        log.info("Found {} potential duplicate pairs", duplicates.size());
        return duplicates;
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
