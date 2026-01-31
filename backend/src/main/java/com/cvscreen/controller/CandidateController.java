package com.cvscreen.controller;

import com.cvscreen.dto.CandidateDTO;
import com.cvscreen.dto.CreateCandidateRequest;
import com.cvscreen.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class CandidateController {
    
    private final CandidateService candidateService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CandidateDTO> candidatePage = candidateService.getAllCandidatesPaginated(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("candidates", candidatePage.getContent());
        response.put("currentPage", candidatePage.getNumber());
        response.put("totalItems", candidatePage.getTotalElements());
        response.put("totalPages", candidatePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CandidateDTO> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCandidates(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CandidateDTO> candidatePage = candidateService.searchCandidatesPaginated(q, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("candidates", candidatePage.getContent());
        response.put("currentPage", candidatePage.getNumber());
        response.put("totalItems", candidatePage.getTotalElements());
        response.put("totalPages", candidatePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<CandidateDTO> createCandidate(@Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateService.createCandidate(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CandidateDTO> updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidate(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
}
