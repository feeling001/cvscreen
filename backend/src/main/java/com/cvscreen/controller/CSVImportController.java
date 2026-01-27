package com.cvscreen.controller;

import com.cvscreen.service.CSVImportService;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class CSVImportController {
    
    private final CSVImportService csvImportService;
    
    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> importCSV(@RequestParam("file") MultipartFile file) {
        try {
            int importedCount = csvImportService.importApplicationsFromCSV(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("importedCount", importedCount);
            response.put("message", importedCount + " applications imported successfully");
            
            return ResponseEntity.ok(response);
        } catch (IOException | CsvException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to import CSV: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
