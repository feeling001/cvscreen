package com.cvscreen.repository;

import com.cvscreen.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Modifying
    @Query("UPDATE Application a SET a.company.id = :targetCompanyId WHERE a.company.id = :sourceCompanyId")
    void updateCompanyForApplications(@Param("sourceCompanyId") Long sourceCompanyId, 
                                      @Param("targetCompanyId") Long targetCompanyId);
    
    /**
     * Find all applications with pagination, supporting sorting by candidate name or average rating
     */
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate " +
           "LEFT JOIN FETCH a.job " +
           "LEFT JOIN FETCH a.company " +
           "WHERE (:candidateName IS NULL OR " +
           "LOWER(CONCAT(a.candidate.firstName, ' ', a.candidate.lastName)) LIKE LOWER(CONCAT('%', :candidateName, '%'))) " +
           "AND (:jobReference IS NULL OR LOWER(a.job.reference) LIKE LOWER(CONCAT('%', :jobReference, '%'))) " +
           "AND (:companyName IS NULL OR LOWER(a.company.name) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:roleCategory IS NULL OR LOWER(a.roleCategory) LIKE LOWER(CONCAT('%', :roleCategory, '%'))) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Application> findAllWithFilters(
            @Param("candidateName") String candidateName,
            @Param("jobReference") String jobReference,
            @Param("companyName") String companyName,
            @Param("roleCategory") String roleCategory,
            @Param("status") Application.ApplicationStatus status,
            Pageable pageable);
}
