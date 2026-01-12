package com.elearning.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public class PasswordUtil {
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Hash a plain text password
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if password meets minimum requirements
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
}
