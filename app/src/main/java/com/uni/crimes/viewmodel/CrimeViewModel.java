package com.uni.crimes.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.database.CrimeDao;
import com.uni.crimes.model.Crime;
import com.uni.crimes.repository.CrimeRepository;

import java.util.List;

/**
 * ViewModel for Crime-related operations following MVVM architecture
 * Manages UI-related data and survives configuration changes
 */
public class CrimeViewModel extends AndroidViewModel {
    
    private CrimeRepository repository;
    private LiveData<List<Crime>> allCrimes;
    private MutableLiveData<List<Crime>> searchResults;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    
    public CrimeViewModel(@NonNull Application application) {
        super(application);
        repository = new CrimeRepository(application);
        allCrimes = repository.getAllCrimes();
        searchResults = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }
    
    // Getters for LiveData (observed by UI)
    public LiveData<List<Crime>> getAllCrimes() {
        return allCrimes;
    }
    
    public LiveData<List<Crime>> getSearchResults() {
        return searchResults;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    // Crime operations
    public void insertCrime(Crime crime) {
        repository.insertCrime(crime);
    }

    public void insertCrimes(List<Crime> crimes) {
        repository.insertCrimes(crimes);
    }
    
    public void updateCrime(Crime crime) {
        repository.updateCrime(crime);
    }
    
    public void deleteCrime(Crime crime) {
        repository.deleteCrime(crime);
    }
    
    public LiveData<Crime> getCrimeById(String crimeId) {
        return repository.getCrimeById(crimeId);
    }
    
    // Search operations
    public void searchCrimes(String searchTerm) {
        isLoading.postValue(true);
        repository.searchCrimesByAnyField(searchTerm, new CrimeRepository.SearchCallback() {
            @Override
            public void onSuccess(List<Crime> crimes) {
                searchResults.postValue(crimes);
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
    
    public void searchByField(String field, String value) {
        isLoading.postValue(true);
        repository.searchByField(field, value, new CrimeRepository.SearchCallback() {
            @Override
            public void onSuccess(List<Crime> crimes) {
                searchResults.postValue(crimes);
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
    
    // Admin operations
    public void importCrimesFromCSV(CrimeRepository.ImportCallback callback) {
        isLoading.postValue(true);
        repository.importCrimesFromCSV(callback);
    }
    
    public LiveData<Integer> getCrimeCount() {
        return repository.getCrimeCount();
    }
    
    // Clear search results
    public void clearSearchResults() {
        searchResults.postValue(null);
    }
    
    // Clear error message
    public void clearErrorMessage() {
        errorMessage.postValue(null);
    }
}