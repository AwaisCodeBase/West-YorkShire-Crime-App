package com.uni.crimes.utils;

import com.uni.crimes.database.CrimeDatabase;
import com.uni.crimes.model.Crime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleDataLoader {
    
    public static void loadSampleData(CrimeDatabase database) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            // Check if data already exists
            int count = database.crimeDao().getCrimeCount();
            if (count > 0) {
                return; // Data already loaded
            }
            
            List<Crime> sampleCrimes = createSampleCrimes();
            database.crimeDao().insertAllCrimes(sampleCrimes);
        });
        
        executor.shutdown();
    }
    
    private static List<Crime> createSampleCrimes() {
        List<Crime> crimes = new ArrayList<>();
        
        crimes.add(new Crime("CRIME001", "Burglary", "West Yorkshire Police", 
                "Leeds 001A", 53.8008, -1.5491, "Investigation complete; no suspect identified", "2024-01"));
        
        crimes.add(new Crime("CRIME002", "Vehicle crime", "West Yorkshire Police", 
                "Bradford 002B", 53.7960, -1.7594, "Under investigation", "2024-01"));
        
        crimes.add(new Crime("CRIME003", "Anti-social behaviour", "West Yorkshire Police", 
                "Wakefield 003C", 53.6833, -1.5000, "No further action", "2024-01"));
        
        crimes.add(new Crime("CRIME004", "Violence and sexual offences", "West Yorkshire Police", 
                "Huddersfield 004D", 53.6458, -1.7850, "Awaiting court outcome", "2024-01"));
        
        crimes.add(new Crime("CRIME005", "Shoplifting", "West Yorkshire Police", 
                "Halifax 005E", 53.7248, -1.8583, "Offender given penalty notice", "2024-01"));
        
        crimes.add(new Crime("CRIME006", "Public order", "West Yorkshire Police", 
                "Dewsbury 006F", 53.6900, -1.6300, "Investigation complete; no suspect identified", "2024-02"));
        
        crimes.add(new Crime("CRIME007", "Criminal damage and arson", "West Yorkshire Police", 
                "Keighley 007G", 53.8671, -2.0000, "Under investigation", "2024-02"));
        
        crimes.add(new Crime("CRIME008", "Drugs", "West Yorkshire Police", 
                "Batley 008H", 53.7167, -1.6333, "Offender given a caution", "2024-02"));
        
        crimes.add(new Crime("CRIME009", "Bicycle theft", "West Yorkshire Police", 
                "Castleford 009I", 53.7167, -1.3667, "Investigation complete; no suspect identified", "2024-02"));
        
        crimes.add(new Crime("CRIME010", "Robbery", "West Yorkshire Police", 
                "Pontefract 010J", 53.6833, -1.3167, "Awaiting court outcome", "2024-02"));
        
        return crimes;
    }
}