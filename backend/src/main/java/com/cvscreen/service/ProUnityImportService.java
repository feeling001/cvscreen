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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
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
            log.info("=== Starting Pro-Unity import ===");
            log.info("File: {}, Size: {} bytes ({} MB)", 
                    file.getOriginalFilename(), file.getSize(), String.format("%.2f", file.getSize() / 1024.0 / 1024.0));
            
            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());
            log.info("JSON parsed successfully");
            log.debug("Root node has {} fields", rootNode.size());
            
            // Log all root level fields to help debug
            Iterator<String> fieldNames = rootNode.fieldNames();
            StringBuilder fieldsInfo = new StringBuilder("Root fields: ");
            while (fieldNames.hasNext()) {
                fieldsInfo.append(fieldNames.next()).append(", ");
            }
            log.info(fieldsInfo.toString());
            
            // Navigate to candidates - try multiple paths
            JsonNode candidatesNode = findCandidatesNode(rootNode);
            
            if (candidatesNode == null || !candidatesNode.isArray()) {
                String errorMsg = "No candidates array found in JSON";
                log.error(errorMsg);
                log.error("JSON structure (first 1000 chars): {}", 
                        rootNode.toPrettyString().substring(0, Math.min(1000, rootNode.toPrettyString().length())));
                result.addError(0, errorMsg + ". Please check the file structure.");
                return result;
            }
            
            log.info("✓ Found {} candidates in JSON", candidatesNode.size());
            
            // Extract job information
            String jobReference = extractJobReference(rootNode);
            String jobTitle = extractJobTitle(rootNode);
            
            log.info("Job context - Reference: '{}', Title: '{}'", 
                    jobReference, jobTitle);
            
            // Find or create job
            Job job = null;
            if (jobReference != null && !jobReference.isEmpty()) {
                job = jobService.findOrCreateJob(jobReference, jobTitle != null ? jobTitle : "Unknown", "Unknown");
                log.info("✓ Job created/found: {}", jobReference);
            } else {
                log.warn("No job reference found, candidates will be imported as spontaneous applications");
            }
            
            // Process candidates
            int candidateIndex = 0;
            for (JsonNode candidateNode : candidatesNode) {
                candidateIndex++;
                try {
                    importCandidate(candidateNode, job, result, candidateIndex);
                } catch (Exception e) {
                    log.error("✗ Failed to import candidate #{}: {}", candidateIndex, e.getMessage(), e);
                    result.addError(candidateIndex, "Failed: " + e.getMessage());
                }
            }
            
            log.info("=== Import completed ===");
            log.info("SUCCESS: {}, SKIPPED (duplicates): {}, FAILED: {}", 
                    result.getSuccessCount(), result.getSkippedCount(), result.getFailedCount());
            
        } catch (IOException e) {
            log.error("✗ Failed to parse Pro-Unity JSON", e);
            result.addError(0, "Failed to parse JSON: " + e.getMessage());
        } catch (Exception e) {
            log.error("✗ Unexpected error during import", e);
            result.addError(0, "Unexpected error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Try to find the candidates array in various possible locations
     */
    private JsonNode findCandidatesNode(JsonNode rootNode) {
        log.debug("Searching for candidates array...");
        
        // Try direct candidates array
        if (rootNode.has("candidates")) {
            JsonNode candidates = rootNode.get("candidates");
            if (candidates.isArray()) {
                log.info("✓ Found candidates at root.candidates");
                return candidates;
            }
        }
        
        // Try jobPost.candidates
        if (rootNode.has("jobPost")) {
            JsonNode jobPost = rootNode.get("jobPost");
            if (jobPost.has("candidates") && jobPost.get("candidates").isArray()) {
                log.info("✓ Found candidates at root.jobPost.candidates");
                return jobPost.get("candidates");
            }
        }
        
        // Try data.candidates
        if (rootNode.has("data")) {
            JsonNode data = rootNode.get("data");
            if (data.has("candidates") && data.get("candidates").isArray()) {
                log.info("✓ Found candidates at root.data.candidates");
                return data.get("candidates");
            }
        }
        
        // Maybe the root itself is an array of candidates?
        if (rootNode.isArray()) {
            log.info("✓ Root node itself is an array (assuming candidates)");
            return rootNode;
        }
        
        log.error("✗ Could not find candidates array in any expected location");
        return null;
    }
    
    private String extractJobReference(JsonNode rootNode) {
        String ref = getTextValue(rootNode, "projectCode");
        if (ref != null) {
            log.debug("Found jobReference at root.projectCode: {}", ref);
            return ref;
        }
        
        ref = getTextValue(rootNode, "jobReference");
        if (ref != null) {
            log.debug("Found jobReference at root.jobReference: {}", ref);
            return ref;
        }
        
        ref = getTextValue(rootNode, "reference");
        if (ref != null) {
            log.debug("Found jobReference at root.reference: {}", ref);
            return ref;
        }
        
        if (rootNode.has("jobPost")) {
            ref = getTextValue(rootNode.get("jobPost"), "projectCode");
            if (ref != null) {
                log.debug("Found jobReference at jobPost.projectCode: {}", ref);
                return ref;
            }
        }
        
        log.debug("No job reference found");
        return null;
    }
    
    private String extractJobTitle(JsonNode rootNode) {
        String title = getTextValue(rootNode, "name");
        if (title != null) return title;
        
        title = getTextValue(rootNode, "title");
        if (title != null) return title;
        
        if (rootNode.has("jobPost")) {
            title = getTextValue(rootNode.get("jobPost"), "name");
            if (title != null) return title;
            
            title = getTextValue(rootNode.get("jobPost"), "title");
            if (title != null) return title;
        }
        
        return null;
    }
    
    private String getTextValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            String value = node.get(fieldName).asText();
            if (value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value.trim();
            }
        }
        return null;
    }
    
    private void importCandidate(JsonNode candidateNode, Job job, ImportResult result, int index) {
        // Extract Pro-Unity UUID
        String externalId = candidateNode.path("id").asText();
        
        log.debug("Processing candidate #{} (externalId: {})", index, externalId);
        
        // Check if application already exists (duplicate detection)
        if (externalId != null && !externalId.isEmpty() && applicationRepository.existsByExternalId(externalId)) {
            log.info("↻ Candidate #{} already exists (externalId: {}), skipping", index, externalId);
            result.incrementSkippedCount();
            return;
        }
        
        // Extract candidate name from resourceProfile
        JsonNode resourceProfileNode = candidateNode.path("resourceProfile");
        
        String firstName = null;
        String lastName = null;
        String contractType = null; // NEW: Extract contract type
        
        if (!resourceProfileNode.isMissingNode()) {
            firstName = getTextValue(resourceProfileNode, "firstName");
            lastName = getTextValue(resourceProfileNode, "lastName");
        }
        
        // Extract resourceType (contract type)
        contractType = getTextValue(candidateNode, "resourceType");
        if (contractType != null) {
            log.debug("Candidate #{}: Found contractType: {}", index, contractType);
        }
        
        // Fallback to fullName if firstName/lastName not found
        if ((firstName == null || firstName.isEmpty()) || (lastName == null || lastName.isEmpty())) {
            String fullName = candidateNode.path("fullName").asText();
            
            if (fullName == null || fullName.trim().isEmpty()) {
                String errorMsg = "Missing candidate name information";
                log.error("✗ Candidate #{}: {}", index, errorMsg);
                result.addError(index, errorMsg);
                return;
            }
            
            // Use simple split as fallback
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : parts[0];
            
            log.debug("Candidate #{}: Extracted from fullName '{}' → firstName='{}', lastName='{}'", 
                    index, fullName, firstName, lastName);
        } else {
            log.debug("Candidate #{}: firstName='{}', lastName='{}', contractType='{}'", 
                    index, firstName, lastName, contractType);
        }
        
        // Find or create candidate with contract type
        Candidate candidate = candidateService.findOrCreateCandidateWithContractType(firstName, lastName, contractType);
        
        // Extract company information for this specific candidate
        Company company = extractCompanyForCandidate(candidateNode, index);
        
        // Extract application details
        String roleCategory = extractRoleCategory(candidateNode, job);
        BigDecimal dailyRate = extractDailyRate(candidateNode);
        LocalDate applicationDate = extractApplicationDate(candidateNode);
        Application.ApplicationStatus status = mapStatus(candidateNode);
        String evaluationNotes = extractEvaluationNotes(candidateNode);
        String conclusion = extractConclusion(candidateNode);
        
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
        application.setConclusion(conclusion);
        
        applicationRepository.save(application);
        result.incrementSuccessCount();
        
        log.info("✓ Candidate #{}: {} {} ({}) - {} ({}) - Company: {}", 
                index, firstName, lastName, contractType != null ? contractType : "N/A", 
                roleCategory, status, company != null ? company.getName() : "None");
    }
    
    /**
     * Extract company information for a specific candidate
     */
    private Company extractCompanyForCandidate(JsonNode candidateNode, int candidateIndex) {
        String companyName = null;
        
        // First try: resourceProfile.contactInfo.company
        JsonNode resourceProfileNode = candidateNode.path("resourceProfile");
        if (!resourceProfileNode.isMissingNode()) {
            JsonNode contactInfoNode = resourceProfileNode.path("contactInfo");
            if (!contactInfoNode.isMissingNode()) {
                companyName = getTextValue(contactInfoNode, "company");
                if (companyName != null) {
                    log.debug("Candidate #{}: Found company in resourceProfile.contactInfo.company: {}", 
                            candidateIndex, companyName);
                }
            }
        }
        
        // Second try: supplierName (direct field)
        if (companyName == null || companyName.isEmpty()) {
            companyName = getTextValue(candidateNode, "supplierName");
            if (companyName != null) {
                log.debug("Candidate #{}: Found company in supplierName: {}", candidateIndex, companyName);
            }
        }
        
        // If no company found
        if (companyName == null || companyName.trim().isEmpty()) {
            log.debug("Candidate #{}: No company information found", candidateIndex);
            return null;
        }
        
        // Find or create company
        Company company = companyService.findOrCreateCompany(companyName.trim());
        return company;
    }
    
    private String extractRoleCategory(JsonNode candidateNode, Job job) {
        // Try roles array
        JsonNode rolesNode = candidateNode.path("roles");
        if (rolesNode.isArray() && rolesNode.size() > 0) {
            String roleName = rolesNode.get(0).path("name").asText();
            if (roleName != null && !roleName.isEmpty()) {
                return roleName;
            }
        }
        
        // Fallback to job title
        if (job != null && job.getTitle() != null && !job.getTitle().equals("Unknown")) {
            return job.getTitle();
        }
        
        // Try profile.jobTitle
        String jobTitle = candidateNode.path("profile").path("jobTitle").asText();
        if (jobTitle != null && !jobTitle.isEmpty()) {
            return jobTitle;
        }
        
        return "Unknown";
    }
    
    private BigDecimal extractDailyRate(JsonNode candidateNode) {
        String[] rateFields = {"proposedDailyRate", "benchmarkDailyRate", "dailyRate", "rate"};
        
        for (String fieldName : rateFields) {
            JsonNode rateNode = candidateNode.path(fieldName);
            if (!rateNode.isMissingNode() && !rateNode.isNull()) {
                try {
                    String rateStr = rateNode.asText().replaceAll("[^0-9.]", "");
                    if (!rateStr.isEmpty()) {
                        return new BigDecimal(rateStr);
                    }
                } catch (NumberFormatException e) {
                    log.debug("Could not parse {} as number", fieldName);
                }
            }
        }
        
        // Try in profile
        JsonNode profileRate = candidateNode.path("profile").path("dailyRate");
        if (!profileRate.isMissingNode()) {
            try {
                String rateStr = profileRate.asText().replaceAll("[^0-9.]", "");
                if (!rateStr.isEmpty()) {
                    return new BigDecimal(rateStr);
                }
            } catch (NumberFormatException e) {
                log.debug("Could not parse profile.dailyRate");
            }
        }
        
        return null;
    }
    
    private LocalDate extractApplicationDate(JsonNode candidateNode) {
        String[] dateFields = {"appliedDate", "createdDate", "submittedDate", "applicationDate", "date"};
        
        for (String fieldName : dateFields) {
            String dateStr = candidateNode.path(fieldName).asText();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
                } catch (Exception e1) {
                    try {
                        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                    } catch (Exception e2) {
                        // Try next field
                    }
                }
            }
        }
        
        return LocalDate.now();
    }
    
    private Application.ApplicationStatus mapStatus(JsonNode candidateNode) {
        String statusLabel = candidateNode.path("status").asText();
        String statusLower = statusLabel.toLowerCase();
        
        if (statusLower.contains("notpreselected") || statusLower.contains("signed")) {
            return Application.ApplicationStatus.CV_RECEIVED;
        }
        else if (statusLower.contains("selected") || statusLower.contains("approved")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        else if (statusLower.contains("reject") || statusLower.contains("declined")) {
            return Application.ApplicationStatus.REJECTED;
        }
        else if (statusLower.contains("withdrawn") || statusLower.contains("refused")) {
            return Application.ApplicationStatus.REJECTED;
        }
        else if (statusLower.contains("interview")) {
            return Application.ApplicationStatus.REMOTE_INTERVIEW;
        }
        else if (statusLower.contains("shortlist") || statusLower.contains("preselected")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        else if (statusLower.contains("longlist") || statusLower.contains("pinned")) {
            return Application.ApplicationStatus.CV_REVIEWED;
        }
        else if (statusLower.contains("hold")) {
            return Application.ApplicationStatus.ON_HOLD;
        }
    
        return Application.ApplicationStatus.CV_RECEIVED;
    }
    
    private String extractEvaluationNotes(JsonNode candidateNode) {
        StringBuilder notes = new StringBuilder();
        
        String statusLabel = candidateNode.path("statusLabel").asText();
        if (statusLabel != null && !statusLabel.isEmpty()) {
            notes.append("Status: ").append(statusLabel).append("\n");
        }
        
        JsonNode supplierNode = candidateNode.path("supplier");
        if (!supplierNode.isMissingNode() && supplierNode.has("name")) {
            notes.append("Supplier: ").append(supplierNode.get("name").asText()).append("\n");
        }
        
        JsonNode commentsNode = candidateNode.path("comments");
        if (commentsNode.isArray() && commentsNode.size() > 0) {
            notes.append("\nComments:\n");
            for (JsonNode comment : commentsNode) {
                String text = comment.path("text").asText();
                if (text != null && !text.isEmpty()) {
                    notes.append("- ").append(text).append("\n");
                }
            }
        }
        
        return notes.length() > 0 ? notes.toString().trim() : null;
    }
    
    private String extractConclusion(JsonNode candidateNode) {
        String conclusion = candidateNode.path("conclusion").asText();
        if (conclusion != null && !conclusion.isEmpty()) {
            return conclusion;
        }
        
        JsonNode rejectionNode = candidateNode.path("rejectionReason");
        if (!rejectionNode.isMissingNode() && !rejectionNode.asText().isEmpty()) {
            return "Rejected: " + rejectionNode.asText();
        }
        
        return null;
    }
    
    // Inner classes for result
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
