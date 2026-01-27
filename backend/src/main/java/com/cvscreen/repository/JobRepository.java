package com.cvscreen.repository;

import com.cvscreen.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Optional<Job> findByReference(String reference);
    
    List<Job> findByReferenceContainingIgnoreCase(String reference);
    
    List<Job> findByStatus(Job.JobStatus status);
    
    List<Job> findByCategoryIgnoreCase(String category);
    
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Job> searchJobs(@Param("searchTerm") String searchTerm);
}
