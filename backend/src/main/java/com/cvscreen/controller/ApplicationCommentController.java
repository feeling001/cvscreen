package com.cvscreen.controller;

import com.cvscreen.dto.ApplicationCommentDTO;
import com.cvscreen.dto.CreateCommentRequest;
import com.cvscreen.service.ApplicationCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications/{applicationId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8082")
public class ApplicationCommentController {
    
    private final ApplicationCommentService commentService;
    
    @GetMapping
    public ResponseEntity<List<ApplicationCommentDTO>> getCommentsForApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(commentService.getCommentsForApplication(applicationId));
    }
    
    @PostMapping
    public ResponseEntity<ApplicationCommentDTO> createComment(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(applicationId, username, request));
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
