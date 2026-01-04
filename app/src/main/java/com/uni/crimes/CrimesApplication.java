package com.uni.crimes;

import android.app.Application;
import android.util.Log;

/**
 * Application class for initializing app-wide components
 * Mapbox Maps SDK v11+ initializes automatically via the access token in resources
 */
public class CrimesApplication extends Application {
    
    private static final String TAG = "CrimesApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate() called - Initializing app components");
        // Mapbox Maps SDK v11+ auto-initializes using R.string.mapbox_access_token
        Log.d(TAG, "Application initialized successfully");
    }
}