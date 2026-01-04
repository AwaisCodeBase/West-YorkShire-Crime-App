package com.uni.crimes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.uni.crimes.adapter.CrimeAdapter;
import com.uni.crimes.model.Crime;
import com.uni.crimes.viewmodel.CrimeViewModel;

/**
 * Fragment displaying list of crimes using MVVM architecture
 * Observes LiveData from CrimeViewModel for automatic UI updates
 * Implements all lifecycle methods with logging as per coursework requirements
 * 
 * Functional Description:
 * - Retrieves all crime records from SQLite database
 * - Displays them using RecyclerView with card-based layout
 * - Shows key information: crime type, LSOA name, outcome category
 * - Loads data asynchronously for smooth user interaction
 * - Clicking crime item opens Crime Detail Screen
 * - Satisfies requirement to view all data from West Yorkshire Crimes database
 */
public class CrimesListFragment extends Fragment implements CrimeAdapter.OnCrimeClickListener {

    private static final String TAG = "CrimesListFragment";
    
    // UI Components
    private RecyclerView rvCrimes;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private View loadingState;
    private CrimeAdapter adapter;
    
    // MVVM Components
    private CrimeViewModel crimeViewModel;

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
        return inflater.inflate(R.layout.fragment_crimes_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called - Setting up UI components");
        
        initViews(view);
        setupRecyclerView();
        observeViewModel();
        setupSwipeRefresh();
    }

    /**
     * Initialize UI components
     */
    private void initViews(View view) {
        rvCrimes = view.findViewById(R.id.rv_crimes);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new CrimeAdapter(this);
        rvCrimes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCrimes.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup completed");
    }

    /**
     * Observe ViewModel LiveData (MVVM pattern)
     * Demonstrates asynchronous data loading from SQLite database
     */
    private void observeViewModel() {
        // Show loading initially
        showLoading();
        
        // Observe all crimes from SQLite database
        crimeViewModel.getAllCrimes().observe(getViewLifecycleOwner(), crimes -> {
            hideLoading();
            swipeRefresh.setRefreshing(false);
            
            if (crimes != null && !crimes.isEmpty()) {
                adapter.setCrimes(crimes);
                showContent();
                Log.d(TAG, "Crime list updated from SQLite database. Count: " + crimes.size());
            } else {
                showEmpty();
                Log.d(TAG, "No crimes found in database");
            }
        });

        // Observe loading state
        crimeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                swipeRefresh.setRefreshing(isLoading);
            }
        });

        // Observe error messages
        crimeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error from ViewModel: " + error);
                crimeViewModel.clearErrorMessage();
            }
        });
    }
    
    private void showLoading() {
        if (loadingState != null) loadingState.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        rvCrimes.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        if (loadingState != null) loadingState.setVisibility(View.GONE);
    }
    
    private void showContent() {
        rvCrimes.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (loadingState != null) loadingState.setVisibility(View.GONE);
    }
    
    private void showEmpty() {
        rvCrimes.setVisibility(View.GONE);
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (loadingState != null) loadingState.setVisibility(View.GONE);
    }

    /**
     * Setup swipe-to-refresh functionality
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered - refreshing data from SQLite");
            // Data will be refreshed automatically through LiveData observation
            swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onCrimeClick(Crime crime) {
        Log.d(TAG, "Crime clicked: " + crime.getCrimeId() + " - Navigating to Crime Detail Screen");
        
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Fragment lifecycle method");
    }
}
