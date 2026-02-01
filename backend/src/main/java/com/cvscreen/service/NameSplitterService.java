package com.cvscreen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Service to intelligently split full names into first name and last name
 * using a dictionary of known first names from Europe and North Africa
 */
@Service
@Slf4j
public class NameSplitterService {
    
    private final Set<String> knownFirstNames = new HashSet<>();
    
    @PostConstruct
    public void init() {
        try {
            loadFirstNamesFromResource();
            log.info("Loaded {} known first names", knownFirstNames.size());
        } catch (IOException e) {
            log.error("Failed to load first names dictionary", e);
            // Load default common names as fallback
            loadDefaultFirstNames();
        }
    }
    
    /**
     * Split a full name into first name and last name
     * @param fullName The full name to split (e.g. "Jean Dupont" or "Marie-Claire De Smet")
     * @return Array with [firstName, lastName]
     */
    public String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Unknown", "Unknown"};
        }
        
        fullName = fullName.trim();
        
        // Handle special case: single name
        if (!fullName.contains(" ")) {
            return new String[]{fullName, fullName};
        }
        
        String[] parts = fullName.split("\\s+");
        
        // If only 2 parts, simple case
        if (parts.length == 2) {
            return new String[]{parts[0], parts[1]};
        }
        
        // Multiple parts - use dictionary to find first name(s)
        int firstNameEndIndex = findFirstNameEndIndex(parts);
        
        // Build first name and last name
        StringBuilder firstName = new StringBuilder();
        StringBuilder lastName = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i <= firstNameEndIndex) {
                if (firstName.length() > 0) firstName.append(" ");
                firstName.append(parts[i]);
            } else {
                if (lastName.length() > 0) lastName.append(" ");
                lastName.append(parts[i]);
            }
        }
        
        // Ensure both parts exist
        String first = firstName.length() > 0 ? firstName.toString() : parts[0];
        String last = lastName.length() > 0 ? lastName.toString() : parts[parts.length - 1];
        
        return new String[]{first, last};
    }
    
    /**
     * Find the index where first name(s) end
     */
    private int findFirstNameEndIndex(String[] parts) {
        // Try to find all consecutive known first names
        int lastKnownFirstNameIndex = -1;
        
        for (int i = 0; i < parts.length - 1; i++) { // Keep at least one part for last name
            String part = parts[i].toLowerCase();
            
            // Remove common prefixes/hyphens for checking
            String cleanPart = part.replace("-", "").replace("'", "");
            
            if (isKnownFirstName(cleanPart)) {
                lastKnownFirstNameIndex = i;
            } else {
                // Stop at first unknown part (likely start of last name)
                break;
            }
        }
        
        // If we found at least one known first name, use it
        if (lastKnownFirstNameIndex >= 0) {
            return lastKnownFirstNameIndex;
        }
        
        // Fallback: assume first part is first name
        return 0;
    }
    
    /**
     * Check if a name is a known first name
     */
    private boolean isKnownFirstName(String name) {
        String normalized = name.toLowerCase().trim();
        return knownFirstNames.contains(normalized);
    }
    
    /**
     * Load first names from CSV resource file
     */
    private void loadFirstNamesFromResource() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/first-names-europe-africa.csv");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Handle CSV format: name or name,gender or just name per line
                    String[] parts = line.split("[,;]");
                    if (parts.length > 0) {
                        knownFirstNames.add(parts[0].trim().toLowerCase());
                    }
                }
            }
        }
    }
    
    /**
     * Load common default first names as fallback
     */
    private void loadDefaultFirstNames() {
        // Common European and North African first names
        String[] commonNames = {
            // French
            "jean", "pierre", "michel", "andré", "philippe", "alain", "jacques", "bernard", "claude", "françois",
            "marie", "nathalie", "isabelle", "sylvie", "catherine", "françoise", "monique", "nicole", "christine", "martine",
            // Dutch/Flemish
            "jan", "peter", "johan", "luc", "marc", "paul", "dirk", "eric", "kris", "tom",
            "katrien", "ann", "els", "sophie", "sarah", "eva", "laura", "julie", "mieke", "inge",
            // English
            "john", "james", "robert", "michael", "william", "david", "richard", "joseph", "thomas", "charles",
            "mary", "patricia", "jennifer", "linda", "barbara", "elizabeth", "susan", "jessica", "sarah", "karen",
            // German
            "hans", "karl", "heinz", "werner", "günter", "klaus", "dieter", "jürgen", "wolfgang", "horst",
            "ursula", "monika", "petra", "angelika", "sabine", "renate", "karin", "ingrid", "helga", "gisela",
            // Spanish
            "josé", "antonio", "manuel", "francisco", "juan", "pedro", "jesús", "carlos", "miguel", "fernando",
            "maría", "carmen", "ana", "isabel", "dolores", "pilar", "teresa", "rosa", "francisca", "antonia",
            // Italian
            "giuseppe", "giovanni", "antonio", "mario", "luigi", "francesco", "angelo", "vincenzo", "pietro", "salvatore",
            "maria", "anna", "giuseppina", "rosa", "angela", "giovanna", "teresa", "lucia", "carmela", "caterina",
            // Arabic/North African
            "mohamed", "mohammed", "muhammad", "ahmed", "ali", "omar", "khalid", "youssef", "hassan", "ibrahim",
            "fatima", "aisha", "khadija", "zainab", "maryam", "amina", "salma", "nour", "yasmin", "laila",
            "mehdi", "karim", "said", "rachid", "hamza", "amine", "bilal", "zakaria", "adam", "ayoub",
            "samira", "sofia", "amira", "ines", "sara", "nadia", "leila", "malika", "houda", "sabrina",
            // Additional compound first names (hyphenated)
            "marie-christine", "marie-france", "jean-pierre", "jean-paul", "jean-claude", "jean-luc",
            "anne-marie", "marie-claire", "marie-thérèse", "jean-françois", "pierre-yves", "marc-antoine",
            // Common in Belgium
            "laurent", "olivier", "christophe", "stéphane", "cédric", "sébastien", "nicolas", "benoît", "vincent", "matthieu",
            "valérie", "sandrine", "véronique", "florence", "delphine", "céline", "aurélie", "émilie", "charlotte", "camille",
            // Additional variations
            "mohamed", "mahmoud", "mustafa", "abdullah", "ismail", "younes", "adil", "fouad", "tarik", "reda"
        };
        
        for (String name : commonNames) {
            knownFirstNames.add(name.toLowerCase());
        }
        
        log.info("Loaded {} default first names as fallback", knownFirstNames.size());
    }
}
