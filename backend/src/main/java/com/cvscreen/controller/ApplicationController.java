package com.cvscreen.controller;

import com.cvscreen.dto.ApplicationDTO;
import com.cvscreen.dto.CreateApplicationRequest;
import com.cvscreen.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ApplicationDTO>> searchApplications(
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String jobReference,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String roleCategory,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(applicationService.searchApplications(
                candidateName, jobReference, companyName, roleCategory, status));
    }
    
    @PostMapping
    public ResponseEntity<ApplicationDTO> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDTO> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/cv")
    public ResponseEntity<ApplicationDTO> uploadCV(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(applicationService.uploadCV(id, file));
    }
}
