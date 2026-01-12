package com.elearning.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string has minimum length
     */
    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    /**
     * Check if string has maximum length
     */
    public static boolean hasMaxLength(String str, int maxLength) {
        return str != null && str.length() <= maxLength;
    }

    /**
     * Validate rating (1-5)
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /**
     * Validate progress percentage (0-100)
     */
    public static boolean isValidProgress(double progress) {
        return progress >= 0.0 && progress <= 100.0;
    }
}
