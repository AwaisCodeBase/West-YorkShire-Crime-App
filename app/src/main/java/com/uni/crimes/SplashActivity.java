package com.uni.crimes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.uni.crimes.auth.AuthManager;

/**
 * Splash Screen Activity - Initial Setup and Authentication Check
 * Performs initial setup tasks and checks authentication state
 * 
 * Functional Description:
 * - Displayed while system performs initial setup tasks
 * - Initializes SQLite database connection
 * - Checks user's authentication state using AuthManager
 * - If valid login session exists, redirects to Home Screen
 * - Otherwise, navigates to Login Screen
 * - Improves user experience by reducing unnecessary login prompts
 * - Demonstrates Android activity lifecycle usage
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIME_OUT = 3000; // 3 seconds as per coursework requirements

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - Activity lifecycle method");
        
        try {
            setContentView(R.layout.activity_splash);
            
            // Initialize AuthManager for authentication state checking
            authManager = new AuthManager(this);
            
            // Perform initial setup and authentication check after splash delay
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Splash timeout completed, performing authentication check");
                    checkAuthenticationAndNavigate();
                }
            }, SPLASH_TIME_OUT);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            // Fallback to login screen on error
            navigateToLogin();
        }
    }

    /**
     * Check authentication state and navigate accordingly
     * Implements persistent login session functionality
     */
    private void checkAuthenticationAndNavigate() {
        try {
            // TEMPORARY: Force logout for testing - uncomment to always show login screen
            
            if (authManager.isLoggedIn()) {
                // Valid login session exists - redirect to Home Screen
                String userName = authManager.getCurrentUserName();
                String userRole = authManager.getCurrentUserRole();
                Log.d(TAG, "Valid session found for user: " + userName + " (Role: " + userRole + ") - navigating to Home Screen");
                
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // No valid session - navigate to Login Screen
                Log.d(TAG, "No valid session found - navigating to Login Screen");
                navigateToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during authentication check", e);
            navigateToLogin();
        }
    }

    /**
     * Navigate to Login Screen
     */
    private void navigateToLogin() {
        try {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity", e);
        }
    }

    // Activity lifecycle methods with explicit logging as per coursework requirements

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called - Activity lifecycle method");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called - Activity lifecycle method");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called - Activity lifecycle method");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called - Activity lifecycle method");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Activity lifecycle method");
    }
}