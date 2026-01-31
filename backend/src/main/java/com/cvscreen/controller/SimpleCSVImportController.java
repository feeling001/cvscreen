package com.cvscreen.controller;

import com.cvscreen.service.SimpleCSVImportService;
import com.cvscreen.service.SimpleCSVImportService.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for simple CSV import operations
 */
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class SimpleCSVImportController {
    
    private final SimpleCSVImportService csvImportService;
    
    /**
     * Import applications from CSV file
     * 
     * Expected CSV format (semicolon-separated):
     * application_date;job_reference;role_category;candidate_name;helper;candidate_first_name;company_name;daily_rate;evaluation_notes;conclusion
     * 
     * Returns detailed import statistics and error information
     */
    @PostMapping("/simple-csv")
    public ResponseEntity<Map<String, Object>> importSimpleCSV(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isValidCSVFile(file)) {
                response.put("success", false);
                response.put("message", "Invalid file type. Please upload a CSV file.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Perform import
            ImportResult result = csvImportService.importApplicationsFromCSV(file);
            
            // Build response
            response.put("success", result.getSuccessCount() > 0);
            response.put("successCount", result.getSuccessCount());
            response.put("failedCount", result.getFailedCount());
            response.put("totalProcessed", result.getSuccessCount() + result.getFailedCount());
            
            if (result.hasErrors()) {
                response.put("errors", result.getErrors().stream()
                    .map(error -> Map.of(
                        "lineNumber", error.getLineNumber(),
                        "message", error.getMessage()
                    ))
                    .collect(Collectors.toList()));
                
                response.put("message", String.format(
                    "Import completed with warnings. %d records imported successfully, %d failed.",
                    result.getSuccessCount(), result.getFailedCount()
                ));
            } else {
                response.put("message", String.format(
                    "Import completed successfully. %d records imported.",
                    result.getSuccessCount()
                ));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Import failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get import template information
     */
    @GetMapping("/simple-template-info")
    public ResponseEntity<Map<String, Object>> getTemplateInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("format", "CSV (semicolon-separated)");
        info.put("encoding", "UTF-8 or ISO-8859-1");
        info.put("columns", new String[] {
            "application_date",
            "job_reference",
            "role_category",
            "candidate_name",
            "helper",
            "candidate_first_name",
            "company_name",
            "daily_rate",
            "evaluation_notes",
            "conclusion"
        });
        
        info.put("example", "19/01/2025;I09526;Full Stack Senior Dev;De Roose;p;Antoine;Extia;595;Philippe Massaert Ne semble pas senior;0");
        
        info.put("fieldDescriptions", Map.of(
            "application_date", "Application date (format: dd/MM/yyyy) - Required",
            "job_reference", "Job reference (e.g., I09526) - Optional",
            "role_category", "Role/Category - Required",
            "candidate_name", "Candidate last name - Required",
            "helper", "Helper field (ignored)",
            "candidate_first_name", "Candidate first name - Required",
            "company_name", "Consulting company name - Optional",
            "daily_rate", "Daily rate in euros - Optional",
            "evaluation_notes", "Evaluation notes - Optional",
            "conclusion", "Conclusion (0/NOK=rejected, 1/OK=approved) - Optional"
        ));
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Validate that uploaded file is a CSV
     */
    private boolean isValidCSVFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String contentType = file.getContentType();
        return filename.toLowerCase().endsWith(".csv") ||
               (contentType != null && (
                   contentType.equals("text/csv") ||
                   contentType.equals("application/csv") ||
                   contentType.equals("text/plain")
               ));
    }
}
