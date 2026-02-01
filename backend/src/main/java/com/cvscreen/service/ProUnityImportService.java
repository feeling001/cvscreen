package com.cvscreen.service;

import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.repository.ApplicationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProUnityImportService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public ImportResult importFromProUnity(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try {
            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());
            
            // Extract job information
            String jobReference = rootNode.path("projectCode").asText();
            String jobTitle = rootNode.path("name").asText();
            String companyName = rootNode.path("department").path("name").asText();
            
            log.info("Importing candidates for job: {} - {}", jobReference, jobTitle);
            
            // Find or create job
            Job job = null;
            if (jobReference != null && !jobReference.isEmpty()) {
                job = jobService.findOrCreateJob(jobReference, jobTitle, "Unknown");
            }
            
            // Find or create company
            Company company = null;
            if (companyName != null && !companyName.isEmpty()) {
                company = companyService.findOrCreateCompany(companyName);
            }
            
            // Process candidates
            JsonNode candidatesNode = rootNode.path("candidates");
            if (candidatesNode.isArray()) {
                for (JsonNode candidateNode : candidatesNode) {
                    try {
                        importCandidate(candidateNode, job, company, result);
                    } catch (Exception e) {
                        log.error("Failed to import candidate: {}", e.getMessage(), e);
                        result.addError(0, "Failed to import candidate: " + e.getMessage());
                    }
                }
            }
            
            log.info("Import completed. Success: {}, Skipped: {}, Failed: {}", 
                    result.getSuccessCount(), result.getSkippedCount(), result.getFailedCount());
            
        } catch (IOException e) {
            log.error("Failed to parse Pro-Unity JSON", e);
            result.addError(0, "Failed to parse JSON file: " + e.getMessage());
        }
        
        return result;
    }
    
    private void importCandidate(JsonNode candidateNode, Job job, Company company, ImportResult result) {
        // Extract Pro-Unity UUID
        String externalId = candidateNode.path("id").asText();
        
        // Check if application already exists
        if (applicationRepository.existsByExternalId(externalId)) {
            log.debug("Application with external ID {} already exists, skipping", externalId);
            result.incrementSkippedCount();
            return;
        }
        
        // Extract candidate information
        JsonNode profileNode = candidateNode.path("profile");
        String firstName = profileNode.path("firstName").asText();
        String lastName = profileNode.path("lastName").asText();
        
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            result.addError(0, "Missing candidate name for external ID: " + externalId);
            return;
        }
        
        // Find or create candidate
        Candidate candidate = candidateService.findOrCreateCandidate(firstName, lastName);
        
        // Extract application information
        String roleCategory = extractRoleCategory(candidateNode);
        BigDecimal dailyRate = extractDailyRate(candidateNode);
        LocalDate applicationDate = extractApplicationDate(candidateNode);
        Application.ApplicationStatus status = mapStatus(candidateNode);
        String evaluationNotes = extractEvaluationNotes(candidateNode);
        
        // Create application
        Application application = new Application();
        application.setExternalId(externalId);
        application.setCandidate(candidate);
        application.setJob(job);
        application.setCompany(company);
        application.setRoleCategory(roleCategory);
        application.setDailyRate(dailyRate);
        application.setApplicationDate(applicationDate);
        application.setStatus(status);
        application.setEvaluationNotes(evaluationNotes);
        
        // Extract conclusion if available
        String conclusion = extractConclusion(candidateNode);
        if (conclusion != null && !conclusion.isEmpty()) {
            application.setConclusion(conclusion);
        }
        
        applicationRepository.save(application);
        result.incrementSuccessCount();
        
        log.debug("Successfully imported candidate: {} {} (External ID: {})", 
                firstName, lastName, externalId);
    }
    
    private String extractRoleCategory(JsonNode candidateNode) {
        // Try to get role from candidate data
        JsonNode rolesNode = candidateNode.path("roles");
        if (rolesNode.isArray() && rolesNode.size() > 0) {
            return rolesNode.get(0).path("name").asText("Unknown");
        }
        return "Unknown";
    }
    
    private BigDecimal extractDailyRate(JsonNode candidateNode) {
        // Try to get daily rate from benchmark or proposed rate
        JsonNode rateNode = candidateNode.path("proposedDailyRate");
        if (!rateNode.isMissingNode() && !rateNode.isNull()) {
            return new BigDecimal(rateNode.asText());
        }
        
        rateNode = candidateNode.path("benchmarkDailyRate");
        if (!rateNode.isMissingNode() && !rateNode.isNull()) {
            return new BigDecimal(rateNode.asText());
        }
        
        return null;
    }
    
    private LocalDate extractApplicationDate(JsonNode candidateNode) {
        // Try different date fields
        String dateStr = candidateNode.path("appliedDate").asText();
        if (dateStr == null || dateStr.isEmpty()) {
            dateStr = candidateNode.path("createdDate").asText();
        }
        
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                // Parse ISO date format
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                return dateTime.toLocalDate();
            } catch (Exception e) {
                log.warn("Failed to parse date: {}", dateStr);
            }
        }
        
        // Default to today
        return LocalDate.now();
    }
    
    private Application.ApplicationStatus mapStatus(JsonNode candidateNode) {
        String statusLabel = candidateNode.path("statusLabel").asText();
        
        if (statusLabel == null || statusLabel.isEmpty()) {
            return Application.ApplicationStatus.CV_RECEIVED;
        }
        
        // Map Pro-Unity status to our status
        String statusLower = statusLabel.toLowerCase();
        
        if (statusLower.contains("approved") || statusLower.contains("selected")) {
            return Application.ApplicationStatus.APPROVED_FOR_MISSION;
        } else if (statusLower.contains("rejected") || statusLower.contains("declined")) {
            return Application.ApplicationStatus.REJECTED;
        } else if (statusLower.contains("interview")) {
            return Application.ApplicationStatus.REMOTE_INTERVIEW;
        } else if (statusLower.contains("shortlist") || statusLower.contains("reviewed")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        } else if (statusLower.contains("hold")) {
            return Application.ApplicationStatus.ON_HOLD;
        }
        
        return Application.ApplicationStatus.CV_RECEIVED;
    }
    
    private String extractEvaluationNotes(JsonNode candidateNode) {
        StringBuilder notes = new StringBuilder();
        
        // Add status information
        String statusLabel = candidateNode.path("statusLabel").asText();
        if (statusLabel != null && !statusLabel.isEmpty()) {
            notes.append("Status: ").append(statusLabel).append("\n");
        }
        
        // Add comments if available
        JsonNode commentsNode = candidateNode.path("comments");
        if (commentsNode.isArray() && commentsNode.size() > 0) {
            notes.append("\nComments:\n");
            for (JsonNode comment : commentsNode) {
                String text = comment.path("text").asText();
                String author = comment.path("author").asText();
                if (text != null && !text.isEmpty()) {
                    notes.append("- ").append(text);
                    if (author != null && !author.isEmpty()) {
                        notes.append(" (").append(author).append(")");
                    }
                    notes.append("\n");
                }
            }
        }
        
        // Add evaluation score if available
        JsonNode scoreNode = candidateNode.path("score");
        if (!scoreNode.isMissingNode() && !scoreNode.isNull()) {
            notes.append("\nScore: ").append(scoreNode.asText()).append("\n");
        }
        
        return notes.toString().trim();
    }
    
    private String extractConclusion(JsonNode candidateNode) {
        // Try to get conclusion from various fields
        String conclusion = candidateNode.path("conclusion").asText();
        if (conclusion != null && !conclusion.isEmpty()) {
            return conclusion;
        }
        
        // Check if rejected with reason
        JsonNode rejectionNode = candidateNode.path("rejectionReason");
        if (!rejectionNode.isMissingNode() && !rejectionNode.isNull()) {
            return "Rejected: " + rejectionNode.asText();
        }
        
        return null;
    }
    
    /**
     * Result object for Pro-Unity import operations
     */
    public static class ImportResult {
        private int successCount = 0;
        private int skippedCount = 0;
        private final List<ImportError> errors = new ArrayList<>();
        
        public void incrementSuccessCount() {
            successCount++;
        }
        
        public void incrementSkippedCount() {
            skippedCount++;
        }
        
        public void addError(int lineNumber, String message) {
            errors.add(new ImportError(lineNumber, message));
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getSkippedCount() {
            return skippedCount;
        }
        
        public int getFailedCount() {
            return errors.size();
        }
        
        public List<ImportError> getErrors() {
            return errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
    
    /**
     * Import error details
     */
    public static class ImportError {
        private final int lineNumber;
        private final String message;
        
        public ImportError(int lineNumber, String message) {
            this.lineNumber = lineNumber;
            this.message = message;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
