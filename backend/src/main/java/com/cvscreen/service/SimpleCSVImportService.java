package com.cvscreen.service;

import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.repository.ApplicationRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
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
 * Simple CSV Import Service
 * 
 * Expected CSV format (semicolon-separated):
 * application_date;job_reference;role_category;candidate_name;helper;candidate_first_name;company_name;daily_rate;evaluation_notes;conclusion
 * 
 * Example:
 * 19/01/2025;I09526;Full Stack Senior Dev;De Roose;p;Antoine;Extia;595;Philippe Massaert Ne semble pas senior;0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleCSVImportService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final CompanyService companyService;
    
    // CSV column indices (based on the new format)
    private static final int COL_APPLICATION_DATE = 0;
    private static final int COL_JOB_REFERENCE = 1;
    private static final int COL_ROLE_CATEGORY = 2;
    private static final int COL_CANDIDATE_NAME = 3;
    private static final int COL_HELPER = 4; // ignored
    private static final int COL_CANDIDATE_FIRST_NAME = 5;
    private static final int COL_COMPANY_NAME = 6;
    private static final int COL_DAILY_RATE = 7;
    private static final int COL_EVALUATION_NOTES = 8;
    private static final int COL_CONCLUSION = 9;
    
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
        if (record.length < 10) {
            throw new IllegalArgumentException(
                "Record has only " + record.length + " columns, minimum 10 required");
        }
        
        // Extract fields
        String applicationDateStr = getFieldValue(record, COL_APPLICATION_DATE);
        String jobReference = getFieldValue(record, COL_JOB_REFERENCE);
        String roleCategory = getFieldValue(record, COL_ROLE_CATEGORY);
        String candidateLastName = getFieldValue(record, COL_CANDIDATE_NAME);
        String candidateFirstName = getFieldValue(record, COL_CANDIDATE_FIRST_NAME);
        String companyName = getFieldValue(record, COL_COMPANY_NAME);
        String dailyRateStr = getFieldValue(record, COL_DAILY_RATE);
        String evaluationNotes = getFieldValue(record, COL_EVALUATION_NOTES);
        String conclusion = getFieldValue(record, COL_CONCLUSION);
        
        // Validate required fields
        if (applicationDateStr == null || applicationDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Application date is required");
        }
        
        if (roleCategory == null || roleCategory.trim().isEmpty()) {
            throw new IllegalArgumentException("Role category is required");
        }
        
        if (candidateLastName == null || candidateLastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate last name is required");
        }
        
        if (candidateFirstName == null || candidateFirstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate first name is required");
        }
        
        // Parse and validate date
        LocalDate applicationDate = parseDate(applicationDateStr);
        if (applicationDate == null) {
            throw new IllegalArgumentException("Invalid date format: " + applicationDateStr);
        }
        
        // Clean up candidate names (remove extra spaces, special characters)
        candidateFirstName = cleanName(candidateFirstName);
        candidateLastName = cleanName(candidateLastName);
        
        // Find or create candidate
        Candidate candidate = candidateService.findOrCreateCandidate(
            candidateFirstName, 
            candidateLastName
        );
        
        // Find or create job if job reference is provided
        Job job = null;
        if (jobReference != null && !jobReference.trim().isEmpty()) {
            job = jobService.findOrCreateJob(
                jobReference.trim(), 
                roleCategory, 
                roleCategory
            );
        }
        
        // Find or create company if company name is provided
        Company company = null;
        if (companyName != null && !companyName.trim().isEmpty()) {
            company = companyService.findOrCreateCompany(companyName.trim());
        }
        
        // Parse daily rate
        BigDecimal dailyRate = null;
        if (dailyRateStr != null && !dailyRateStr.trim().isEmpty()) {
            try {
                dailyRate = new BigDecimal(dailyRateStr.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid daily rate '{}' at line {}, setting to null", dailyRateStr, lineNumber);
            }
        }
        
        // Create application
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setRoleCategory(roleCategory);
        application.setCompany(company);
        application.setDailyRate(dailyRate);
        application.setApplicationDate(applicationDate);
        application.setStatus(Application.ApplicationStatus.CV_RECEIVED);
        
        // Set evaluation notes if provided
        if (evaluationNotes != null && !evaluationNotes.trim().isEmpty()) {
            application.setEvaluationNotes(evaluationNotes.trim());
        }
        
        // Set conclusion if provided
        if (conclusion != null && !conclusion.trim().isEmpty()) {
            // Handle various "negative" conclusion formats
            String conclusionLower = conclusion.trim().toLowerCase();
            if (conclusionLower.equals("0") || 
                conclusionLower.equals("nok") || 
                conclusionLower.equals("ko") ||
                conclusionLower.equals("rejected")) {
                application.setConclusion("Not suitable");
                application.setStatus(Application.ApplicationStatus.REJECTED);
            } else if (conclusionLower.equals("1") || 
                       conclusionLower.equals("ok") || 
                       conclusionLower.equals("approved")) {
                application.setConclusion("Approved");
                application.setStatus(Application.ApplicationStatus.APPROVED_FOR_MISSION);
            } else {
                application.setConclusion(conclusion.trim());
            }
        }
        
        // Save application
        applicationRepository.save(application);
        
        log.debug("Successfully imported application for {} {} (line {})", 
                candidateFirstName, candidateLastName, lineNumber);
    }
    
    /**
     * Clean up name field (remove extra spaces, trim)
     */
    private String cleanName(String name) {
        if (name == null) {
            return null;
        }
        // Remove extra spaces and trim
        return name.trim().replaceAll("\\s+", " ");
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
