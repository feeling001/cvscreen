package com.cvscreen.repository;

import com.cvscreen.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByCandidateId(Long candidateId);
    
    List<Application> findByJobId(Long jobId);
    
    List<Application> findByCompanyId(Long companyId);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByRoleCategoryIgnoreCase(String roleCategory);
    
    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.candidate c " +
           "LEFT JOIN FETCH a.job j " +
           "LEFT JOIN FETCH a.company co " +
           "WHERE (:candidateName IS NULL OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :candidateName, '%'))) " +
           "AND (:jobReference IS NULL OR :jobReference = '' OR LOWER(j.reference) LIKE LOWER(CONCAT('%', :jobReference, '%'))) " +
           "AND (:companyName IS NULL OR :companyName = '' OR LOWER(co.name) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:roleCategory IS NULL OR :roleCategory = '' OR LOWER(a.roleCategory) LIKE LOWER(CONCAT('%', :roleCategory, '%'))) " +
           "AND (:statusStr IS NULL OR :statusStr = '' OR CAST(a.status AS string) = :statusStr)")
    List<Application> searchApplications(
        @Param("candidateName") String candidateName,
        @Param("jobReference") String jobReference,
        @Param("companyName") String companyName,
        @Param("roleCategory") String roleCategory,
        @Param("statusStr") String statusStr
    );
}
