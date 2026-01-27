package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryDTO {
    private Long id;
    private String jobReference;
    private String jobTitle;
    private String roleCategory;
    private String companyName;
    private BigDecimal dailyRate;
    private LocalDate applicationDate;
    private String status;
    private String conclusion;
}
