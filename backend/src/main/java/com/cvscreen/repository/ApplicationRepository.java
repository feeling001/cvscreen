package com.cvscreen.repository;

import com.cvscreen.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    
    List<Application> findByCandidateId(Long candidateId);
    
    List<Application> findByJobId(Long jobId);
    
    List<Application> findByCompanyId(Long companyId);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByRoleCategoryIgnoreCase(String roleCategory);
    
    boolean existsByExternalId(String externalId);
    
    Optional<Application> findByExternalId(String externalId);
    
    @Modifying
    @Query("UPDATE Application a SET a.candidate.id = :targetCandidateId WHERE a.candidate.id = :sourceCandidateId")
    void updateCandidateForApplications(@Param("sourceCandidateId") Long sourceCandidateId, 
                                       @Param("targetCandidateId") Long targetCandidateId);
}
