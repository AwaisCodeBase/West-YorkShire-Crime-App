package com.uni.crimes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.model.Crime;
import com.uni.crimes.utils.CSVImporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportDatasetFragment extends Fragment {

    private static final String TAG = "ImportDatasetFragment";
    
    private TextView tvInfo, tvStatus;
    private MaterialButton btnImport, btnImportFromFile;
    private ProgressBar progressBar;
    
    private CrimeDatabase database;
    private ExecutorService executor;
    
    // Permission launcher
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    // File picker launcher
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        
        database = CrimeDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        
        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Storage permission granted");
                    openFilePicker();
                } else {
                    Log.d(TAG, "Storage permission denied");
                    Toast.makeText(getContext(), "Storage permission is required to import files", Toast.LENGTH_LONG).show();
                }
            }
        );
        
        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importFromUri(uri);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        return inflater.inflate(R.layout.fragment_import_dataset, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        tvInfo = view.findViewById(R.id.tv_info);
        tvStatus = view.findViewById(R.id.tv_status);
        btnImport = view.findViewById(R.id.btn_import);
        btnImportFromFile = view.findViewById(R.id.btn_import_from_file);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnImport.setOnClickListener(v -> importDataset());
        
        if (btnImportFromFile != null) {
            btnImportFromFile.setOnClickListener(v -> checkPermissionAndPickFile());
        }
    }
    
    private void checkPermissionAndPickFile() {
        Log.d(TAG, "checkPermissionAndPickFile() called, SDK version: " + Build.VERSION.SDK_INT);
        
        // For Android 13+ (API 33+), use photo picker or READ_MEDIA_* permissions
        // For Android 10-12 (API 29-32), use scoped storage with SAF (no permission needed)
        // For Android 9 and below (API 28-), need READ_EXTERNAL_STORAGE
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Use Storage Access Framework, no permission needed for user-selected files
            Log.d(TAG, "Android 13+ detected, opening file picker directly");
            openFilePicker();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12: Scoped storage, SAF doesn't need permission
            Log.d(TAG, "Android 10-12 detected, opening file picker directly");
            openFilePicker();
        } else {
            // Android 9 and below: Need READ_EXTERNAL_STORAGE permission
            Log.d(TAG, "Android 9 or below detected, checking permission");
            if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show explanation to user
                Toast.makeText(getContext(), 
                    "Storage permission is needed to import CSV files from your device", 
                    Toast.LENGTH_LONG).show();
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }
    
    private void openFilePicker() {
        Log.d(TAG, "openFilePicker() called");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Request read permission for the selected file
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            Log.d(TAG, "Launching file picker intent");
            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file picker", e);
            Toast.makeText(getContext(), "Unable to open file picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void importFromUri(Uri uri) {
        Log.d(TAG, "Importing from URI: " + uri);
        
        btnImport.setEnabled(false);
        if (btnImportFromFile != null) {
            btnImportFromFile.setEnabled(false);
        }
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Importing dataset from selected file...");
        tvStatus.setVisibility(View.VISIBLE);
        
        executor.execute(() -> {
            // Try to take persistable permission (may fail, that's okay)
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException e) {
                Log.w(TAG, "Could not take persistable permission (this is normal for some file sources)", e);
            }
            
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                
                List<Crime> crimesToImport = new ArrayList<>();
                String line;
                int lineNumber = 0;
                int importedCount = 0;
                
                // Skip header line
                String header = reader.readLine();
                Log.d(TAG, "CSV Header: " + header);
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    
                    try {
                        Crime crime = parseCrimeLine(line, lineNumber);
                        if (crime != null) {
                            if (!database.crimeDao().crimeExists(crime.getCrimeId())) {
                                crimesToImport.add(crime);
                                importedCount++;
                                
                                if (crimesToImport.size() >= 100) {
                                    database.crimeDao().insertAllCrimes(crimesToImport);
                                    crimesToImport.clear();
                                    
                                    final int progress = importedCount;
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> 
                                            tvStatus.setText("Imported " + progress + " records..."));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing line " + lineNumber, e);
                    }
                }
                
                if (!crimesToImport.isEmpty()) {
                    database.crimeDao().insertAllCrimes(crimesToImport);
                }
                
                final int finalCount = importedCount;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Import successful! Imported " + finalCount + " new crime records.");
                        btnImport.setEnabled(true);
                        if (btnImportFromFile != null) {
                            btnImportFromFile.setEnabled(true);
                        }
                        Toast.makeText(getContext(), "Dataset imported successfully", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied reading file", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Permission denied. Please grant storage access.");
                        btnImport.setEnabled(true);
                        if (btnImportFromFile != null) {
                            btnImportFromFile.setEnabled(true);
                        }
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing from file", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Import failed: " + e.getMessage());
                        btnImport.setEnabled(true);
                        if (btnImportFromFile != null) {
                            btnImportFromFile.setEnabled(true);
                        }
                        Toast.makeText(getContext(), "Import failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    private Crime parseCrimeLine(String line, int lineNumber) {
        try {
            String[] parts = parseCSVLine(line);
            
            if (parts.length < 7) {
                return null;
            }
            
            String crimeId = parts[0].trim();
            String crimeType = parts[1].trim();
            String reportedBy = parts[2].trim();
            String lsoaName = parts[3].trim();
            String latitudeStr = parts[4].trim();
            String longitudeStr = parts[5].trim();
            String outcomeCategory = parts[6].trim();
            
            if (crimeId.isEmpty() || crimeType.isEmpty()) {
                return null;
            }
            
            double latitude = 0.0;
            double longitude = 0.0;
            
            try {
                if (!latitudeStr.isEmpty()) latitude = Double.parseDouble(latitudeStr);
                if (!longitudeStr.isEmpty()) longitude = Double.parseDouble(longitudeStr);
            } catch (NumberFormatException e) {
                // Continue with 0,0 coordinates
            }
            
            String month = "2024-01";
            return new Crime(crimeId, crimeType, reportedBy, lsoaName, latitude, longitude, outcomeCategory, month);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }

    private void importDataset() {
        Log.d(TAG, "Starting dataset import from assets");
        
        btnImport.setEnabled(false);
        if (btnImportFromFile != null) {
            btnImportFromFile.setEnabled(false);
        }
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Importing dataset from crimeyorkshire.csv...");
        tvStatus.setVisibility(View.VISIBLE);
        
        CSVImporter importer = new CSVImporter(requireContext(), database.crimeDao());
        
        executor.execute(() -> {
            importer.importFromAssets("crimeyorkshire.csv", new CSVImporter.ImportListener() {
                @Override
                public void onProgress(int progress) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            tvStatus.setText("Imported " + progress + " records..."));
                    }
                }

                @Override
                public void onSuccess(int importedCount) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvStatus.setText("Import successful! Imported " + importedCount + " new crime records.");
                            btnImport.setEnabled(true);
                            if (btnImportFromFile != null) {
                                btnImportFromFile.setEnabled(true);
                            }
                            Toast.makeText(getContext(), "Dataset imported successfully", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvStatus.setText("Import failed: " + error);
                            btnImport.setEnabled(true);
                            if (btnImportFromFile != null) {
                                btnImportFromFile.setEnabled(true);
                            }
                            Toast.makeText(getContext(), "Import failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });
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
