package com.cvscreen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/info")
@CrossOrigin(origins = "${cvscreen.cors.allowed-origins}")
public class InfoController {

    @Value("${application.version}")
    private String version;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "version", version,
            "environment", activeProfile
        ));
    }
}