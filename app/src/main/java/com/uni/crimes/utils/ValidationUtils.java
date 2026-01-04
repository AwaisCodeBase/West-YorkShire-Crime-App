package com.uni.crimes.utils;

import android.util.Patterns;

/**
 * Utility class for input validation
 * Provides consistent validation across the application
 */
public class ValidationUtils {
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && 
               Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Validate latitude coordinate
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }
    
    /**
     * Validate longitude coordinate
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }
    
    /**
     * Validate crime ID format
     */
    public static boolean isValidCrimeId(String crimeId) {
        return crimeId != null && !crimeId.trim().isEmpty() && crimeId.length() >= 3;
    }
    
    /**
     * Validate required text field
     */
    public static boolean isValidText(String text) {
        return text != null && !text.trim().isEmpty();
    }
    
    /**
     * Get validation error message for email
     */
    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Invalid email format";
        }
        return null;
    }
    
    /**
     * Get validation error message for password
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }
}