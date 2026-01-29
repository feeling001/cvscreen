package com.cvscreen.controller;

import com.cvscreen.service.EnhancedCSVImportService;
import com.cvscreen.service.EnhancedCSVImportService.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for CSV import operations with detailed error reporting
 */
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class EnhancedCSVImportController {
    
    private final EnhancedCSVImportService csvImportService;
    
    /**
     * Import applications from CSV file
     * 
     * Expected CSV format (semicolon-separated):
     * Date;Canal;Demande;Fonction;NOM - Prenom;Linkedin;Supplier;Reviewer - Avis CV 1;Reviewer - Avis CV 2;Reviewer - Avis interview
     * 
     * Returns detailed import statistics and error information
     */
    @PostMapping("/enhanced-csv")
    public ResponseEntity<Map<String, Object>> importEnhancedCSV(
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
    @GetMapping("/template-info")
    public ResponseEntity<Map<String, Object>> getTemplateInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("format", "CSV (semicolon-separated)");
        info.put("encoding", "UTF-8 or ISO-8859-1");
        info.put("columns", new String[] {
            "Date",
            "Canal",
            "Demande",
            "Fonction",
            "NOM - Prenom",
            "Linkedin",
            "Supplier",
            "Reviewer - Avis CV 1",
            "Reviewer - Avis CV 2",
            "Reviewer - Avis interview"
        });
        
        info.put("example", "15/01/2024;Pro-Unity;I01234;System Architect;DUPONT - Jean;https://linkedin.com/in/jean-dupont;Accenture;Good profile;Strong technical skills;Excellent communication");
        
        info.put("fieldDescriptions", Map.of(
            "Date", "Application date (format: dd/MM/yyyy)",
            "Canal", "Source/channel (e.g., Pro-Unity)",
            "Demande", "Job reference (e.g., I01234) - optional for spontaneous applications",
            "Fonction", "Role/Category (required)",
            "NOM - Prenom", "Candidate name in format 'LASTNAME - Firstname'",
            "Linkedin", "LinkedIn profile URL (optional)",
            "Supplier", "Consulting company name (optional)",
            "Reviewer - Avis CV 1", "First CV review feedback (optional)",
            "Reviewer - Avis CV 2", "Second CV review feedback (optional)",
            "Reviewer - Avis interview", "Interview feedback (optional)"
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
