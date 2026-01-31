package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCommentDTO {
    private Long id;
    private Long applicationId;
    private Long userId;
    private String username;
    private String displayName;
    private String comment;
    private Integer rating; // Rating from 1 to 5 stars
    private LocalDateTime createdAt;
    
    // Additional fields for displaying comments from other applications of the same candidate
    private String jobReference;
    private String roleCategory;
    private Boolean currentApplication; // Changed from isCurrentApplication to currentApplication for proper getter/setter
}
