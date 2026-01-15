package com.gct.reportgenerator.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt hashed passwords
 * Used for creating initial test data
 * 
 * @author GCT Team
 * @since 1.0.0
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashed passwords for test accounts
        String adminPassword = encoder.encode("admin123");
        String designerPassword = encoder.encode("designer123");
        String viewerPassword = encoder.encode("viewer123");
        
        System.out.println("-- BCrypt hashed passwords for test accounts");
        System.out.println("-- Generated at: " + java.time.LocalDateTime.now());
        System.out.println();
        System.out.println("Admin password (admin123):");
        System.out.println(adminPassword);
        System.out.println();
        System.out.println("Designer password (designer123):");
        System.out.println(designerPassword);
        System.out.println();
        System.out.println("Viewer password (viewer123):");
        System.out.println(viewerPassword);
    }
}
