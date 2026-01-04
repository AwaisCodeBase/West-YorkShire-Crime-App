package com.uni.crimes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.uni.crimes.auth.AuthManager;
import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.model.Crime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrimeDetailFragment extends Fragment {

    private static final String TAG = "CrimeDetailFragment";
    private static final String ARG_CRIME_ID = "crime_id";
    
    private TextView tvCrimeId, tvCrimeType, tvReportedBy, tvLsoaName, tvCoordinates, tvOutcome;
    private LinearLayout llAdminButtons;
    private MaterialButton btnEdit, btnDelete;
    
    private String crimeId;
    private Crime currentCrime;
    private CrimeDatabase database;
    private AuthManager authManager;
    private ExecutorService executor;

    public static CrimeDetailFragment newInstance(String crimeId) {
        CrimeDetailFragment fragment = new CrimeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CRIME_ID, crimeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        
        if (getArguments() != null) {
            crimeId = getArguments().getString(ARG_CRIME_ID);
        }
        
        database = CrimeDatabase.getInstance(requireContext());
        authManager = new AuthManager(requireContext());
        executor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        return inflater.inflate(R.layout.fragment_crime_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        
        initViews(view);
        setupAdminButtons();
        loadCrimeDetails();
    }

    private void initViews(View view) {
        tvCrimeId = view.findViewById(R.id.tv_crime_id);
        tvCrimeType = view.findViewById(R.id.tv_crime_type);
        tvReportedBy = view.findViewById(R.id.tv_reported_by);
        tvLsoaName = view.findViewById(R.id.tv_lsoa_name);
        tvCoordinates = view.findViewById(R.id.tv_coordinates);
        tvOutcome = view.findViewById(R.id.tv_outcome);
        llAdminButtons = view.findViewById(R.id.ll_admin_buttons);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
    }

    private void setupAdminButtons() {
        boolean isAdmin = "Admin".equals(authManager.getCurrentUserRole());
        llAdminButtons.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        
        if (isAdmin) {
            btnEdit.setOnClickListener(v -> editCrime());
            btnDelete.setOnClickListener(v -> deleteCrime());
        }
    }

    private void loadCrimeDetails() {
        if (crimeId == null) {
            Toast.makeText(getContext(), "Crime ID not provided", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(() -> {
            Crime crime = database.crimeDao().getCrimeById(crimeId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (crime != null) {
                        currentCrime = crime;
                        displayCrimeDetails(crime);
                    } else {
                        Toast.makeText(getContext(), "Crime not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void displayCrimeDetails(Crime crime) {
        tvCrimeId.setText(crime.getCrimeId());
        tvCrimeType.setText(crime.getCrimeType());
        tvReportedBy.setText(crime.getReportedBy());
        tvLsoaName.setText(crime.getLsoaName());
        tvCoordinates.setText(String.format("%.6f, %.6f", crime.getLatitude(), crime.getLongitude()));
        tvOutcome.setText(crime.getOutcome());
    }

    private void editCrime() {
        if (currentCrime != null) {
            Log.d(TAG, "Edit crime: " + currentCrime.getCrimeId());
            // Navigate to AddUpdateCrimeFragment for editing
            AddUpdateCrimeFragment editFragment = AddUpdateCrimeFragment.newInstance(currentCrime.getCrimeId());
            
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(editFragment);
            }
        }
    }

    private void deleteCrime() {
        if (currentCrime != null) {
            Log.d(TAG, "Delete crime: " + currentCrime.getCrimeId());
            
            executor.execute(() -> {
                database.crimeDao().deleteCrime(currentCrime);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Crime deleted successfully", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    });
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        if (executor != null) {
            executor.shutdown();
        }
    }
}