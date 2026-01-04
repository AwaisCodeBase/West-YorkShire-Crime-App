package com.uni.crimes.config;

/**
 * Configuration class for database settings
 * Manages both SQLite (local) and MySQL (remote) database configurations
 */
public class DatabaseConfig {
    
    // ========== LOCAL SQLite CONFIGURATION ==========
    public static final String LOCAL_DB_NAME = "crime_database";
    public static final int LOCAL_DB_VERSION = 1;
    
    // ========== REMOTE MySQL CONFIGURATION ==========
    
    // Backend server configuration
    public static final String REMOTE_BASE_URL = "https://your-backend-server.com/api/";
    
    // Alternative configurations for different environments
    public static final String DEV_BASE_URL = "http://localhost:8080/api/";
    public static final String STAGING_BASE_URL = "https://staging.your-backend-server.com/api/";
    public static final String PROD_BASE_URL = "https://api.your-backend-server.com/api/";
    
    // Network timeouts (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // ========== DATABASE OPERATION MODES ==========
    
    public enum DatabaseMode {
        LOCAL_ONLY,     // Use only SQLite (offline mode)
        REMOTE_ONLY,    // Use only MySQL via API (online mode)
        HYBRID          // Use both with sync (recommended)
    }
    
    // Current database mode - can be changed based on user preference or connectivity
    public static DatabaseMode CURRENT_MODE = DatabaseMode.HYBRID;
    
    // ========== SYNC CONFIGURATION ==========
    
    // Auto-sync settings
    public static final boolean AUTO_SYNC_ENABLED = true;
    public static final long SYNC_INTERVAL_MINUTES = 30; // Sync every 30 minutes
    
    // Batch sizes for sync operations
    public static final int SYNC_BATCH_SIZE = 100;
    public static final int IMPORT_BATCH_SIZE = 50;
    
    // ========== API ENDPOINTS ==========
    
    public static final String ENDPOINT_CRIMES = "crimes";
    public static final String ENDPOINT_SEARCH = "crimes/search";
    public static final String ENDPOINT_AUTH_LOGIN = "auth/login";
    public static final String ENDPOINT_AUTH_REGISTER = "auth/register";
    public static final String ENDPOINT_IMPORT = "crimes/import";
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get the appropriate base URL based on build type
     */
    public static String getBaseUrl() {
        // In a real app, this would check BuildConfig.DEBUG or other build variants
        return REMOTE_BASE_URL;
    }
    
    /**
     * Check if remote database operations are enabled
     */
    public static boolean isRemoteEnabled() {
        return CURRENT_MODE == DatabaseMode.REMOTE_ONLY || CURRENT_MODE == DatabaseMode.HYBRID;
    }
    
    /**
     * Check if local database operations are enabled
     */
    public static boolean isLocalEnabled() {
        return CURRENT_MODE == DatabaseMode.LOCAL_ONLY || CURRENT_MODE == DatabaseMode.HYBRID;
    }
    
    /**
     * Check if sync operations are enabled
     */
    public static boolean isSyncEnabled() {
        return CURRENT_MODE == DatabaseMode.HYBRID && AUTO_SYNC_ENABLED;
    }
}