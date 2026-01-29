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
        return commentRepository.findByApplicationIdWithUser(applicationId).stream()
            .map(this::convertToDTO)
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
        
        comment = commentRepository.save(comment);
        return convertToDTO(comment);
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
    
    private ApplicationCommentDTO convertToDTO(ApplicationComment comment) {
        ApplicationCommentDTO dto = new ApplicationCommentDTO();
        dto.setId(comment.getId());
        dto.setApplicationId(comment.getApplication().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setDisplayName(comment.getUser().getDisplayName());
        dto.setComment(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
