package com.cvscreen.service;

import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.repository.ApplicationRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVImportService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final CompanyService companyService;
    
    @Transactional
    public int importApplicationsFromCSV(MultipartFile file) throws IOException, CsvException {
        List<String[]> records = readCSV(file);
        
        if (records.isEmpty()) {
            return 0;
        }
        
        // Skip header row
        List<String[]> dataRecords = records.subList(1, records.size());
        int importedCount = 0;
        
        for (String[] record : dataRecords) {
            try {
                importApplicationRecord(record);
                importedCount++;
            } catch (Exception e) {
                log.error("Failed to import record: " + String.join(",", record), e);
            }
        }
        
        return importedCount;
    }
    
    private List<String[]> readCSV(MultipartFile file) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            return reader.readAll();
        }
    }
    
    private void importApplicationRecord(String[] record) {
        // Expected CSV columns: firstName, lastName, company, jobReference, dailyRate, applicationDate, status, conclusion
        
        String firstName = getFieldValue(record, 0);
        String lastName = getFieldValue(record, 1);
        String companyName = getFieldValue(record, 2);
        String jobReference = getFieldValue(record, 3);
        String dailyRateStr = getFieldValue(record, 4);
        String applicationDateStr = getFieldValue(record, 5);
        String statusStr = getFieldValue(record, 6);
        String conclusion = getFieldValue(record, 7);
        String roleCategory = getFieldValue(record, 8);
        
        // Find or create candidate
        Candidate candidate = candidateService.findOrCreateCandidate(firstName, lastName);
        
        // Find or create job if reference provided
        Job job = null;
        if (jobReference != null && !jobReference.isEmpty()) {
            job = jobService.findOrCreateJob(jobReference, "Imported Job", 
                roleCategory != null ? roleCategory : "Unknown");
        }
        
        // Find or create company
        Company company = null;
        if (companyName != null && !companyName.isEmpty()) {
            company = companyService.findOrCreateCompany(companyName);
        }
        
        // Parse daily rate
        BigDecimal dailyRate = null;
        if (dailyRateStr != null && !dailyRateStr.isEmpty()) {
            try {
                dailyRate = new BigDecimal(dailyRateStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid daily rate: " + dailyRateStr);
            }
        }
        
        // Parse application date
        LocalDate applicationDate = parseDate(applicationDateStr);
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
        
        // Parse status
        Application.ApplicationStatus status = Application.ApplicationStatus.CV_RECEIVED;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Application.ApplicationStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: " + statusStr);
            }
        }
        
        // Create application
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setRoleCategory(roleCategory != null ? roleCategory : "Unknown");
        application.setCompany(company);
        application.setDailyRate(dailyRate);
        application.setApplicationDate(applicationDate);
        application.setStatus(status);
        application.setConclusion(conclusion);
        
        applicationRepository.save(application);
    }
    
    private String getFieldValue(String[] record, int index) {
        if (record.length > index) {
            String value = record[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
        formatters.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        log.warn("Could not parse date: " + dateStr);
        return null;
    }
}
