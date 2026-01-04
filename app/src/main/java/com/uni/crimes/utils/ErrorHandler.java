package com.uni.crimes.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Centralized error handling utility
 * Provides consistent error logging and user feedback
 */
public class ErrorHandler {
    
    private static final String TAG = "ErrorHandler";
    
    /**
     * Handle database errors with user feedback
     */
    public static void handleDatabaseError(Context context, String operation, Exception e) {
        String message = "Database error during " + operation;
        Log.e(TAG, message, e);
        
        if (context != null) {
            Toast.makeText(context, "Error: " + operation + " failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle network errors with user feedback
     */
    public static void handleNetworkError(Context context, String operation, Exception e) {
        String message = "Network error during " + operation;
        Log.e(TAG, message, e);
        
        if (context != null) {
            Toast.makeText(context, "Network error: Please check your connection", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle authentication errors
     */
    public static void handleAuthError(Context context, String error) {
        Log.e(TAG, "Authentication error: " + error);
        
        if (context != null) {
            Toast.makeText(context, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle validation errors
     */
    public static void handleValidationError(Context context, String field, String error) {
        Log.w(TAG, "Validation error for " + field + ": " + error);
        
        if (context != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Log info message
     */
    public static void logInfo(String tag, String message) {
        Log.i(tag, message);
    }
    
    /**
     * Log debug message
     */
    public static void logDebug(String tag, String message) {
        Log.d(tag, message);
    }
}