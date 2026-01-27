package com.cvscreen.controller;

import com.cvscreen.dto.CompanyDTO;
import com.cvscreen.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class CompanyController {
    
    private final CompanyService companyService;
    
    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CompanyDTO>> searchCompanies(@RequestParam String q) {
        return ResponseEntity.ok(companyService.searchCompanies(q));
    }
    
    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(
            @RequestParam String name,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(name, notes));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(companyService.updateCompany(id, name, notes));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
