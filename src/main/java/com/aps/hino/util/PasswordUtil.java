package com.aps.hino.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public final class PasswordUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    
    // Private constructor to prevent instantiation
    private PasswordUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Hash a plain text password using BCrypt
     * 
     * @param plainPassword the plain text password to hash
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }
    
    /**
     * Verify a plain text password against a hashed password
     * 
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(plainPassword, hashedPassword);
    }
}
