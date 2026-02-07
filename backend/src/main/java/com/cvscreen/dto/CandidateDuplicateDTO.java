package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a pair of potentially duplicate candidates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDuplicateDTO {
    private CandidateDTO candidate1;
    private CandidateDTO candidate2;
    private double similarityScore; // 0.0 to 1.0 (1.0 = identical)
    private int levenshteinDistance;
    private String matchReason; // e.g., "Name similarity: 95%"
}
