package com.cvscreen.controller;

import com.cvscreen.dto.CandidateReviewDTO;
import com.cvscreen.dto.CreateReviewRequest;
import com.cvscreen.service.CandidateReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates/{candidateId}/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class CandidateReviewController {
    
    private final CandidateReviewService reviewService;
    
    @GetMapping
    public ResponseEntity<List<CandidateReviewDTO>> getReviewsForCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(reviewService.getReviewsForCandidate(candidateId));
    }
    
    @PostMapping
    public ResponseEntity<CandidateReviewDTO> createReview(
            @PathVariable Long candidateId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(candidateId, username, request));
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
