package com.cvscreen.controller;

import com.cvscreen.dto.ApplicationDTO;
import com.cvscreen.dto.CreateApplicationRequest;
import com.cvscreen.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ApplicationDTO> applicationPage = applicationService.getAllApplicationsPaginated(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("applications", applicationPage.getContent());
        response.put("currentPage", applicationPage.getNumber());
        response.put("totalItems", applicationPage.getTotalElements());
        response.put("totalPages", applicationPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchApplications(
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String jobReference,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String roleCategory,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ApplicationDTO> applicationPage = applicationService.searchApplicationsPaginated(
                candidateName, jobReference, companyName, roleCategory, status, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("applications", applicationPage.getContent());
        response.put("currentPage", applicationPage.getNumber());
        response.put("totalItems", applicationPage.getTotalElements());
        response.put("totalPages", applicationPage.getTotalPages());
        
        return ResponseEntity.ok(response);
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
