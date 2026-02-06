package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private Long jobId;
    private String jobReference;
    private String jobTitle;
    private String roleCategory;
    private Long companyId;
    private String companyName;
    private BigDecimal dailyRate;
    private LocalDate applicationDate;
    private String status;
    private String conclusion;
    private String cvFilePath;
    private Long commentCount;
    private Double averageRating; // Average rating from comments (1-5 stars)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
