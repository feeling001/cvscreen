package com.cvscreen.repository;

import com.cvscreen.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    
    List<Application> findByCandidateId(Long candidateId);
    
    List<Application> findByJobId(Long jobId);
    
    List<Application> findByCompanyId(Long companyId);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByRoleCategoryIgnoreCase(String roleCategory);
}