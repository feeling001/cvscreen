package com.cvscreen.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    private String displayName;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional - only set if changing password
    
    private Boolean enabled;
}
