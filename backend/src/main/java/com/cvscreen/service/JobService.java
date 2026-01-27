package com.cvscreen.service;

import com.cvscreen.dto.CreateJobRequest;
import com.cvscreen.dto.JobDTO;
import com.cvscreen.entity.Job;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    
    private final JobRepository jobRepository;
    
    @Transactional(readOnly = true)
    public List<JobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return convertToDTO(job);
    }
    
    @Transactional(readOnly = true)
    public JobDTO getJobByReference(String reference) {
        Job job = jobRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with reference: " + reference));
        return convertToDTO(job);
    }
    
    @Transactional(readOnly = true)
    public List<JobDTO> searchJobs(String searchTerm) {
        return jobRepository.searchJobs(searchTerm).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public JobDTO createJob(CreateJobRequest request) {
        Job job = new Job();
        job.setReference(request.getReference());
        job.setTitle(request.getTitle());
        job.setCategory(request.getCategory());
        job.setPublicationDate(request.getPublicationDate());
        job.setSource(request.getSource() != null ? request.getSource() : "Pro-Unity");
        job.setDescription(request.getDescription());
        
        if (request.getStatus() != null) {
            job.setStatus(Job.JobStatus.valueOf(request.getStatus()));
        } else {
            job.setStatus(Job.JobStatus.OPEN);
        }
        
        job = jobRepository.save(job);
        return convertToDTO(job);
    }
    
    @Transactional
    public JobDTO updateJob(Long id, CreateJobRequest request) {
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        
        job.setReference(request.getReference());
        job.setTitle(request.getTitle());
        job.setCategory(request.getCategory());
        job.setPublicationDate(request.getPublicationDate());
        job.setSource(request.getSource() != null ? request.getSource() : job.getSource());
        job.setDescription(request.getDescription());
        
        if (request.getStatus() != null) {
            job.setStatus(Job.JobStatus.valueOf(request.getStatus()));
        }
        
        job = jobRepository.save(job);
        return convertToDTO(job);
    }
    
    @Transactional
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }
    
    public Job findOrCreateJob(String reference, String title, String category) {
        return jobRepository.findByReference(reference)
            .orElseGet(() -> {
                Job newJob = new Job();
                newJob.setReference(reference);
                newJob.setTitle(title != null ? title : "Unknown");
                newJob.setCategory(category != null ? category : "Unknown");
                newJob.setStatus(Job.JobStatus.OPEN);
                return jobRepository.save(newJob);
            });
    }
    
    private JobDTO convertToDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setReference(job.getReference());
        dto.setTitle(job.getTitle());
        dto.setCategory(job.getCategory());
        dto.setPublicationDate(job.getPublicationDate());
        dto.setStatus(job.getStatus().name());
        dto.setSource(job.getSource());
        dto.setDescription(job.getDescription());
        dto.setApplicationCount(job.getApplications() != null ? job.getApplications().size() : 0);
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }
}
