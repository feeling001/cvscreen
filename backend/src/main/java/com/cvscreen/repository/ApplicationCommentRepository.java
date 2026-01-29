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
    
    long countByApplicationId(Long applicationId);
}
