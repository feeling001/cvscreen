package com.cvscreen.controller;

import com.cvscreen.dto.CreateJobRequest;
import com.cvscreen.dto.JobDTO;
import com.cvscreen.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class JobController {
    
    private final JobService jobService;
    
    @GetMapping
    public ResponseEntity<List<JobDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
    
    @GetMapping("/reference/{reference}")
    public ResponseEntity<JobDTO> getJobByReference(@PathVariable String reference) {
        return ResponseEntity.ok(jobService.getJobByReference(reference));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<JobDTO>> searchJobs(@RequestParam String q) {
        return ResponseEntity.ok(jobService.searchJobs(q));
    }
    
    @PostMapping
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JobDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
