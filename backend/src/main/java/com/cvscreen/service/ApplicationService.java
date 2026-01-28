package com.cvscreen.service;

import com.cvscreen.dto.ApplicationDTO;
import com.cvscreen.dto.CreateApplicationRequest;
import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CandidateRepository;
import com.cvscreen.repository.CandidateReviewRepository;
import com.cvscreen.repository.CompanyRepository;
import com.cvscreen.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final CandidateReviewRepository reviewRepository;
    
    private static final String CV_STORAGE_PATH = "./cvs";
    
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAllApplications() {
        return applicationRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ApplicationDTO getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return convertToDTO(application);
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationDTO> searchApplications(String candidateName, String jobReference, 
                                                   String companyName, String roleCategory, String status) {
        // Convert empty strings to null to avoid type casting issues with PostgreSQL
        String cleanCandidateName = (candidateName != null && !candidateName.trim().isEmpty()) ? candidateName : null;
        String cleanJobReference = (jobReference != null && !jobReference.trim().isEmpty()) ? jobReference : null;
        String cleanCompanyName = (companyName != null && !companyName.trim().isEmpty()) ? companyName : null;
        String cleanRoleCategory = (roleCategory != null && !roleCategory.trim().isEmpty()) ? roleCategory : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status : null;
        
        return applicationRepository.searchApplications(
                cleanCandidateName, 
                cleanJobReference, 
                cleanCompanyName, 
                cleanRoleCategory, 
                cleanStatus
            ).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ApplicationDTO createApplication(CreateApplicationRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + request.getCandidateId()));
        
        Application application = new Application();
        application.setCandidate(candidate);
        
        if (request.getJobId() != null) {
            Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));
            application.setJob(job);
        }
        
        application.setRoleCategory(request.getRoleCategory());
        
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));
            application.setCompany(company);
        }
        
        application.setDailyRate(request.getDailyRate());
        application.setApplicationDate(request.getApplicationDate());
        application.setConclusion(request.getConclusion());
        application.setEvaluationNotes(request.getEvaluationNotes());
        
        if (request.getStatus() != null) {
            application.setStatus(Application.ApplicationStatus.valueOf(request.getStatus()));
        } else {
            application.setStatus(Application.ApplicationStatus.CV_RECEIVED);
        }
        
        application = applicationRepository.save(application);
        return convertToDTO(application);
    }
    
    @Transactional
    public ApplicationDTO updateApplication(Long id, CreateApplicationRequest request) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + request.getCandidateId()));
        
        application.setCandidate(candidate);
        
        if (request.getJobId() != null) {
            Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));
            application.setJob(job);
        } else {
            application.setJob(null);
        }
        
        application.setRoleCategory(request.getRoleCategory());
        
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));
            application.setCompany(company);
        } else {
            application.setCompany(null);
        }
        
        application.setDailyRate(request.getDailyRate());
        application.setApplicationDate(request.getApplicationDate());
        application.setConclusion(request.getConclusion());
        application.setEvaluationNotes(request.getEvaluationNotes());
        
        if (request.getStatus() != null) {
            application.setStatus(Application.ApplicationStatus.valueOf(request.getStatus()));
        }
        
        application = applicationRepository.save(application);
        return convertToDTO(application);
    }
    
    @Transactional
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        applicationRepository.deleteById(id);
    }
    
    @Transactional
    public ApplicationDTO uploadCV(Long applicationId, MultipartFile file) throws IOException {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        // Create directory if not exists
        Path uploadPath = Paths.get(CV_STORAGE_PATH);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        application.setCvFilePath(newFilename);
        application = applicationRepository.save(application);
        
        return convertToDTO(application);
    }
    
    private ApplicationDTO convertToDTO(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setCandidateName(application.getCandidate().getFullName());
        
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobReference(application.getJob().getReference());
            dto.setJobTitle(application.getJob().getTitle());
        }
        
        dto.setRoleCategory(application.getRoleCategory());
        
        if (application.getCompany() != null) {
            dto.setCompanyId(application.getCompany().getId());
            dto.setCompanyName(application.getCompany().getName());
        }
        
        dto.setDailyRate(application.getDailyRate());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus().name());
        dto.setConclusion(application.getConclusion());
        dto.setEvaluationNotes(application.getEvaluationNotes());
        dto.setCvFilePath(application.getCvFilePath());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        
        // Check if candidate has reviews
        long reviewCount = reviewRepository.findByCandidateId(application.getCandidate().getId()).size();
        dto.setHasReviews(reviewCount > 0);
        
        return dto;
    }
}
