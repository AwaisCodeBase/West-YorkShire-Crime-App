package com.uni.crimes.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uni.crimes.model.User;
import com.uni.crimes.repository.AuthRepository;

/**
 * ViewModel for Authentication operations following MVVM architecture
 * Manages authentication state and user data
 */
public class AuthViewModel extends AndroidViewModel {
    
    private AuthRepository repository;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<String> authError;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> isLoggedIn;
    
    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
        currentUser = new MutableLiveData<>();
        authError = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        isLoggedIn = new MutableLiveData<>(repository.isLoggedIn());
    }
    
    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<String> getAuthError() {
        return authError;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
    
    // Authentication operations
    public void login(String email, String password) {
        isLoading.setValue(true);
        repository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
                isLoggedIn.setValue(true);
                isLoading.setValue(false);
                authError.setValue(null);
            }
            
            @Override
            public void onError(String error) {
                authError.setValue(error);
                isLoading.setValue(false);
            }
        });
    }
    
    public void register(String fullName, String email, String password, String role) {
        isLoading.setValue(true);
        repository.register(fullName, email, password, role, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
                isLoggedIn.setValue(true);
                isLoading.setValue(false);
                authError.setValue(null);
            }
            
            @Override
            public void onError(String error) {
                authError.setValue(error);
                isLoading.setValue(false);
            }
        });
    }
    
    public void logout() {
        repository.logout();
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }
    
    // User info getters
    public String getCurrentUserRole() {
        return repository.getCurrentUserRole();
    }
    
    public String getCurrentUserName() {
        return repository.getCurrentUserName();
    }
    
    public boolean isAdmin() {
        return "Admin".equals(getCurrentUserRole());
    }
    
    // Clear error message
    public void clearAuthError() {
        authError.setValue(null);
    }
}