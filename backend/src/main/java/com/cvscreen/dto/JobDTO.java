package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long id;
    private String reference;
    private String title;
    private String category;
    private LocalDate publicationDate;
    private String status;
    private String source;
    private String description;
    private Integer applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
