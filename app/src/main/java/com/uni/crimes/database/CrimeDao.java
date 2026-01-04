package com.uni.crimes.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.uni.crimes.model.Crime;

import java.util.List;

/**
 * Data Access Object (DAO) for Crime entity
 * Provides all required database operations as per coursework requirements
 */
@Dao
public interface CrimeDao {
    
    // Required DAO methods as per coursework specification
    
    /**
     * Insert a single crime record
     * @param crime Crime object to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrime(Crime crime);
    
    /**
     * Insert multiple crime records (for CSV import)
     * @param crimes List of Crime objects to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCrimes(List<Crime> crimes);
    
    /**
     * Get all crimes from database
     * @return LiveData list of all crimes (for MVVM pattern)
     */
    @Query("SELECT * FROM crimes ORDER BY crimeId DESC")
    LiveData<List<Crime>> getAllCrimes();
    
    /**
     * Get all crimes synchronously (for non-UI operations)
     * @return List of all crimes
     */
    @Query("SELECT * FROM crimes ORDER BY crimeId DESC")
    List<Crime> getAllCrimesSync();
    
    /**
     * Search crimes by any field using LIKE query
     * @param searchTerm The term to search for
     * @return LiveData list of matching crimes
     */
    @Query("SELECT * FROM crimes WHERE " +
           "crimeType LIKE '%' || :searchTerm || '%' OR " +
           "lsoaName LIKE '%' || :searchTerm || '%' OR " +
           "outcomeCategory LIKE '%' || :searchTerm || '%' OR " +
           "reportedBy LIKE '%' || :searchTerm || '%' OR " +
           "crimeId LIKE '%' || :searchTerm || '%'")
    LiveData<List<Crime>> searchCrimesByAnyField(String searchTerm);
    
    /**
     * Search crimes by any field synchronously
     * @param searchTerm The term to search for
     * @return List of matching crimes
     */
    @Query("SELECT * FROM crimes WHERE " +
           "crimeType LIKE '%' || :searchTerm || '%' OR " +
           "lsoaName LIKE '%' || :searchTerm || '%' OR " +
           "outcomeCategory LIKE '%' || :searchTerm || '%' OR " +
           "reportedBy LIKE '%' || :searchTerm || '%' OR " +
           "crimeId LIKE '%' || :searchTerm || '%'")
    List<Crime> searchCrimesByAnyFieldSync(String searchTerm);
    
    /**
     * Search crimes by specific field
     * @param field The field name to search in
     * @param value The value to search for
     * @return LiveData list of matching crimes
     */
    @Query("SELECT * FROM crimes WHERE " +
           "CASE :field " +
           "WHEN 'crimeType' THEN crimeType LIKE '%' || :value || '%' " +
           "WHEN 'lsoaName' THEN lsoaName LIKE '%' || :value || '%' " +
           "WHEN 'outcomeCategory' THEN outcomeCategory LIKE '%' || :value || '%' " +
           "WHEN 'reportedBy' THEN reportedBy LIKE '%' || :value || '%' " +
           "ELSE crimeType LIKE '%' || :value || '%' END")
    LiveData<List<Crime>> searchByField(String field, String value);
    
    /**
     * Update an existing crime record
     * @param crime Crime object with updated data
     */
    @Update
    void updateCrime(Crime crime);
    
    /**
     * Delete a crime record
     * @param crime Crime object to delete
     */
    @Delete
    void deleteCrime(Crime crime);
    
    // Additional utility methods
    
    /**
     * Get a specific crime by ID
     * @param crimeId The crime ID to search for
     * @return Crime object or null if not found
     */
    @Query("SELECT * FROM crimes WHERE crimeId = :crimeId")
    Crime getCrimeById(String crimeId);
    
    /**
     * Get a specific crime by ID as LiveData
     * @param crimeId The crime ID to search for
     * @return LiveData Crime object
     */
    @Query("SELECT * FROM crimes WHERE crimeId = :crimeId")
    LiveData<Crime> getCrimeByIdLive(String crimeId);
    
    /**
     * Delete all crimes (for fresh import)
     */
    @Query("DELETE FROM crimes")
    void deleteAllCrimes();
    
    /**
     * Get total crime count
     * @return Number of crimes in database
     */
    @Query("SELECT COUNT(*) FROM crimes")
    int getCrimeCount();
    
    /**
     * Get crime count as LiveData
     * @return LiveData count of crimes
     */
    @Query("SELECT COUNT(*) FROM crimes")
    LiveData<Integer> getCrimeCountLive();
    
    /**
     * Check if crime exists by ID (prevent duplicates during import)
     * @param crimeId The crime ID to check
     * @return True if crime exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM crimes WHERE crimeId = :crimeId)")
    boolean crimeExists(String crimeId);
}