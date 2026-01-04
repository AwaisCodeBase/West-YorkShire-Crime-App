package com.uni.crimes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.uni.crimes.adapter.CrimeAdapter;
import com.uni.crimes.model.Crime;
import com.uni.crimes.viewmodel.CrimeViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements CrimeAdapter.OnCrimeClickListener {

    private static final String TAG = "SearchFragment";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // UI Components
    private AutoCompleteTextView spinnerSearchField;
    private TextInputEditText etSearchTerm;
    private MaterialButton btnSearch, btnShowOnMap;
    private RecyclerView rvSearchResults;
    private View tvNoResults;
    private ProgressBar progressSearch;

    // MVVM Components
    private CrimeViewModel crimeViewModel;
    private CrimeAdapter adapter;
    private List<Crime> currentResults = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - Fragment lifecycle method");

        // Initialize ViewModel (MVVM pattern)
        crimeViewModel = new ViewModelProvider(this).get(CrimeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called - Fragment lifecycle method");
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called - Setting up search functionality");

        initViews(view);
        setupSpinner();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permissions granted");
            } else {
                Toast.makeText(getContext(), "Storage permission is required to import files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize UI components
     */
    private void initViews(View view) {
        spinnerSearchField = view.findViewById(R.id.spinner_search_field);
        etSearchTerm = view.findViewById(R.id.et_search_term);
        btnSearch = view.findViewById(R.id.btn_search);
        btnShowOnMap = view.findViewById(R.id.btn_show_on_map);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        progressSearch = view.findViewById(R.id.progress_search);
    }

    /**
     * Setup search field spinner with all searchable fields
     */
    private void setupSpinner() {
        String[] searchFields = {
                "All Fields",
                "Crime Type",
                "LSOA Name",
                "Outcome Category",
                "Reported By"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                searchFields
        );
        spinnerSearchField.setAdapter(adapter);
        spinnerSearchField.setText(searchFields[0], false);

        Log.d(TAG, "Search field spinner setup completed");
    }

    /**
     * Setup RecyclerView for search results
     */
    private void setupRecyclerView() {
        adapter = new CrimeAdapter(this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);
    }

    /**
     * Setup button click listeners
     */
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnShowOnMap.setOnClickListener(v -> showResultsOnMap());
    }

    /**
     * Observe ViewModel LiveData for search results (MVVM pattern)
     */
    private void observeViewModel() {
        // Observe search results from SQLite database
        crimeViewModel.getSearchResults().observe(getViewLifecycleOwner(), crimes -> {
            if (crimes != null) {
                currentResults = crimes;
                displayResults(crimes);
                btnShowOnMap.setEnabled(!crimes.isEmpty());
                Log.d(TAG, "Search results updated from SQLite database. Count: " + crimes.size());
            }
        });

        // Observe loading state
        crimeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                btnSearch.setEnabled(!isLoading);
                if (progressSearch != null) {
                    progressSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
                if (isLoading) {
                    Log.d(TAG, "Search in progress on SQLite database...");
                }
            }
        });

        // Observe error messages
        crimeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Search error from ViewModel: " + error);
                displayResults(new ArrayList<>()); // Show empty results on error
                crimeViewModel.clearErrorMessage();
            }
        });
    }

    /**
     * Perform search using ViewModel (MVVM pattern)
     * Supports both "All Fields" and specific field search as per coursework requirements
     * Executes SQL LIKE queries on SQLite database
     */
    private void performSearch() {
        String searchTerm = etSearchTerm.getText().toString().trim();
        if (searchTerm.isEmpty()) {
            Log.w(TAG, "Search term is empty");
            return;
        }

        String selectedField = spinnerSearchField.getText().toString();
        Log.d(TAG, "Performing search on SQLite database - Field: " + selectedField + ", Term: " + searchTerm);

        if ("All Fields".equals(selectedField)) {
            // Search across all fields using SQL LIKE queries
            crimeViewModel.searchCrimes(searchTerm);
        } else {
            // Search specific field
            crimeViewModel.searchByField(selectedField, searchTerm);
        }
    }

    /**
     * Display search results in RecyclerView
     */
    private void displayResults(List<Crime> results) {
        if (results.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
            Log.d(TAG, "No search results to display");
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
            adapter.setCrimes(results);
            Log.d(TAG, "Displaying " + results.size() + " search results");
        }
    }

    /**
     * Show search results on map by passing filtered crime list
     * Enables geographic analysis of crime data
     */
    private void showResultsOnMap() {
        if (!currentResults.isEmpty()) {
            Log.d(TAG, "Showing " + currentResults.size() + " search results on map for geographic analysis");

            MapFragment mapFragment = MapFragment.newInstance(new ArrayList<>(currentResults));

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(mapFragment);
            }
        }
    }

    @Override
    public void onCrimeClick(Crime crime) {
        Log.d(TAG, "Search result clicked: " + crime.getCrimeId() + " - Navigating to Crime Detail Screen");

        CrimeDetailFragment detailFragment = CrimeDetailFragment.newInstance(crime.getCrimeId());

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(detailFragment);
        }
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
        // Clear search results when leaving fragment
        crimeViewModel.clearSearchResults();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Fragment lifecycle method");
    }
}
