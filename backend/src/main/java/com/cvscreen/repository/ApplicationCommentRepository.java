package com.cvscreen.repository;

import com.cvscreen.entity.ApplicationComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationCommentRepository extends JpaRepository<ApplicationComment, Long> {
    
    List<ApplicationComment> findByApplicationId(Long applicationId);
    
    List<ApplicationComment> findByUserId(Long userId);
    
    @Query("SELECT ac FROM ApplicationComment ac " +
           "LEFT JOIN FETCH ac.user " +
           "WHERE ac.application.id = :applicationId " +
           "ORDER BY ac.createdAt DESC")
    List<ApplicationComment> findByApplicationIdWithUser(@Param("applicationId") Long applicationId);
    
    @Query("SELECT ac FROM ApplicationComment ac " +
           "LEFT JOIN FETCH ac.user " +
           "LEFT JOIN FETCH ac.application a " +
           "LEFT JOIN FETCH a.job " +
           "WHERE a.candidate.id = :candidateId " +
           "ORDER BY ac.createdAt DESC")
    List<ApplicationComment> findByCandidateIdWithDetails(@Param("candidateId") Long candidateId);
    
    long countByApplicationId(Long applicationId);
    
    @Query("SELECT COUNT(ac) FROM ApplicationComment ac " +
           "WHERE ac.application.candidate.id = :candidateId")
    long countByCandidateId(@Param("candidateId") Long candidateId);
    
    @Query("SELECT COALESCE(AVG(ac.rating), 0.0) FROM ApplicationComment ac " +
           "WHERE ac.application.id = :applicationId AND ac.rating IS NOT NULL")
    Double getAverageRatingByApplicationId(@Param("applicationId") Long applicationId);
    
    @Query("SELECT COALESCE(AVG(ac.rating), 0.0) FROM ApplicationComment ac " +
           "WHERE ac.application.candidate.id = :candidateId AND ac.rating IS NOT NULL")
    Double getAverageRatingByCandidateId(@Param("candidateId") Long candidateId);
}
