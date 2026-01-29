package com.cvscreen.service;

import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.entity.User;
import com.cvscreen.repository.ApplicationRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced CSV Import Service
 * 
 * Expected CSV format (semicolon-separated):
 * Date;Canal;Demande;Fonction;NOM - Prenom;Linkedin;Supplier;Reviewer - Avis CV 1;Reviewer - Avis CV 2;Reviewer - Avis interview
 * 
 * Example:
 * 15/01/2024;Pro-Unity;I01234;System Architect;DUPONT - Jean;https://linkedin.com/in/jean-dupont;Accenture;Good profile;Strong technical skills;Excellent communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedCSVImportService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final CompanyService companyService;
    
    // CSV column indices
    private static final int COL_DATE = 0;
    private static final int COL_CANAL = 1;
    private static final int COL_DEMANDE = 2;
    private static final int COL_FONCTION = 3;
    private static final int COL_NOM_PRENOM = 4;
    private static final int COL_LINKEDIN = 5;
    private static final int COL_SUPPLIER = 6;
    private static final int COL_AVIS_CV_1 = 7;
    private static final int COL_AVIS_CV_2 = 8;
    private static final int COL_AVIS_INTERVIEW = 9;
    
    /**
     * Import applications from CSV file with comprehensive error handling
     */
    @Transactional
    public ImportResult importApplicationsFromCSV(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try {
            // Read CSV with proper encoding
            List<String[]> records = readCSVWithEncoding(file);
            
            if (records.isEmpty()) {
                result.addError(0, "CSV file is empty");
                return result;
            }
            
            // Skip header row and filter empty lines
            List<String[]> dataRecords = records.subList(1, records.size()).stream()
                .filter(record -> record.length > 0 && !isEmptyRecord(record))
                .collect(Collectors.toList());
            
            log.info("Starting import of {} records", dataRecords.size());
            
            int lineNumber = 2; // Start at 2 (line 1 is header)
            for (String[] record : dataRecords) {
                try {
                    importSingleRecord(record, lineNumber, result);
                    result.incrementSuccessCount();
                } catch (Exception e) {
                    log.error("Failed to import record at line {}: {}", lineNumber, e.getMessage(), e);
                    result.addError(lineNumber, e.getMessage());
                }
                lineNumber++;
            }
            
            log.info("Import completed. Success: {}, Failed: {}", 
                    result.getSuccessCount(), result.getFailedCount());
            
        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file", e);
            result.addError(0, "Failed to read CSV file: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Read CSV with proper encoding detection
     */
    private List<String[]> readCSVWithEncoding(MultipartFile file) throws IOException, CsvException {
        // Try UTF-8 first, then fallback to ISO-8859-1 if needed
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            return reader.readAll();
        } catch (Exception e) {
            log.warn("UTF-8 reading failed, trying ISO-8859-1", e);
            try (CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(file.getInputStream(), "ISO-8859-1"))
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                    .build()) {
                return reader.readAll();
            }
        }
    }
    
    /**
     * Import a single record with all error handling
     */
    private void importSingleRecord(String[] record, int lineNumber, ImportResult result) {
        // Validate record has minimum required columns
        if (record.length < 7) {
            throw new IllegalArgumentException(
                "Record has only " + record.length + " columns, minimum 7 required");
        }
        
        // Extract and validate fields
        String dateStr = getFieldValue(record, COL_DATE);
        String canal = getFieldValue(record, COL_CANAL);
        String demande = getFieldValue(record, COL_DEMANDE);
        String fonction = getFieldValue(record, COL_FONCTION);
        String nomPrenom = getFieldValue(record, COL_NOM_PRENOM);
        String linkedin = getFieldValue(record, COL_LINKEDIN);
        String supplier = getFieldValue(record, COL_SUPPLIER);
        String avisCv1 = getFieldValue(record, COL_AVIS_CV_1);
        String avisCv2 = getFieldValue(record, COL_AVIS_CV_2);
        String avisInterview = getFieldValue(record, COL_AVIS_INTERVIEW);
        
        // Parse and validate date
        LocalDate applicationDate = parseDate(dateStr);
        if (applicationDate == null) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
        
        // Parse candidate name
        CandidateName candidateName = parseCandidateName(nomPrenom);
        if (candidateName == null) {
            throw new IllegalArgumentException("Invalid candidate name format: " + nomPrenom);
        }
        
        // Validate required fields
        if (fonction == null || fonction.trim().isEmpty()) {
            throw new IllegalArgumentException("Fonction (role category) is required");
        }
        
        // Find or create entities
        Candidate candidate = candidateService.findOrCreateCandidate(
            candidateName.firstName, 
            candidateName.lastName
        );
        
        // Update candidate with LinkedIn if provided
        if (linkedin != null && !linkedin.isEmpty()) {
            updateCandidateLinkedIn(candidate, linkedin);
        }
        
        // Find or create job if demande (job reference) is provided
        Job job = null;
        if (demande != null && !demande.isEmpty()) {
            job = jobService.findOrCreateJob(
                demande, 
                "Imported: " + fonction, 
                fonction
            );
            
            // Update job source if canal is provided
            if (canal != null && !canal.isEmpty()) {
                updateJobSource(job, canal);
            }
        }
        
        // Find or create company if supplier is provided
        Company company = null;
        if (supplier != null && !supplier.isEmpty()) {
            company = companyService.findOrCreateCompany(supplier);
        }
        
        // Create application
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setRoleCategory(fonction);
        application.setCompany(company);
        application.setApplicationDate(applicationDate);
        application.setStatus(Application.ApplicationStatus.CV_RECEIVED);
        
        // Build consolidated conclusion from reviews
        String conclusion = buildConclusion(avisCv1, avisCv2, avisInterview);
        if (conclusion != null && !conclusion.isEmpty()) {
            application.setConclusion(conclusion);
        }
        
        // Save application
        applicationRepository.save(application);
        
        // Create candidate reviews for each non-empty review
        createReviewsFromFeedback(candidate, avisCv1, avisCv2, avisInterview, result);
        
        log.debug("Successfully imported application for {} {} (line {})", 
                candidateName.firstName, candidateName.lastName, lineNumber);
    }
    
    /**
     * Parse candidate name from "NOM - Prenom" format
     */
    private CandidateName parseCandidateName(String nomPrenom) {
        if (nomPrenom == null || nomPrenom.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = nomPrenom.split("-");
        if (parts.length < 2) {
            // Try space separator as fallback
            parts = nomPrenom.split(" ", 2);
            if (parts.length < 2) {
                return null;
            }
        }
        
        String lastName = parts[0].trim();
        String firstName = parts[1].trim();
        
        if (lastName.isEmpty() || firstName.isEmpty()) {
            return null;
        }
        
        return new CandidateName(firstName, lastName);
    }
    
    /**
     * Parse date with multiple format support
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("d/M/yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        formatters.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        formatters.add(DateTimeFormatter.ISO_LOCAL_DATE);
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        log.warn("Could not parse date: {}", dateStr);
        return null;
    }
    
    /**
     * Build consolidated conclusion from multiple review fields
     */
    private String buildConclusion(String avisCv1, String avisCv2, String avisInterview) {
        StringBuilder conclusion = new StringBuilder();
        
        if (avisCv1 != null && !avisCv1.isEmpty()) {
            conclusion.append("CV Review 1: ").append(avisCv1);
        }
        
        if (avisCv2 != null && !avisCv2.isEmpty()) {
            if (conclusion.length() > 0) conclusion.append(" | ");
            conclusion.append("CV Review 2: ").append(avisCv2);
        }
        
        if (avisInterview != null && !avisInterview.isEmpty()) {
            if (conclusion.length() > 0) conclusion.append(" | ");
            conclusion.append("Interview: ").append(avisInterview);
        }
        
        return conclusion.length() > 0 ? conclusion.toString() : null;
    }
    
    /**
     * Create candidate reviews from feedback fields
     */
    private void createReviewsFromFeedback(Candidate candidate, String avisCv1, 
                                          String avisCv2, String avisInterview, 
                                          ImportResult result) {
        // Get or create system user for imports
        User systemUser = getSystemUser();
        
        if (avisCv1 != null && !avisCv1.isEmpty()) {
            createCandidateReview(candidate, systemUser, "CV Review 1: " + avisCv1);
        }
        
        if (avisCv2 != null && !avisCv2.isEmpty()) {
            createCandidateReview(candidate, systemUser, "CV Review 2: " + avisCv2);
        }
        
        if (avisInterview != null && !avisInterview.isEmpty()) {
            createCandidateReview(candidate, systemUser, "Interview Feedback: " + avisInterview);
        }
    }
    
    /**
     * Create a candidate review
     */
    private void createCandidateReview(Candidate candidate, User user, String comment) {
        CandidateReview review = new CandidateReview();
        review.setCandidate(candidate);
        review.setUser(user);
        review.setComment(comment);
        reviewRepository.save(review);
    }
    
    /**
     * Get system user for automated imports
     */
    private User getSystemUser() {
        // This should be implemented to return a system/import user
        // For now, we'll create a placeholder - you should implement proper user lookup
        User systemUser = new User();
        systemUser.setId(1L); // Assume admin user has ID 1
        return systemUser;
    }
    
    /**
     * Update candidate's LinkedIn URL
     */
    private void updateCandidateLinkedIn(Candidate candidate, String linkedin) {
        // Add LinkedIn to global notes if not already there
        String notes = candidate.getGlobalNotes();
        if (notes == null || !notes.contains(linkedin)) {
            String newNotes = (notes != null ? notes + "\n" : "") + "LinkedIn: " + linkedin;
            candidate.setGlobalNotes(newNotes);
        }
    }
    
    /**
     * Update job source/canal
     */
    private void updateJobSource(Job job, String canal) {
        if (job.getSource() == null || job.getSource().equals("Pro-Unity")) {
            job.setSource(canal);
        }
    }
    
    /**
     * Get field value from record, handling empty strings and nulls
     */
    private String getFieldValue(String[] record, int index) {
        if (record.length > index) {
            String value = record[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }
    
    /**
     * Check if a record is empty (all fields are null or whitespace)
     */
    private boolean isEmptyRecord(String[] record) {
        for (String field : record) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Inner class to hold parsed candidate name
     */
    private static class CandidateName {
        final String firstName;
        final String lastName;
        
        CandidateName(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
    
    /**
     * Result object for import operations
     */
    public static class ImportResult {
        private int successCount = 0;
        private final List<ImportError> errors = new ArrayList<>();
        
        public void incrementSuccessCount() {
            successCount++;
        }
        
        public void addError(int lineNumber, String message) {
            errors.add(new ImportError(lineNumber, message));
        }
        
        public int getSuccessCount() {
            return successCount;
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
        
        @Override
        public String toString() {
            return "Line " + lineNumber + ": " + message;
        }
    }
}

