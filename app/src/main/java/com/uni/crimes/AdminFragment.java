package com.uni.crimes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.uni.crimes.auth.AuthManager;
import com.uni.crimes.viewmodel.CrimeViewModel;
import com.uni.crimes.model.Crime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard Fragment - Role-Based Access Control
 * Accessible only to users with administrative privileges
 * Implements MVVM pattern with proper lifecycle logging
 * 
 * Functional Description:
 * - Accessible only to Admin users (role-based access control)
 * - Provides controls for managing crime dataset and user operations
 * - Admin-only functionalities: import data, add/update/delete records
 * - Role-based access enforced at both UI and logic levels for security
 * - Demonstrates proper separation of admin and user functionalities
 */
public class AdminFragment extends Fragment {

    private static final String TAG = "AdminFragment";
    
    private TextView tvAdminInfo, tvCrimeCount, tvSyncStatus;
    private MaterialButton btnImportDataset, btnAddCrime, btnManageCrimes, btnSyncData;
    
    private AuthManager authManager;
    private CrimeViewModel crimeViewModel;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        readCsvFile(uri);
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openFilePicker();
                } else {
                    Toast.makeText(requireContext(), "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - Fragment lifecycle method");
        
        authManager = new AuthManager(requireContext());
        crimeViewModel = new ViewModelProvider(this).get(CrimeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called - Fragment lifecycle method");
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called - Setting up Admin Dashboard with role-based access control");
        
        // Verify admin access before setting up UI
        if (!isAdminUser()) {
            Log.w(TAG, "Non-admin user attempting to access Admin Dashboard");
            // In a real app, you might want to redirect or show error
            return;
        }
        
        initViews(view);
        setupAdminInfo();
        setupListeners();
        observeViewModel();
    }

    /**
     * Verify if current user has admin privileges
     * Implements role-based access control as per coursework requirements
     */
    private boolean isAdminUser() {
        String userRole = authManager.getCurrentUserRole();
        boolean isAdmin = "Admin".equals(userRole);
        Log.d(TAG, "User role verification - Role: " + userRole + ", Is Admin: " + isAdmin);
        return isAdmin;
    }

    private void initViews(View view) {
        tvAdminInfo = view.findViewById(R.id.tv_admin_info);
        tvCrimeCount = view.findViewById(R.id.tv_crime_count);
        tvSyncStatus = view.findViewById(R.id.tv_sync_status);
        btnImportDataset = view.findViewById(R.id.btn_import_dataset);
        btnAddCrime = view.findViewById(R.id.btn_add_crime);
        btnManageCrimes = view.findViewById(R.id.btn_manage_crimes);
        btnSyncData = view.findViewById(R.id.btn_sync_data);
    }

    /**
     * Setup admin information display
     */
    private void setupAdminInfo() {
        String adminName = authManager.getCurrentUserName();
        tvAdminInfo.setText("Welcome, " + adminName + "!\nAdmin Dashboard - Manage Crime Data");
        Log.d(TAG, "Admin dashboard initialized for user: " + adminName);
    }

    /**
     * Setup button click listeners for admin operations
     */
    private void setupListeners() {
        btnImportDataset.setOnClickListener(v -> checkPermissionAndOpenFilePicker());
        btnAddCrime.setOnClickListener(v -> navigateToAddCrime());
        btnManageCrimes.setOnClickListener(v -> navigateToManageCrimes());
        btnSyncData.setOnClickListener(v -> performDataSync());
    }

    /**
     * Observe ViewModel for crime statistics and sync status (MVVM pattern)
     */
    private void observeViewModel() {
        // Observe crime count from SQLite database
        crimeViewModel.getCrimeCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvCrimeCount.setText("Total Crimes in Database: " + count);
                Log.d(TAG, "Crime count updated from SQLite database: " + count);
            }
        });

        // Observe sync status if using hybrid database
        // This would be implemented if using the HybridCrimeRepository
    }

    private void checkPermissionAndOpenFilePicker() {
        // On Android 10+ (API 29+), ACTION_OPEN_DOCUMENT doesn't need storage permission
        // The system file picker grants access to the selected file automatically
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Log.d(TAG, "Android 10+ detected, opening file picker directly (no permission needed)");
            openFilePicker();
        } else {
            // Only request permission on Android 9 and below
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Opens the file picker to select a CSV file.
     */
    private void openFilePicker() {
        Log.d(TAG, "Opening file picker for CSV import");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        filePickerLauncher.launch(intent);
    }

    /**
     * Reads and processes the selected CSV file.
     */
    private void readCsvFile(Uri uri) {
        try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            List<Crime> crimes = new ArrayList<>();
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                // NOTE: This assumes a specific CSV structure. 
                // You may need to adjust this based on your CSV file's format.
                if (tokens.length >= 10) {
                    Crime crime = new Crime();
                    crime.setCrimeId(tokens[0]);
                    crime.setMonth(tokens[1]);
                    crime.setReportedBy(tokens[2]);
                    crime.setLsoaName(tokens[3]);
                    crime.setLongitude(Double.parseDouble(tokens[4]));
                    crime.setLatitude(Double.parseDouble(tokens[5]));
                    crime.setCrimeType(tokens[9]);
                    crimes.add(crime);
                }
            }
            crimeViewModel.insertCrimes(crimes);
            Toast.makeText(requireContext(), "CSV data imported successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
            Toast.makeText(requireContext(), "Error reading CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to Add Crime Screen (Admin only)
     * Enables creation of new crime records
     */
    private void navigateToAddCrime() {
        Log.d(TAG, "Admin navigating to Add Crime Screen for CRUD operations");
        
        AddUpdateCrimeFragment addFragment = AddUpdateCrimeFragment.newInstance(null);
        
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(addFragment);
        }
    }

    /**
     * Navigate to Manage Crimes Screen (Admin only)
     * Provides access to all crimes with edit/delete capabilities
     */
    private void navigateToManageCrimes() {
        Log.d(TAG, "Admin navigating to Manage Crimes Screen for CRUD operations");
        
        // Navigate to crimes list with admin context for edit/delete operations
        CrimesListFragment crimesFragment = new CrimesListFragment();
        
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(crimesFragment);
        }
    }

    /**
     * Perform data synchronization (if using hybrid database)
     * Syncs local SQLite with remote MySQL database
     */
    private void performDataSync() {
        Log.d(TAG, "Admin initiating data synchronization");
        
        // This would be implemented if using HybridCrimeRepository
        // For now, just update the status
        tvSyncStatus.setText("Sync Status: Local SQLite database active");
    }

    // Lifecycle methods with explicit logging as per coursework requirements

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called - Fragment lifecycle method");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called - Fragment lifecycle method");
        // Refresh statistics when returning to admin dashboard
        if (isAdminUser()) {
            Log.d(TAG, "Refreshing admin dashboard statistics");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called - Fragment lifecycle method");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called - Fragment lifecycle method");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Fragment lifecycle method");
    }
}