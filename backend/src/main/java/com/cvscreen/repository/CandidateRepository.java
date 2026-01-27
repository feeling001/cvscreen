package com.cvscreen.repository;

import com.cvscreen.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByFirstNameAndLastName(String firstName, String lastName);
    
    List<Candidate> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT c FROM Candidate c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Candidate> searchByName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT DISTINCT c FROM Candidate c " +
           "LEFT JOIN FETCH c.applications a " +
           "LEFT JOIN FETCH a.job " +
           "LEFT JOIN FETCH a.company " +
           "WHERE c.id = :candidateId")
    Optional<Candidate> findByIdWithApplications(@Param("candidateId") Long candidateId);
}
