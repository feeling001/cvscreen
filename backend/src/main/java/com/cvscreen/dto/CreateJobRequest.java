package com.cvscreen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    
    @NotBlank(message = "Reference is required")
    private String reference;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private LocalDate publicationDate;
    
    private String status;
    
    private String source;
    
    private String description;
}
