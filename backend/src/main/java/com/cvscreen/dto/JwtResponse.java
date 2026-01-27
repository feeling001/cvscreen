package com.cvscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String displayName;
    
    public JwtResponse(String token, String username, String displayName) {
        this.token = token;
        this.username = username;
        this.displayName = displayName;
    }
}
