package com.cvscreen.service;

import com.cvscreen.dto.CreateUserRequest;
import com.cvscreen.dto.UpdateUserRequest;
import com.cvscreen.dto.UserDTO;
import com.cvscreen.entity.User;
import com.cvscreen.exception.ResourceNotFoundException;
import com.cvscreen.repository.ApplicationCommentRepository;
import com.cvscreen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final ApplicationCommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDTO(user);
    }
    
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        
        user = userRepository.save(user);
        return convertToDTO(user);
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if current user is admin or the user themselves
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        boolean isAdmin = "admin".equals(currentUsername);
        boolean isOwnProfile = user.getId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwnProfile) {
            throw new AccessDeniedException("You can only edit your own profile");
        }
        
        // Update fields
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Only admin can change enabled status
        if (request.getEnabled() != null && isAdmin) {
            user.setEnabled(request.getEnabled());
        }
        
        user = userRepository.save(user);
        return convertToDTO(user);
    }
    
    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        // Only admin can delete users
        if (!"admin".equals(currentUsername)) {
            throw new AccessDeniedException("Only admin can delete users");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Prevent deleting admin user
        if ("admin".equals(user.getUsername())) {
            throw new IllegalArgumentException("Cannot delete admin user");
        }
        
        userRepository.deleteById(id);
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        
        // Get comment count
        long commentCount = commentRepository.countByUserId(user.getId());
        dto.setCommentCount((int) commentCount);
        
        return dto;
    }
}
