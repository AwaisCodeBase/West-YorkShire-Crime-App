package com.uni.crimes.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.uni.crimes.model.Crime;

/**
 * Room Database for Crime data storage
 * Handles database creation and provides DAO access
 */
@Database(entities = {Crime.class}, version = 2, exportSchema = false)
public abstract class CrimeDatabase extends RoomDatabase {
    
    private static final String TAG = "CrimeDatabase";
    private static CrimeDatabase INSTANCE;
    
    public abstract CrimeDao crimeDao();
    
    public static synchronized CrimeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            try {
                Log.d(TAG, "Creating database instance");
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        CrimeDatabase.class, "crime_database")
                        .fallbackToDestructiveMigration() // Handle schema changes
                        .build();
                Log.d(TAG, "Database instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating database", e);
                throw new RuntimeException("Failed to create database", e);
            }
        }
        return INSTANCE;
    }
}