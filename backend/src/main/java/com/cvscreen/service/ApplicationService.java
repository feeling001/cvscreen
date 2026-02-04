package com.cvscreen.service;

import com.cvscreen.dto.ApplicationDTO;
import com.cvscreen.dto.CreateApplicationRequest;
import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationCommentRepository;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.CandidateRepository;
import com.cvscreen.repository.CompanyRepository;
import com.cvscreen.repository.JobRepository;
import com.cvscreen.specification.ApplicationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
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
    private final ApplicationCommentRepository commentRepository;
    
    private static final String CV_STORAGE_PATH = "./cvs";
    
    @Transactional(readOnly = true)
    public Page<ApplicationDTO> getAllApplicationsPaginated(Pageable pageable) {
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by computed fields (candidateName or averageRating)
        if ("candidateName".equals(sortProperty) || "averageRating".equals(sortProperty)) {
            return getAllApplicationsWithCustomSort(null, null, null, null, null, pageable);
        }
        
        return applicationRepository.findAll(pageable).map(this::convertToDTO);
    }
    
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
    public Page<ApplicationDTO> searchApplicationsPaginated(String candidateName, String jobReference, 
                                                           String companyName, String roleCategory, 
                                                           String status, Pageable pageable) {
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : null;
        
        // Check if sorting by computed fields
        if ("candidateName".equals(sortProperty) || "averageRating".equals(sortProperty)) {
            return getAllApplicationsWithCustomSort(candidateName, jobReference, companyName, roleCategory, status, pageable);
        }
        
        Specification<Application> spec = ApplicationSpecification.searchApplications(
            candidateName, 
            jobReference, 
            companyName, 
            roleCategory, 
            status
        );
        
        return applicationRepository.findAll(spec, pageable).map(this::convertToDTO);
    }
    
    /**
     * Custom method to handle sorting by candidateName or averageRating
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDTO> getAllApplicationsWithCustomSort(
            String candidateName, String jobReference, String companyName, 
            String roleCategory, String status, Pageable pageable) {
        
        // Get all applications matching the filters (without pagination for sorting)
        Specification<Application> spec = ApplicationSpecification.searchApplications(
            candidateName, jobReference, companyName, roleCategory, status
        );
        
        List<Application> allApplications = applicationRepository.findAll(spec);
        
        // Convert to DTOs
        List<ApplicationDTO> dtos = allApplications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // Apply custom sorting
        String sortProperty = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().getProperty() 
            : "applicationDate";
        boolean ascending = pageable.getSort().iterator().hasNext() 
            ? pageable.getSort().iterator().next().isAscending() 
            : false;
        
        if ("candidateName".equals(sortProperty)) {
            // Sort by candidate name
            if (ascending) {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getCandidateName() != null ? dto.getCandidateName().toLowerCase() : "",
                    Comparator.naturalOrder()
                ));
            } else {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getCandidateName() != null ? dto.getCandidateName().toLowerCase() : "",
                    Comparator.reverseOrder()
                ));
            }
        } else if ("averageRating".equals(sortProperty)) {
            // Sort by average rating
            if (ascending) {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getAverageRating() != null ? dto.getAverageRating() : 0.0,
                    Comparator.naturalOrder()
                ));
            } else {
                dtos.sort(Comparator.comparing(
                    dto -> dto.getAverageRating() != null ? dto.getAverageRating() : 0.0,
                    Comparator.reverseOrder()
                ));
            }
        }
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        List<ApplicationDTO> pageContent = dtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationDTO> searchApplications(String candidateName, String jobReference, 
                                                   String companyName, String roleCategory, String status) {
        Specification<Application> spec = ApplicationSpecification.searchApplications(
            candidateName, 
            jobReference, 
            companyName, 
            roleCategory, 
            status
        );
        
        return applicationRepository.findAll(spec).stream()
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
        
        Path uploadPath = Paths.get(CV_STORAGE_PATH);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
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
        
        // Get comment count and average rating for this application
        long commentCount = commentRepository.countByApplicationId(application.getId());
        dto.setCommentCount(commentCount);
        
        Double averageRating = commentRepository.getAverageRatingByApplicationId(application.getId());
        dto.setAverageRating(averageRating);
        
        return dto;
    }
}
