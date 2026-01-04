package com.uni.crimes.network;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uni.crimes.model.Crime;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for handling network operations with remote MySQL database
 * Provides abstraction layer between ViewModels and API calls
 */
public class NetworkRepository {
    
    private static final String TAG = "NetworkRepository";
    private ApiService apiService;
    
    public interface NetworkCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
    
    public NetworkRepository() {
        this.apiService = NetworkModule.getApiService();
    }
    
    /**
     * Fetch all crimes from remote MySQL database
     */
    public void getAllCrimes(NetworkCallback<List<Crime>> callback) {
        Call<List<Crime>> call = apiService.getAllCrimes();
        call.enqueue(new Callback<List<Crime>>() {
            @Override
            public void onResponse(Call<List<Crime>> call, Response<List<Crime>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched " + response.body().size() + " crimes from MySQL");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to fetch crimes: " + response.code());
                    callback.onError("Failed to fetch crimes: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Crime>> call, Throwable t) {
                Log.e(TAG, "Network error fetching crimes", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Search crimes in remote MySQL database
     */
    public void searchCrimes(String searchTerm, NetworkCallback<List<Crime>> callback) {
        Call<List<Crime>> call = apiService.searchCrimes(searchTerm);
        call.enqueue(new Callback<List<Crime>>() {
            @Override
            public void onResponse(Call<List<Crime>> call, Response<List<Crime>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Search found " + response.body().size() + " crimes in MySQL");
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Search failed: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Crime>> call, Throwable t) {
                Log.e(TAG, "Network error during search", t);
                callback.onError("Search error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get crime by ID from remote MySQL database
     */
    public void getCrimeById(String crimeId, NetworkCallback<Crime> callback) {
        Call<Crime> call = apiService.getCrimeById(crimeId);
        call.enqueue(new Callback<Crime>() {
            @Override
            public void onResponse(Call<Crime> call, Response<Crime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched crime: " + crimeId);
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Crime not found: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Crime> call, Throwable t) {
                Log.e(TAG, "Network error fetching crime", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Create new crime in remote MySQL database (Admin only)
     */
    public void createCrime(Crime crime, String authToken, NetworkCallback<Crime> callback) {
        Call<Crime> call = apiService.createCrime(crime, "Bearer " + authToken);
        call.enqueue(new Callback<Crime>() {
            @Override
            public void onResponse(Call<Crime> call, Response<Crime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully created crime in MySQL");
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create crime: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Crime> call, Throwable t) {
                Log.e(TAG, "Network error creating crime", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Update crime in remote MySQL database (Admin only)
     */
    public void updateCrime(String crimeId, Crime crime, String authToken, NetworkCallback<Crime> callback) {
        Call<Crime> call = apiService.updateCrime(crimeId, crime, "Bearer " + authToken);
        call.enqueue(new Callback<Crime>() {
            @Override
            public void onResponse(Call<Crime> call, Response<Crime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully updated crime in MySQL");
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update crime: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Crime> call, Throwable t) {
                Log.e(TAG, "Network error updating crime", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Delete crime from remote MySQL database (Admin only)
     */
    public void deleteCrime(String crimeId, String authToken, NetworkCallback<Void> callback) {
        Call<Void> call = apiService.deleteCrime(crimeId, "Bearer " + authToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully deleted crime from MySQL");
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete crime: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error deleting crime", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}