package com.cvscreen.repository;

import com.cvscreen.entity.CandidateReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateReviewRepository extends JpaRepository<CandidateReview, Long> {
    
    List<CandidateReview> findByCandidateId(Long candidateId);
    
    List<CandidateReview> findByUserId(Long userId);
    
    @Query("SELECT cr FROM CandidateReview cr " +
           "LEFT JOIN FETCH cr.user " +
           "WHERE cr.candidate.id = :candidateId " +
           "ORDER BY cr.createdAt DESC")
    List<CandidateReview> findByCandidateIdWithUser(@Param("candidateId") Long candidateId);
}
