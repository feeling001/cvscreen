package com.cvscreen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {
    
    @NotNull(message = "Candidate ID is required")
    private Long candidateId;
    
    private Long jobId;
    
    @NotBlank(message = "Role category is required")
    private String roleCategory;
    
    private Long companyId;
    
    private BigDecimal dailyRate;
    
    @NotNull(message = "Application date is required")
    private LocalDate applicationDate;
    
    private String status;
    
    private String conclusion;
    
    private String evaluationNotes;
}
