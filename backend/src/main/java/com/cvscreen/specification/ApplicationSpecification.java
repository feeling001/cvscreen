package com.cvscreen.specification;

import com.cvscreen.entity.Application;
import com.cvscreen.entity.Candidate;
import com.cvscreen.entity.Company;
import com.cvscreen.entity.Job;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {
    
    public static Specification<Application> searchApplications(
            String candidateName, 
            String jobReference,
            String companyName, 
            String roleCategory, 
            String statusStr) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Eager fetch joins to avoid N+1 queries
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("candidate", JoinType.LEFT);
                root.fetch("job", JoinType.LEFT);
                root.fetch("company", JoinType.LEFT);
            }
            
            // Candidate name search - FIXED: use firstName and lastName fields
            if (candidateName != null && !candidateName.trim().isEmpty()) {
                Join<Application, Candidate> candidateJoin = root.join("candidate", JoinType.LEFT);
                Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(candidateJoin.get("firstName"), " "),
                    candidateJoin.get("lastName")
                );
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(fullName),
                    "%" + candidateName.toLowerCase() + "%"
                ));
            }
            
            // Job reference search
            if (jobReference != null && !jobReference.trim().isEmpty()) {
                Join<Application, Job> jobJoin = root.join("job", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(jobJoin.get("reference")),
                    "%" + jobReference.toLowerCase() + "%"
                ));
            }
            
            // Company name search
            if (companyName != null && !companyName.trim().isEmpty()) {
                Join<Application, Company> companyJoin = root.join("company", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(companyJoin.get("name")),
                    "%" + companyName.toLowerCase() + "%"
                ));
            }
            
            // Role category search
            if (roleCategory != null && !roleCategory.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("roleCategory")),
                    "%" + roleCategory.toLowerCase() + "%"
                ));
            }
            
            // Status filter
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(statusStr);
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status value, ignore this filter
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
