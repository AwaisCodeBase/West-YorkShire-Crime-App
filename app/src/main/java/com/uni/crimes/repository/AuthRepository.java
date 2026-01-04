package com.uni.crimes.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.uni.crimes.model.User;

/**
 * Repository class for Authentication operations using local storage
 * Simplified version without Firebase for demo purposes
 * Part of MVVM architecture pattern
 */
public class AuthRepository {
    
    private static final String TAG = "AuthRepository";
    private static final String PREFS_NAME = "CrimesAppPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // Admin email - hardcoded as per coursework requirements
    private static final String ADMIN_EMAIL = "admin@crimes.com";
    private static final String ADMIN_PASSWORD = "admin123";
    
    private SharedPreferences preferences;
    
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }
    
    public AuthRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Login user with local authentication
     */
    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting login for: " + email);
        
        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError("Email and password are required");
            return;
        }
        
        // Check for admin credentials
        if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
            User adminUser = new User("admin_uid", "Admin User", email, "Admin");
            saveUserToPreferences(adminUser);
            callback.onSuccess(adminUser);
            return;
        }
        
        // Check for demo user credentials
        if ("user@crimes.com".equalsIgnoreCase(email) && "user123".equals(password)) {
            User regularUser = new User("user_uid", "Regular User", email, "User");
            saveUserToPreferences(regularUser);
            callback.onSuccess(regularUser);
            return;
        }
        
        // For demo purposes, accept any valid email with password >= 6 characters
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length() >= 6) {
            String role = isAdminEmail(email) ? "Admin" : "User";
            User demoUser = new User("demo_" + System.currentTimeMillis(), "Demo User", email, role);
            saveUserToPreferences(demoUser);
            callback.onSuccess(demoUser);
        } else {
            callback.onError("Invalid email format or password too short (minimum 6 characters)");
        }
    }
    
    /**
     * Register new user with local authentication
     */
    public void register(String fullName, String email, String password, String role, AuthCallback callback) {
        Log.d(TAG, "Attempting registration for: " + email);
        
        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            callback.onError("All fields are required");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError("Invalid email format");
            return;
        }
        
        if (password.length() < 6) {
            callback.onError("Password must be at least 6 characters");
            return;
        }
        
        // Override role if admin email
        if (isAdminEmail(email)) {
            role = "Admin";
        }
        
        // Create new user
        User newUser = new User("user_" + System.currentTimeMillis(), fullName, email, role);
        saveUserToPreferences(newUser);
        callback.onSuccess(newUser);
    }
    
    /**
     * Save user data to local preferences
     */
    private void saveUserToPreferences(User user) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ROLE, user.getRole())
                .putString(KEY_USER_NAME, user.getFullName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .apply();
        
        Log.d(TAG, "User saved to preferences: " + user.getFullName() + " (" + user.getRole() + ")");
    }
    
    /**
     * Check if user is currently logged in
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get current user role from preferences
     */
    public String getCurrentUserRole() {
        return preferences.getString(KEY_USER_ROLE, "User");
    }
    
    /**
     * Get current user name from preferences
     */
    public String getCurrentUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }
    
    /**
     * Get current user email from preferences
     */
    public String getCurrentUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        Log.d(TAG, "Logging out user");
        preferences.edit().clear().apply();
    }
    
    /**
     * Check if email is admin email (hardcoded as per requirements)
     */
    private boolean isAdminEmail(String email) {
        return ADMIN_EMAIL.equalsIgnoreCase(email);
    }
}