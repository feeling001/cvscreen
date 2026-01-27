package com.cvscreen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;
    
    @Column(name = "role_category", nullable = false)
    private String roleCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;
    
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;
    
    @Column(name = "cv_file_path")
    private String cvFilePath;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.CV_RECEIVED;
    
    @Column(columnDefinition = "TEXT")
    private String conclusion;
    
    @Column(name = "evaluation_notes", columnDefinition = "TEXT")
    private String evaluationNotes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ApplicationStatus {
        CV_RECEIVED,
        CV_REVIEWED,
        REMOTE_INTERVIEW,
        ONSITE_INTERVIEW,
        APPROVED_FOR_MISSION,
        REJECTED,
        ON_HOLD
    }
}
