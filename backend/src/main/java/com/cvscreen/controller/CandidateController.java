package com.cvscreen.controller;

import com.cvscreen.dto.CandidateDTO;
import com.cvscreen.dto.CreateCandidateRequest;
import com.cvscreen.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class CandidateController {
    
    private final CandidateService candidateService;
    
    @GetMapping
    public ResponseEntity<List<CandidateDTO>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CandidateDTO> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CandidateDTO>> searchCandidates(@RequestParam String q) {
        return ResponseEntity.ok(candidateService.searchCandidates(q));
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
