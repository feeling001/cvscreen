package com.cvscreen.service;

import com.cvscreen.dto.ApplicationCommentDTO;
import com.cvscreen.dto.CreateCommentRequest;
import com.cvscreen.entity.Application;
import com.cvscreen.entity.ApplicationComment;
import com.cvscreen.entity.User;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationCommentRepository;
import com.cvscreen.repository.ApplicationRepository;
import com.cvscreen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationCommentService {
    
    private final ApplicationCommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<ApplicationCommentDTO> getCommentsForApplication(Long applicationId) {
        List<ApplicationComment> comments = commentRepository.findByApplicationIdWithUser(applicationId);
        return comments.stream()
            .map(comment -> convertToDTO(comment, true))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationCommentDTO> getAllCommentsForCandidate(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        Long candidateId = application.getCandidate().getId();
        
        List<ApplicationComment> allComments = commentRepository.findByCandidateIdWithDetails(candidateId);
        
        return allComments.stream()
            .map(comment -> {
                boolean isCurrent = comment.getApplication().getId().equals(applicationId);
                return convertToDTO(comment, isCurrent);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ApplicationCommentDTO createComment(Long applicationId, String username, CreateCommentRequest request) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        ApplicationComment comment = new ApplicationComment();
        comment.setApplication(application);
        comment.setUser(user);
        comment.setComment(request.getComment());
        comment.setRating(request.getRating());
        
        comment = commentRepository.save(comment);
        return convertToDTO(comment, true);
    }
    
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
    
    @Transactional(readOnly = true)
    public long getCommentCountForApplication(Long applicationId) {
        return commentRepository.countByApplicationId(applicationId);
    }
    
    @Transactional(readOnly = true)
    public Double getAverageRatingForApplication(Long applicationId) {
        return commentRepository.getAverageRatingByApplicationId(applicationId);
    }
    
    private ApplicationCommentDTO convertToDTO(ApplicationComment comment, boolean isCurrentApplication) {
        ApplicationCommentDTO dto = new ApplicationCommentDTO();
        dto.setId(comment.getId());
        dto.setApplicationId(comment.getApplication().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setDisplayName(comment.getUser().getDisplayName());
        dto.setComment(comment.getComment());
        dto.setRating(comment.getRating());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setCurrentApplication(isCurrentApplication); // Fixed: use setCurrentApplication instead of setIsCurrentApplication
        
        // Add job reference and role category for context
        if (comment.getApplication().getJob() != null) {
            dto.setJobReference(comment.getApplication().getJob().getReference());
        }
        dto.setRoleCategory(comment.getApplication().getRoleCategory());
        
        return dto;
    }
}
