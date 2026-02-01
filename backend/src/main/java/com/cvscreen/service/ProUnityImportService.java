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
    private final NameSplitterService nameSplitterService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public ImportResult importFromProUnity(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try {
            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());
            
            // Navigate to jobPost object (handle both {"jobPost": {...}} and direct {...} formats)
            JsonNode jobPostNode = rootNode.has("jobPost") ? rootNode.get("jobPost") : rootNode;
            
            // Extract job information
            String jobReference = jobPostNode.path("projectCode").asText();
            String jobTitle = jobPostNode.path("name").asText();
            String companyName = jobPostNode.path("department").path("name").asText();
            
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
            JsonNode candidatesNode = jobPostNode.path("candidates");
            if (candidatesNode.isArray()) {
                int candidateIndex = 0;
                for (JsonNode candidateNode : candidatesNode) {
                    candidateIndex++;
                    try {
                        importCandidate(candidateNode, job, company, result, candidateIndex);
                    } catch (Exception e) {
                        log.error("Failed to import candidate #{}: {}", candidateIndex, e.getMessage(), e);
                        result.addError(candidateIndex, "Failed to import candidate: " + e.getMessage());
                    }
                }
            } else {
                log.warn("No candidates array found in JSON");
            }
            
            log.info("Import completed. Success: {}, Skipped: {}, Failed: {}", 
                    result.getSuccessCount(), result.getSkippedCount(), result.getFailedCount());
            
        } catch (IOException e) {
            log.error("Failed to parse Pro-Unity JSON", e);
            result.addError(0, "Failed to parse JSON file: " + e.getMessage());
        }
        
        return result;
    }
    
    private void importCandidate(JsonNode candidateNode, Job job, Company company, ImportResult result, int index) {
        // Extract Pro-Unity UUID
        String externalId = candidateNode.path("id").asText();
        
        log.debug("Processing candidate #{} with external ID: {}", index, externalId);
        
        // Check if application already exists
        if (externalId != null && !externalId.isEmpty() && applicationRepository.existsByExternalId(externalId)) {
            log.debug("Application with external ID {} already exists, skipping", externalId);
            result.incrementSkippedCount();
            return;
        }
        
        // Extract candidate information from profile
        JsonNode profileNode = candidateNode.path("profile");
        String fullName = profileNode.path("fullName").asText();
        
        log.debug("Candidate #{} full name: {}", index, fullName);
        
        if (fullName == null || fullName.trim().isEmpty()) {
            String errorMsg = "Missing candidate name (fullName) for external ID: " + externalId;
            log.error(errorMsg);
            result.addError(index, errorMsg);
            return;
        }
        
        // Split full name into first name and last name using intelligent splitter
        String[] nameParts = nameSplitterService.splitName(fullName);
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        
        log.info("Candidate #{}: Split '{}' into firstName='{}', lastName='{}'", 
                index, fullName, firstName, lastName);
        
        // Find or create candidate
        Candidate candidate = candidateService.findOrCreateCandidate(firstName, lastName);
        
        // Extract application information
        String roleCategory = extractRoleCategory(candidateNode, job);
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
        
        log.info("Successfully imported candidate #{}: {} {} (External ID: {})", 
                index, firstName, lastName, externalId);
    }
    
    private String extractRoleCategory(JsonNode candidateNode, Job job) {
        // Try to get role from candidate's roles
        JsonNode rolesNode = candidateNode.path("roles");
        if (rolesNode.isArray() && rolesNode.size() > 0) {
            String roleName = rolesNode.get(0).path("name").asText();
            if (roleName != null && !roleName.isEmpty()) {
                return roleName;
            }
        }
        
        // Fallback to job title if available
        if (job != null && job.getTitle() != null) {
            return job.getTitle();
        }
        
        return "Unknown";
    }
    
    private BigDecimal extractDailyRate(JsonNode candidateNode) {
        // Try to get daily rate from various fields in candidateNode
        JsonNode rateNode = candidateNode.path("proposedDailyRate");
        if (!rateNode.isMissingNode() && !rateNode.isNull()) {
            try {
                return new BigDecimal(rateNode.asText());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse proposedDailyRate: {}", rateNode.asText());
            }
        }
        
        rateNode = candidateNode.path("benchmarkDailyRate");
        if (!rateNode.isMissingNode() && !rateNode.isNull()) {
            try {
                return new BigDecimal(rateNode.asText());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse benchmarkDailyRate: {}", rateNode.asText());
            }
        }
        
        // Try profile.dailyRate
        rateNode = candidateNode.path("profile").path("dailyRate");
        if (!rateNode.isMissingNode() && !rateNode.isNull()) {
            try {
                return new BigDecimal(rateNode.asText());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse profile.dailyRate: {}", rateNode.asText());
            }
        }
        
        return null;
    }
    
    private LocalDate extractApplicationDate(JsonNode candidateNode) {
        // Try different date fields
        String dateStr = candidateNode.path("appliedDate").asText();
        if (dateStr == null || dateStr.isEmpty()) {
            dateStr = candidateNode.path("createdDate").asText();
        }
        if (dateStr == null || dateStr.isEmpty()) {
            dateStr = candidateNode.path("submittedDate").asText();
        }
        
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                // Parse ISO date format (e.g., "2026-01-28T10:44:53.73")
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                return dateTime.toLocalDate();
            } catch (Exception e) {
                log.warn("Failed to parse date '{}', trying alternative formats", dateStr);
                try {
                    // Try just date format
                    return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                } catch (Exception e2) {
                    log.warn("Failed to parse date: {}", dateStr);
                }
            }
        }
        
        // Default to today
        return LocalDate.now();
    }
    
    private Application.ApplicationStatus mapStatus(JsonNode candidateNode) {
        String statusLabel = candidateNode.path("statusLabel").asText();
        String statusId = candidateNode.path("statusId").asText();
        
        if (statusLabel == null || statusLabel.isEmpty()) {
            statusLabel = statusId;
        }
        
        if (statusLabel == null || statusLabel.isEmpty()) {
            return Application.ApplicationStatus.CV_RECEIVED;
        }
        
        // Map Pro-Unity status to our status
        String statusLower = statusLabel.toLowerCase();
        
        // Contracting/Selected statuses
        if (statusLower.contains("contract") || statusLower.contains("selected")) {
            if (statusLower.contains("signed")) {
                return Application.ApplicationStatus.APPROVED_FOR_MISSION;
            }
            return Application.ApplicationStatus.APPROVED_FOR_MISSION;
        }
        
        // Rejected/Withdrawn statuses
        if (statusLower.contains("reject") || statusLower.contains("declined") || 
            statusLower.contains("withdrawn") || statusLower.contains("refused")) {
            return Application.ApplicationStatus.REJECTED;
        }
        
        // Interview statuses
        if (statusLower.contains("interview")) {
            return Application.ApplicationStatus.REMOTE_INTERVIEW;
        }
        
        // Shortlist/Longlist statuses
        if (statusLower.contains("shortlist")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        
        if (statusLower.contains("longlist")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        
        // Pre-selected/Applied statuses
        if (statusLower.contains("preselected") || statusLower.contains("pinned")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        
        // On Hold
        if (statusLower.contains("hold")) {
            return Application.ApplicationStatus.ON_HOLD;
        }
        
        // Default for "Applied" or unknown
        return Application.ApplicationStatus.CV_RECEIVED;
    }
    
    private String extractEvaluationNotes(JsonNode candidateNode) {
        StringBuilder notes = new StringBuilder();
        
        // Add status information
        String statusLabel = candidateNode.path("statusLabel").asText();
        if (statusLabel != null && !statusLabel.isEmpty()) {
            notes.append("Pro-Unity Status: ").append(statusLabel).append("\n\n");
        }
        
        // Add supplier information if available
        JsonNode supplierNode = candidateNode.path("supplier");
        if (!supplierNode.isMissingNode()) {
            String supplierName = supplierNode.path("name").asText();
            if (supplierName != null && !supplierName.isEmpty()) {
                notes.append("Supplier: ").append(supplierName).append("\n");
            }
        }
        
        // Add comments if available
        JsonNode commentsNode = candidateNode.path("comments");
        if (commentsNode.isArray() && commentsNode.size() > 0) {
            notes.append("\nComments:\n");
            for (JsonNode comment : commentsNode) {
                String text = comment.path("text").asText();
                String author = comment.path("author").asText();
                String date = comment.path("createdDate").asText();
                
                if (text != null && !text.isEmpty()) {
                    notes.append("- ").append(text);
                    if (author != null && !author.isEmpty()) {
                        notes.append(" (by ").append(author);
                        if (date != null && !date.isEmpty()) {
                            notes.append(" on ").append(date);
                        }
                        notes.append(")");
                    }
                    notes.append("\n");
                }
            }
        }
        
        // Add evaluation/score if available
        JsonNode scoreNode = candidateNode.path("score");
        if (!scoreNode.isMissingNode() && !scoreNode.isNull()) {
            notes.append("\nScore: ").append(scoreNode.asText()).append("\n");
        }
        
        // Add matching score if available
        JsonNode matchingNode = candidateNode.path("matching");
        if (!matchingNode.isMissingNode()) {
            JsonNode percentageNode = matchingNode.path("percentage");
            if (!percentageNode.isMissingNode()) {
                notes.append("Matching: ").append(percentageNode.asText()).append("%\n");
            }
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
        
        // Check status-based conclusion
        String statusLabel = candidateNode.path("statusLabel").asText();
        if (statusLabel != null && !statusLabel.isEmpty()) {
            String statusLower = statusLabel.toLowerCase();
            if (statusLower.contains("signed") || statusLower.contains("contracted")) {
                return "Contract signed";
            } else if (statusLower.contains("selected")) {
                return "Selected for mission";
            } else if (statusLower.contains("rejected")) {
                return "Not selected";
            }
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
