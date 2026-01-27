package com.cvscreen.config;

import com.cvscreen.entity.User;
import com.cvscreen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setDisplayName("Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            
            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        }
    }
}
