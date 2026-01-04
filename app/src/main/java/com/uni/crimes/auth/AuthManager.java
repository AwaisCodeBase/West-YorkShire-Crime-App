package com.uni.crimes.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.uni.crimes.model.User;

/**
 * Authentication Manager with Firebase fallback to local authentication
 * Handles login, registration, and user session management
 * Falls back to local auth if Firebase is not configured properly
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private static final String PREFS_NAME = "CrimesAppPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // Admin email hardcoded as per coursework requirements
    private static final String ADMIN_EMAIL = "admin@crimes.com";
    private static final String ADMIN_PASSWORD = "admin123";
    
    private SharedPreferences prefs;
    private boolean useLocalAuth = true; // Set to true for demo purposes
    
    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }
    
    /**
     * Login user with local authentication (Firebase fallback)
     */
    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting login for: " + email);
        
        if (useLocalAuth) {
            performLocalLogin(email, password, callback);
        } else {
            // Firebase authentication would go here
            performLocalLogin(email, password, callback); // Fallback
        }
    }
    
    /**
     * Register new user with local authentication (Firebase fallback)
     */
    public void register(String fullName, String email, String password, String role, AuthCallback callback) {
        Log.d(TAG, "Attempting registration for: " + email);
        
        if (useLocalAuth) {
            performLocalRegistration(fullName, email, password, role, callback);
        } else {
            // Firebase authentication would go here
            performLocalRegistration(fullName, email, password, role, callback); // Fallback
        }
    }
    
    /**
     * Local authentication implementation
     */
    private void performLocalLogin(String email, String password, AuthCallback callback) {
        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError("Email and password are required");
            return;
        }
        
        // Check for admin credentials
        if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
            User adminUser = new User("admin_uid", "Admin User", email, "Admin");
            saveUserToPrefs(adminUser);
            callback.onSuccess(adminUser);
            return;
        }
        
        // Check for demo user credentials
        if ("user@crimes.com".equalsIgnoreCase(email) && "user123".equals(password)) {
            User regularUser = new User("user_uid", "Regular User", email, "User");
            saveUserToPrefs(regularUser);
            callback.onSuccess(regularUser);
            return;
        }
        
        // For demo purposes, accept any valid email with password >= 6 characters
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length() >= 6) {
            String role = isAdminEmail(email) ? "Admin" : "User";
            User demoUser = new User("demo_" + System.currentTimeMillis(), "Demo User", email, role);
            saveUserToPrefs(demoUser);
            callback.onSuccess(demoUser);
        } else {
            callback.onError("Invalid email format or password too short (minimum 6 characters)");
        }
    }
    
    /**
     * Local registration implementation
     */
    private void performLocalRegistration(String fullName, String email, String password, String role, AuthCallback callback) {
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
        saveUserToPrefs(newUser);
        callback.onSuccess(newUser);
    }
    
    /**
     * Save user data to SharedPreferences
     */
    private void saveUserToPrefs(User user) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ROLE, user.getRole())
                .putString(KEY_USER_NAME, user.getFullName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .apply();
        
        Log.d(TAG, "User saved to preferences: " + user.getFullName() + " (" + user.getRole() + ")");
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get current user role
     */
    public String getCurrentUserRole() {
        return prefs.getString(KEY_USER_ROLE, "User");
    }
    
    /**
     * Get current user name
     */
    public String getCurrentUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    
    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        Log.d(TAG, "Logging out user");
        prefs.edit().clear().apply();
    }
    
    /**
     * Clear all authentication data (for testing/debugging)
     */
    public void clearAllData() {
        Log.d(TAG, "Clearing all authentication data");
        prefs.edit().clear().apply();
    }
    
    /**
     * Check if email is admin email (hardcoded as per requirements)
     */
    private boolean isAdminEmail(String email) {
        return ADMIN_EMAIL.equalsIgnoreCase(email);
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isCurrentUserAdmin() {
        return "Admin".equals(getCurrentUserRole());
    }
}