package com.example.financeapp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class để generate BCrypt password hash
 * Chạy class này để tạo hash cho password test
 * 
 * Cách chạy:
 * 1. Mở file này trong IDE
 * 2. Right-click -> Run 'PasswordHashGenerator.main()'
 * 3. Copy hash và paste vào SQL script
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=".repeat(80));
        System.out.println("BCRYPT PASSWORD HASH GENERATOR");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Password test
        String[] passwords = {
            "Test123!",
            "Admin123!",
            "User123!"
        };
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
            
            // Verify
            boolean matches = encoder.matches(password, hash);
            System.out.println("Verification: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));
            System.out.println("-".repeat(80));
            System.out.println();
        }
        
        // SQL Insert Example
        System.out.println("=".repeat(80));
        System.out.println("SQL INSERT EXAMPLE");
        System.out.println("=".repeat(80));
        System.out.println();
        
        String testPassword = "Test123!";
        String testHash = encoder.encode(testPassword);
        
        System.out.println("INSERT INTO Users (UserID, userName, email, password, fullName, CreatedAt, IsActive)");
        System.out.println("VALUES (");
        System.out.println("    UUID(),");
        System.out.println("    'testuser',");
        System.out.println("    'test@example.com',");
        System.out.println("    '" + testHash + "',");
        System.out.println("    'Test User',");
        System.out.println("    NOW(),");
        System.out.println("    true");
        System.out.println(");");
        System.out.println();
        System.out.println("Password for this user: " + testPassword);
        System.out.println("=".repeat(80));
    }
}

