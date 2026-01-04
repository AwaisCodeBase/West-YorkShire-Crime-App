package com.uni.crimes.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.database.CrimeDao;
import com.uni.crimes.model.Crime;
import com.uni.crimes.utils.CSVImporter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class for Crime data operations
 * Abstracts data sources and provides clean API for ViewModels
 * Part of MVVM architecture pattern
 */
public class CrimeRepository {
    
    private static final String TAG = "CrimeRepository";
    
    private CrimeDao crimeDao;
    private LiveData<List<Crime>> allCrimes;
    private ExecutorService executor;
    private Context context;
    
    public interface SearchCallback {
        void onSuccess(List<Crime> crimes);
        void onError(String error);
    }
    
    public interface ImportCallback {
        void onSuccess(int importedCount);
        void onError(String error);
        void onProgress(int progress);
    }
    
    public CrimeRepository(Application application) {
        CrimeDatabase database = CrimeDatabase.getInstance(application);
        crimeDao = database.crimeDao();
        allCrimes = crimeDao.getAllCrimes();
        executor = Executors.newFixedThreadPool(4);
        context = application.getApplicationContext();
    }
    
    // LiveData operations (automatically observed by UI)
    public LiveData<List<Crime>> getAllCrimes() {
        return allCrimes;
    }
    
    public LiveData<Crime> getCrimeById(String crimeId) {
        return crimeDao.getCrimeByIdLive(crimeId);
    }
    
    public LiveData<Integer> getCrimeCount() {
        return crimeDao.getCrimeCountLive();
    }
    
    // CRUD operations
    public void insertCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.insertCrime(crime);
                Log.d(TAG, "Crime inserted: " + crime.getCrimeId());
            } catch (Exception e) {
                Log.e(TAG, "Error inserting crime: " + crime.getCrimeId(), e);
                // In a real app, you might want to notify the UI about this error
            }
        });
    }

    public void insertCrimes(List<Crime> crimes) {
        executor.execute(() -> {
            try {
                crimeDao.insertAllCrimes(crimes);
                Log.d(TAG, "Crimes inserted: " + crimes.size());
            } catch (Exception e) {
                Log.e(TAG, "Error inserting crimes", e);
            }
        });
    }
    
    public void updateCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.updateCrime(crime);
                Log.d(TAG, "Crime updated: " + crime.getCrimeId());
            } catch (Exception e) {
                Log.e(TAG, "Error updating crime: " + crime.getCrimeId(), e);
            }
        });
    }
    
    public void deleteCrime(Crime crime) {
        executor.execute(() -> {
            try {
                crimeDao.deleteCrime(crime);
                Log.d(TAG, "Crime deleted: " + crime.getCrimeId());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting crime: " + crime.getCrimeId(), e);
            }
        });
    }
    
    // Search operations
    public void searchCrimesByAnyField(String searchTerm, SearchCallback callback) {
        executor.execute(() -> {
            try {
                List<Crime> results = crimeDao.searchCrimesByAnyFieldSync(searchTerm);
                callback.onSuccess(results);
                Log.d(TAG, "Search completed. Found " + results.size() + " results for: " + searchTerm);
            } catch (Exception e) {
                Log.e(TAG, "Error searching crimes", e);
                callback.onError("Search failed: " + e.getMessage());
            }
        });
    }
    
    public void searchByField(String field, String value, SearchCallback callback) {
        executor.execute(() -> {
            try {
                // Convert field name for query
                String queryField = convertFieldName(field);
                List<Crime> results;
                
                switch (queryField) {
                    case "crimeType":
                        results = crimeDao.searchCrimesByAnyFieldSync(value); // Use any field search for simplicity
                        break;
                    case "lsoaName":
                        results = crimeDao.searchCrimesByAnyFieldSync(value);
                        break;
                    case "outcomeCategory":
                        results = crimeDao.searchCrimesByAnyFieldSync(value);
                        break;
                    default:
                        results = crimeDao.searchCrimesByAnyFieldSync(value);
                }
                
                callback.onSuccess(results);
                Log.d(TAG, "Field search completed. Found " + results.size() + " results");
            } catch (Exception e) {
                Log.e(TAG, "Error searching by field", e);
                callback.onError("Search failed: " + e.getMessage());
            }
        });
    }
    
    // CSV Import operation (Admin only)
    public void importCrimesFromCSV(ImportCallback callback) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Starting CSV import...");
                
                // Use CSVImporter utility class
                CSVImporter importer = new CSVImporter(context, crimeDao);
                importer.importFromAssets("crimeyorkshire.csv", new CSVImporter.ImportListener() {
                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(progress);
                    }
                    
                    @Override
                    public void onSuccess(int importedCount) {
                        Log.d(TAG, "CSV import completed. Imported " + importedCount + " records");
                        callback.onSuccess(importedCount);
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
    
    // Utility methods
    private String convertFieldName(String displayName) {
        switch (displayName) {
            case "Crime Type": return "crimeType";
            case "LSOA Name": return "lsoaName";
            case "Outcome Category": return "outcomeCategory";
            case "Reported By": return "reportedBy";
            default: return "crimeType";
        }
    }
    
    // Get synchronous data (for non-UI operations)
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