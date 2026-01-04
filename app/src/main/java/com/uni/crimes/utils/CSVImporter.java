package com.uni.crimes.utils;

import android.content.Context;
import android.util.Log;

import com.uni.crimes.database.CrimeDao;
import com.uni.crimes.model.Crime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing crime data from CSV files
 * Handles parsing CSV from assets folder and inserting into database
 * Prevents duplicate imports as per coursework requirements
 */
public class CSVImporter {
    
    private static final String TAG = "CSVImporter";
    
    private Context context;
    private CrimeDao crimeDao;
    
    public interface ImportListener {
        void onProgress(int progress);
        void onSuccess(int importedCount);
        void onError(String error);
    }
    
    public CSVImporter(Context context, CrimeDao crimeDao) {
        this.context = context;
        this.crimeDao = crimeDao;
    }
    
    /**
     * Import crimes from CSV file in assets folder
     * Expected CSV format: crimeId,crimeType,reportedBy,lsoaName,latitude,longitude,outcomeCategory
     */
    public void importFromAssets(String fileName, ImportListener listener) {
        try {
            Log.d(TAG, "Starting CSV import from: " + fileName);
            
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            List<Crime> crimesToImport = new ArrayList<>();
            String line;
            int lineNumber = 0;
            int importedCount = 0;
            int skippedCount = 0;
            
            // Skip header line
            String header = reader.readLine();
            Log.d(TAG, "CSV Header: " + header);
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    Crime crime = parseCrimeLine(line, lineNumber);
                    if (crime != null) {
                        // Check for duplicates
                        if (!crimeDao.crimeExists(crime.getCrimeId())) {
                            crimesToImport.add(crime);
                            importedCount++;
                            
                            // Batch insert every 100 records for performance
                            if (crimesToImport.size() >= 100) {
                                crimeDao.insertAllCrimes(crimesToImport);
                                crimesToImport.clear();
                                
                                // Report progress
                                listener.onProgress(importedCount);
                            }
                        } else {
                            skippedCount++;
                            Log.d(TAG, "Skipping duplicate crime: " + crime.getCrimeId());
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing line " + lineNumber + ": " + line, e);
                    // Continue with next line
                }
            }
            
            // Insert remaining crimes
            if (!crimesToImport.isEmpty()) {
                crimeDao.insertAllCrimes(crimesToImport);
            }
            
            reader.close();
            inputStream.close();
            
            Log.d(TAG, "CSV import completed. Imported: " + importedCount + ", Skipped: " + skippedCount);
            listener.onSuccess(importedCount);
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
            listener.onError("Failed to read CSV file: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error during CSV import", e);
            listener.onError("Import failed: " + e.getMessage());
        }
    }
    
    /**
     * Parse a single CSV line into a Crime object
     * Expected format: crimeId,crimeType,reportedBy,lsoaName,latitude,longitude,outcomeCategory
     */
    private Crime parseCrimeLine(String line, int lineNumber) {
        try {
            // Split by comma, handling quoted values
            String[] parts = parseCSVLine(line);
            
            if (parts.length < 7) {
                Log.w(TAG, "Line " + lineNumber + " has insufficient columns: " + parts.length);
                return null;
            }
            
            String crimeId = parts[0].trim();
            String crimeType = parts[1].trim();
            String reportedBy = parts[2].trim();
            String lsoaName = parts[3].trim();
            String latitudeStr = parts[4].trim();
            String longitudeStr = parts[5].trim();
            String outcomeCategory = parts[6].trim();
            
            // Validate required fields
            if (crimeId.isEmpty() || crimeType.isEmpty()) {
                Log.w(TAG, "Line " + lineNumber + " missing required fields");
                return null;
            }
            
            // Parse coordinates
            double latitude = 0.0;
            double longitude = 0.0;
            
            try {
                if (!latitudeStr.isEmpty()) {
                    latitude = Double.parseDouble(latitudeStr);
                }
                if (!longitudeStr.isEmpty()) {
                    longitude = Double.parseDouble(longitudeStr);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid coordinates on line " + lineNumber + ": " + latitudeStr + ", " + longitudeStr);
                // Continue with 0,0 coordinates
            }
            
            // Generate month if not provided (use current month as default)
            String month = "2024-01"; // Default month for imported data
            
            return new Crime(crimeId, crimeType, reportedBy, lsoaName, 
                           latitude, longitude, outcomeCategory, month);
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing line " + lineNumber + ": " + line, e);
            return null;
        }
    }
    
    /**
     * Parse CSV line handling quoted values and commas within quotes
     */
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
        
        // Add the last field
        result.add(currentField.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Create sample CSV data for testing (if crimeyorkshire.csv is not available)
     */
    public void createSampleData(ImportListener listener) {
        try {
            Log.d(TAG, "Creating sample crime data");
            
            List<Crime> sampleCrimes = new ArrayList<>();
            
            // Sample Yorkshire crime data
            sampleCrimes.add(new Crime("CRIME001", "Burglary", "West Yorkshire Police", 
                    "Leeds 001A", 53.8008, -1.5491, "Investigation complete; no suspect identified", "2024-01"));
            
            sampleCrimes.add(new Crime("CRIME002", "Vehicle crime", "West Yorkshire Police", 
                    "Bradford 002B", 53.7960, -1.7594, "Under investigation", "2024-01"));
            
            sampleCrimes.add(new Crime("CRIME003", "Anti-social behaviour", "West Yorkshire Police", 
                    "Wakefield 003C", 53.6833, -1.5000, "No further action", "2024-01"));
            
            sampleCrimes.add(new Crime("CRIME004", "Violence and sexual offences", "West Yorkshire Police", 
                    "Huddersfield 004D", 53.6458, -1.7850, "Awaiting court outcome", "2024-01"));
            
            sampleCrimes.add(new Crime("CRIME005", "Shoplifting", "West Yorkshire Police", 
                    "Halifax 005E", 53.7248, -1.8583, "Offender given penalty notice", "2024-01"));
            
            sampleCrimes.add(new Crime("CRIME006", "Public order", "West Yorkshire Police", 
                    "Dewsbury 006F", 53.6900, -1.6300, "Investigation complete; no suspect identified", "2024-02"));
            
            sampleCrimes.add(new Crime("CRIME007", "Criminal damage and arson", "West Yorkshire Police", 
                    "Keighley 007G", 53.8671, -2.0000, "Under investigation", "2024-02"));
            
            sampleCrimes.add(new Crime("CRIME008", "Drugs", "West Yorkshire Police", 
                    "Batley 008H", 53.7167, -1.6333, "Offender given a caution", "2024-02"));
            
            sampleCrimes.add(new Crime("CRIME009", "Bicycle theft", "West Yorkshire Police", 
                    "Castleford 009I", 53.7167, -1.3667, "Investigation complete; no suspect identified", "2024-02"));
            
            sampleCrimes.add(new Crime("CRIME010", "Robbery", "West Yorkshire Police", 
                    "Pontefract 010J", 53.6833, -1.3167, "Awaiting court outcome", "2024-02"));
            
            // Insert sample data
            crimeDao.insertAllCrimes(sampleCrimes);
            
            Log.d(TAG, "Sample data created successfully");
            listener.onSuccess(sampleCrimes.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating sample data", e);
            listener.onError("Failed to create sample data: " + e.getMessage());
        }
    }
}