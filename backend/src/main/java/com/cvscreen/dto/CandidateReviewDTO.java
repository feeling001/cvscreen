package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateReviewDTO {
    private Long id;
    private Long candidateId;
    private Long userId;
    private String username;
    private String displayName;
    private String comment;
    private LocalDateTime createdAt;
}
