package com.cvscreen.controller;

import com.cvscreen.dto.CreateUserRequest;
import com.cvscreen.dto.UpdateUserRequest;
import com.cvscreen.dto.UserDTO;
import com.cvscreen.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8082", "http://127.0.0.1:8082"})
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        
        String currentUsername = authentication.getName();
        
        // Only admin can create users
        if (!"admin".equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        String currentUsername = authentication.getName();
        return ResponseEntity.ok(userService.updateUser(id, request, currentUsername));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        
        String currentUsername = authentication.getName();
        userService.deleteUser(id, currentUsername);
        return ResponseEntity.noContent().build();
    }
}
