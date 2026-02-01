package com.cvscreen.controller;

import com.cvscreen.service.ProUnityImportService;
import com.cvscreen.service.ProUnityImportService.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Pro-Unity JSON import operations
 */
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class ProUnityImportController {
    
    private final ProUnityImportService proUnityImportService;
    
    /**
     * Import candidates from Pro-Unity JSON export
     * 
     * Expected JSON format: Pro-Unity job post export with candidates array
     * 
     * Returns detailed import statistics and error information
     */
    @PostMapping("/prounity")
    public ResponseEntity<Map<String, Object>> importProUnity(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isValidJSONFile(file)) {
                response.put("success", false);
                response.put("message", "Invalid file type. Please upload a JSON file.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Perform import
            ImportResult result = proUnityImportService.importFromProUnity(file);
            
            // Build response
            response.put("success", result.getSuccessCount() > 0 || result.getSkippedCount() > 0);
            response.put("successCount", result.getSuccessCount());
            response.put("skippedCount", result.getSkippedCount());
            response.put("failedCount", result.getFailedCount());
            response.put("totalProcessed", result.getSuccessCount() + result.getSkippedCount() + result.getFailedCount());
            
            if (result.hasErrors()) {
                response.put("errors", result.getErrors().stream()
                    .map(error -> Map.of(
                        "lineNumber", error.getLineNumber(),
                        "message", error.getMessage()
                    ))
                    .collect(Collectors.toList()));
                
                response.put("message", String.format(
                    "Import completed with warnings. %d new records imported, %d duplicates skipped, %d failed.",
                    result.getSuccessCount(), result.getSkippedCount(), result.getFailedCount()
                ));
            } else {
                response.put("message", String.format(
                    "Import completed successfully. %d new records imported, %d duplicates skipped.",
                    result.getSuccessCount(), result.getSkippedCount()
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
     * Get import information for Pro-Unity format
     */
    @GetMapping("/prounity-info")
    public ResponseEntity<Map<String, Object>> getProUnityInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("format", "JSON (Pro-Unity export)");
        info.put("description", "Export job post data from Pro-Unity including candidates");
        
        info.put("instructions", Map.of(
            "step1", "In Pro-Unity, navigate to the job post you want to import",
            "step2", "Open the browser console (F12)",
            "step3", "Type: copy(JSON.stringify(jobPost, null, 2))",
            "step4", "Paste the content into a .json file",
            "step5", "Upload the file here"
        ));
        
        info.put("features", new String[] {
            "Automatic candidate creation",
            "Automatic job creation from project code",
            "Automatic company creation from department",
            "Duplicate detection using Pro-Unity UUID",
            "Status mapping from Pro-Unity to CVScreen",
            "Evaluation notes from comments and scores"
        });
        
        info.put("deduplication", "Applications are matched by Pro-Unity UUID. " +
                "Re-importing the same file will skip duplicates and only import new candidates.");
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Validate that uploaded file is a JSON
     */
    private boolean isValidJSONFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String contentType = file.getContentType();
        return filename.toLowerCase().endsWith(".json") ||
               (contentType != null && contentType.equals("application/json"));
    }
}
