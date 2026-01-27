package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String globalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer applicationCount;
    private Integer reviewCount;
    private List<ApplicationSummaryDTO> applications;
    private List<CandidateReviewDTO> reviews;
}
