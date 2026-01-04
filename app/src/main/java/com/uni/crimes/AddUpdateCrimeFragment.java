package com.uni.crimes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.model.Crime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddUpdateCrimeFragment extends Fragment {

    private static final String TAG = "AddUpdateCrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id";
    
    private TextInputEditText etCrimeId, etCrimeType, etReportedBy, etLsoaName, 
                             etLatitude, etLongitude, etOutcome, etMonth;
    private MaterialButton btnSave, btnCancel;
    
    private String crimeId;
    private boolean isEditMode = false;
    private Crime currentCrime;
    private CrimeDatabase database;
    private ExecutorService executor;

    public static AddUpdateCrimeFragment newInstance(String crimeId) {
        AddUpdateCrimeFragment fragment = new AddUpdateCrimeFragment();
        Bundle args = new Bundle();
        if (crimeId != null) {
            args.putString(ARG_CRIME_ID, crimeId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        
        if (getArguments() != null) {
            crimeId = getArguments().getString(ARG_CRIME_ID);
            isEditMode = crimeId != null;
        }
        
        database = CrimeDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        return inflater.inflate(R.layout.fragment_add_update_crime, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        
        initViews(view);
        setupListeners();
        
        if (isEditMode) {
            loadCrimeData();
        }
    }

    private void initViews(View view) {
        etCrimeId = view.findViewById(R.id.et_crime_id);
        etCrimeType = view.findViewById(R.id.et_crime_type);
        etReportedBy = view.findViewById(R.id.et_reported_by);
        etLsoaName = view.findViewById(R.id.et_lsoa_name);
        etLatitude = view.findViewById(R.id.et_latitude);
        etLongitude = view.findViewById(R.id.et_longitude);
        etOutcome = view.findViewById(R.id.et_outcome);
        etMonth = view.findViewById(R.id.et_month);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etCrimeId.addTextChangedListener(textWatcher);
        etCrimeType.addTextChangedListener(textWatcher);
        etReportedBy.addTextChangedListener(textWatcher);
        etLsoaName.addTextChangedListener(textWatcher);
        etLatitude.addTextChangedListener(textWatcher);
        etLongitude.addTextChangedListener(textWatcher);
        etOutcome.addTextChangedListener(textWatcher);
        etMonth.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(v -> saveCrime());
        btnCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadCrimeData() {
        if (crimeId == null) return;
        
        executor.execute(() -> {
            Crime crime = database.crimeDao().getCrimeById(crimeId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (crime != null) {
                        currentCrime = crime;
                        populateFields(crime);
                    } else {
                        Toast.makeText(getContext(), "Crime not found", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }
                });
            }
        });
    }

    private void populateFields(Crime crime) {
        etCrimeId.setText(crime.getCrimeId());
        etCrimeType.setText(crime.getCrimeType());
        etReportedBy.setText(crime.getReportedBy());
        etLsoaName.setText(crime.getLsoaName());
        etLatitude.setText(String.valueOf(crime.getLatitude()));
        etLongitude.setText(String.valueOf(crime.getLongitude()));
        etOutcome.setText(crime.getOutcome());
        etMonth.setText(crime.getMonth());
        
        // Disable crime ID editing in edit mode
        etCrimeId.setEnabled(false);
    }

    private void validateInputs() {
        String crimeIdText = etCrimeId.getText().toString().trim();
        String crimeType = etCrimeType.getText().toString().trim();
        String reportedBy = etReportedBy.getText().toString().trim();
        String lsoaName = etLsoaName.getText().toString().trim();
        String latitudeText = etLatitude.getText().toString().trim();
        String longitudeText = etLongitude.getText().toString().trim();
        String outcome = etOutcome.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        
        boolean isValid = !crimeIdText.isEmpty() && 
                         !crimeType.isEmpty() && 
                         !reportedBy.isEmpty() && 
                         !lsoaName.isEmpty() && 
                         !latitudeText.isEmpty() && 
                         !longitudeText.isEmpty() && 
                         !outcome.isEmpty() && 
                         !month.isEmpty();
        
        // Validate latitude and longitude
        if (isValid) {
            try {
                double lat = Double.parseDouble(latitudeText);
                double lng = Double.parseDouble(longitudeText);
                isValid = lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }
        
        btnSave.setEnabled(isValid);
    }

    private void saveCrime() {
        String crimeIdText = etCrimeId.getText().toString().trim();
        String crimeType = etCrimeType.getText().toString().trim();
        String reportedBy = etReportedBy.getText().toString().trim();
        String lsoaName = etLsoaName.getText().toString().trim();
        String latitudeText = etLatitude.getText().toString().trim();
        String longitudeText = etLongitude.getText().toString().trim();
        String outcome = etOutcome.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        
        try {
            double latitude = Double.parseDouble(latitudeText);
            double longitude = Double.parseDouble(longitudeText);
            
            Crime crime = new Crime(crimeIdText, crimeType, reportedBy, lsoaName, 
                                  latitude, longitude, outcome, month);
            
            btnSave.setEnabled(false);
            
            executor.execute(() -> {
                try {
                    if (isEditMode) {
                        database.crimeDao().updateCrime(crime);
                    } else {
                        database.crimeDao().insertCrime(crime);
                    }
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String message = isEditMode ? "Crime updated successfully" : "Crime added successfully";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        });
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error saving crime", e);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error saving crime: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                        });
                    }
                }
            });
            
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
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