package com.cvscreen.service;

import com.cvscreen.dto.CandidateReviewDTO;
import com.cvscreen.dto.CreateReviewRequest;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.CandidateReview;
import com.cvscreen.entity.User;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.CandidateRepository;
import com.cvscreen.repository.CandidateReviewRepository;
import com.cvscreen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateReviewService {
    
    private final CandidateReviewRepository reviewRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<CandidateReviewDTO> getReviewsForCandidate(Long candidateId) {
        return reviewRepository.findByCandidateIdWithUser(candidateId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public CandidateReviewDTO createReview(Long candidateId, String username, CreateReviewRequest request) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        CandidateReview review = new CandidateReview();
        review.setCandidate(candidate);
        review.setUser(user);
        review.setComment(request.getComment());
        
        review = reviewRepository.save(review);
        return convertToDTO(review);
    }
    
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
    
    private CandidateReviewDTO convertToDTO(CandidateReview review) {
        CandidateReviewDTO dto = new CandidateReviewDTO();
        dto.setId(review.getId());
        dto.setCandidateId(review.getCandidate().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setDisplayName(review.getUser().getDisplayName());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
