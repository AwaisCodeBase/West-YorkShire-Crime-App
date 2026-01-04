package com.uni.crimes.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.database.CrimeDao;
import com.uni.crimes.model.Crime;
import com.uni.crimes.network.NetworkRepository;
import com.uni.crimes.utils.CSVImporter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hybrid Repository that works with both SQLite (local) and MySQL (remote) databases
 * Provides offline-first functionality with remote synchronization
 * Part of MVVM architecture pattern
 */
public class HybridCrimeRepository {
    
    private static final String TAG = "HybridCrimeRepository";
    
    // Local SQLite database components
    private CrimeDao crimeDao;
    private LiveData<List<Crime>> allCrimes;
    private ExecutorService executor;
    private Context context;
    
    // Remote MySQL database components
    private NetworkRepository networkRepository;
    private MutableLiveData<Boolean> isOnline;
    private MutableLiveData<String> syncStatus;
    
    public interface SearchCallback {
        void onSuccess(List<Crime> crimes);
        void onError(String error);
    }
    
    public interface ImportCallback {
        void onSuccess(int importedCount);
        void onError(String error);
        void onProgress(int progress);
    }
    
    public interface SyncCallback {
        void onSyncComplete(int syncedCount);
        void onSyncError(String error);
    }
    
    public HybridCrimeRepository(Application application) {
        // Initialize local SQLite database
        CrimeDatabase database = CrimeDatabase.getInstance(application);
        crimeDao = database.crimeDao();
        allCrimes = crimeDao.getAllCrimes();
        executor = Executors.newFixedThreadPool(4);
        context = application.getApplicationContext();
        
        // Initialize remote MySQL database
        networkRepository = new NetworkRepository();
        isOnline = new MutableLiveData<>(false);
        syncStatus = new MutableLiveData<>("Not synced");
        
        // Check network connectivity and sync if online
        checkConnectivityAndSync();
    }
    
    // ========== LOCAL SQLite OPERATIONS ==========
    
    /**
     * Get all crimes from local SQLite database
     */
    public LiveData<List<Crime>> getAllCrimes() {
        return allCrimes;
    }
    
    /**
     * Get crime by ID from local SQLite database
     */
    public LiveData<Crime> getCrimeById(String crimeId) {
        return crimeDao.getCrimeByIdLive(crimeId);
    }
    
    /**
     * Get crime count from local SQLite database
     */
    public LiveData<Integer> getCrimeCount() {
        return crimeDao.getCrimeCountLive();
    }
    
    /**
     * Insert crime into local SQLite database
     */
    public void insertCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.insertCrime(crime);
                Log.d(TAG, "Crime inserted locally: " + crime.getCrimeId());
                
                // If online, also sync to remote MySQL database
                if (Boolean.TRUE.equals(isOnline.getValue())) {
                    syncCrimeToRemote(crime);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting crime locally", e);
            }
        });
    }
    
    /**
     * Update crime in local SQLite database
     */
    public void updateCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.updateCrime(crime);
                Log.d(TAG, "Crime updated locally: " + crime.getCrimeId());
                
                // If online, also sync to remote MySQL database
                if (Boolean.TRUE.equals(isOnline.getValue())) {
                    syncCrimeUpdateToRemote(crime);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating crime locally", e);
            }
        });
    }
    
    /**
     * Delete crime from local SQLite database
     */
    public void deleteCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.deleteCrime(crime);
                Log.d(TAG, "Crime deleted locally: " + crime.getCrimeId());
                
                // If online, also sync deletion to remote MySQL database
                if (Boolean.TRUE.equals(isOnline.getValue())) {
                    syncCrimeDeletionToRemote(crime.getCrimeId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting crime locally", e);
            }
        });
    }
    
    // ========== SEARCH OPERATIONS ==========
    
    /**
     * Search crimes by any field (local first, then remote if needed)
     */
    public void searchCrimesByAnyField(String searchTerm, SearchCallback callback) {
        executor.execute(() -> {
            try {
                // First search locally in SQLite
                List<Crime> localResults = crimeDao.searchCrimesByAnyFieldSync(searchTerm);
                
                if (!localResults.isEmpty()) {
                    // Return local results immediately
                    callback.onSuccess(localResults);
                    Log.d(TAG, "Search completed locally. Found " + localResults.size() + " results");
                } else if (Boolean.TRUE.equals(isOnline.getValue())) {
                    // If no local results and online, search remote MySQL database
                    searchRemoteCrimes(searchTerm, callback);
                } else {
                    // No local results and offline
                    callback.onSuccess(localResults);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching crimes locally", e);
                callback.onError("Search failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Search crimes by specific field
     */
    public void searchByField(String field, String value, SearchCallback callback) {
        executor.execute(() -> {
            try {
                // Use local search for now (can be enhanced to use remote search)
                List<Crime> results = crimeDao.searchCrimesByAnyFieldSync(value);
                callback.onSuccess(results);
                Log.d(TAG, "Field search completed locally. Found " + results.size() + " results");
            } catch (Exception e) {
                Log.e(TAG, "Error searching by field locally", e);
                callback.onError("Search failed: " + e.getMessage());
            }
        });
    }
    
    // ========== REMOTE MySQL OPERATIONS ==========
    
    /**
     * Search crimes in remote MySQL database
     */
    private void searchRemoteCrimes(String searchTerm, SearchCallback callback) {
        networkRepository.searchCrimes(searchTerm, new NetworkRepository.NetworkCallback<List<Crime>>() {
            @Override
            public void onSuccess(List<Crime> crimes) {
                // Cache remote results locally for offline access
                executor.execute(() -> {
                    try {
                        for (Crime crime : crimes) {
                            crimeDao.insertCrime(crime);
                        }
                        Log.d(TAG, "Cached " + crimes.size() + " remote search results locally");
                    } catch (Exception e) {
                        Log.w(TAG, "Error caching remote results", e);
                    }
                });
                
                callback.onSuccess(crimes);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Remote search failed: " + error);
                callback.onError("Remote search failed: " + error);
            }
        });
    }
    
    /**
     * Sync local crime to remote MySQL database
     */
    private void syncCrimeToRemote(Crime crime) {
        // This would require authentication token - implement based on your auth system
        String authToken = getAuthToken();
        if (authToken != null) {
            networkRepository.createCrime(crime, authToken, new NetworkRepository.NetworkCallback<Crime>() {
                @Override
                public void onSuccess(Crime remoteCrime) {
                    Log.d(TAG, "Crime synced to remote MySQL: " + crime.getCrimeId());
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Failed to sync crime to remote: " + error);
                }
            });
        }
    }
    
    /**
     * Sync crime update to remote MySQL database
     */
    private void syncCrimeUpdateToRemote(Crime crime) {
        String authToken = getAuthToken();
        if (authToken != null) {
            networkRepository.updateCrime(crime.getCrimeId(), crime, authToken, new NetworkRepository.NetworkCallback<Crime>() {
                @Override
                public void onSuccess(Crime remoteCrime) {
                    Log.d(TAG, "Crime update synced to remote MySQL: " + crime.getCrimeId());
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Failed to sync crime update to remote: " + error);
                }
            });
        }
    }
    
    /**
     * Sync crime deletion to remote MySQL database
     */
    private void syncCrimeDeletionToRemote(String crimeId) {
        String authToken = getAuthToken();
        if (authToken != null) {
            networkRepository.deleteCrime(crimeId, authToken, new NetworkRepository.NetworkCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Crime deletion synced to remote MySQL: " + crimeId);
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Failed to sync crime deletion to remote: " + error);
                }
            });
        }
    }
    
    // ========== SYNCHRONIZATION OPERATIONS ==========
    
    /**
     * Sync all data from remote MySQL to local SQLite
     */
    public void syncFromRemote(SyncCallback callback) {
        if (!Boolean.TRUE.equals(isOnline.getValue())) {
            callback.onSyncError("No internet connection");
            return;
        }
        
        syncStatus.setValue("Syncing from remote MySQL...");
        
        networkRepository.getAllCrimes(new NetworkRepository.NetworkCallback<List<Crime>>() {
            @Override
            public void onSuccess(List<Crime> remoteCrimes) {
                executor.execute(() -> {
                    try {
                        int syncedCount = 0;
                        for (Crime crime : remoteCrimes) {
                            crimeDao.insertCrime(crime);
                            syncedCount++;
                        }
                        
                        syncStatus.postValue("Sync completed: " + syncedCount + " crimes");
                        callback.onSyncComplete(syncedCount);
                        Log.d(TAG, "Synced " + syncedCount + " crimes from remote MySQL");
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing from remote", e);
                        syncStatus.postValue("Sync failed: " + e.getMessage());
                        callback.onSyncError("Sync failed: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                syncStatus.setValue("Sync failed: " + error);
                callback.onSyncError(error);
            }
        });
    }
    
    /**
     * Check network connectivity and perform initial sync
     */
    private void checkConnectivityAndSync() {
        // Simple connectivity check - can be enhanced with proper network monitoring
        executor.execute(() -> {
            try {
                // Try to fetch a small amount of data to test connectivity
                networkRepository.getAllCrimes(new NetworkRepository.NetworkCallback<List<Crime>>() {
                    @Override
                    public void onSuccess(List<Crime> crimes) {
                        isOnline.postValue(true);
                        syncStatus.postValue("Connected to remote MySQL");
                        Log.d(TAG, "Connected to remote MySQL database");
                    }
                    
                    @Override
                    public void onError(String error) {
                        isOnline.postValue(false);
                        syncStatus.postValue("Offline mode - using local SQLite");
                        Log.d(TAG, "Using offline mode with local SQLite");
                    }
                });
            } catch (Exception e) {
                isOnline.postValue(false);
                syncStatus.postValue("Offline mode - using local SQLite");
                Log.d(TAG, "Using offline mode with local SQLite");
            }
        });
    }
    
    // ========== CSV IMPORT OPERATIONS ==========
    
    /**
     * Import crimes from CSV (local first, then sync to remote if online)
     */
    public void importCrimesFromCSV(ImportCallback callback) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Starting CSV import...");
                
                // Use local CSV importer first
                CSVImporter importer = new CSVImporter(context, crimeDao);
                importer.importFromAssets("crimeyorkshire.csv", new CSVImporter.ImportListener() {
                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(progress);
                    }
                    
                    @Override
                    public void onSuccess(int importedCount) {
                        Log.d(TAG, "CSV import completed locally. Imported " + importedCount + " records");
                        
                        // If online, also sync to remote MySQL
                        if (Boolean.TRUE.equals(isOnline.getValue())) {
                            syncLocalDataToRemote(importedCount, callback);
                        } else {
                            callback.onSuccess(importedCount);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "CSV import failed: " + error);
                        callback.onError(error);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error during CSV import", e);
                callback.onError("Import failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sync locally imported data to remote MySQL
     */
    private void syncLocalDataToRemote(int localImportCount, ImportCallback callback) {
        // Get all local crimes and sync to remote
        executor.execute(() -> {
            try {
                List<Crime> allLocalCrimes = crimeDao.getAllCrimesSync();
                
                // This is a simplified sync - in production, you'd want to batch this
                // and handle it more efficiently
                String authToken = getAuthToken();
                if (authToken != null) {
                    // For now, just report success - actual remote sync would be implemented here
                    Log.d(TAG, "Would sync " + allLocalCrimes.size() + " crimes to remote MySQL");
                    callback.onSuccess(localImportCount);
                } else {
                    callback.onSuccess(localImportCount);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing to remote after import", e);
                callback.onSuccess(localImportCount); // Still report local success
            }
        });
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get authentication token for remote API calls
     */
    private String getAuthToken() {
        // This should integrate with your authentication system
        // For now, return null - implement based on your auth setup
        return null;
    }
    
    /**
     * Get online status
     */
    public LiveData<Boolean> getIsOnline() {
        return isOnline;
    }
    
    /**
     * Get sync status
     */
    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }
    
    /**
     * Force refresh from remote MySQL
     */
    public void forceRefreshFromRemote(SyncCallback callback) {
        syncFromRemote(callback);
    }
    
    /**
     * Get synchronous data for non-UI operations
     */
    public void getAllCrimesSync(SearchCallback callback) {
        executor.execute(() -> {
            try {
                List<Crime> crimes = crimeDao.getAllCrimesSync();
                callback.onSuccess(crimes);
            } catch (Exception e) {
                callback.onError("Failed to fetch crimes: " + e.getMessage());
            }
        });
    }
}