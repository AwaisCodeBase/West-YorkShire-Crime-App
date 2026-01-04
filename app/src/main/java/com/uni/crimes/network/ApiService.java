package com.uni.crimes.network;

import com.uni.crimes.model.Crime;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * API Service interface for connecting to remote MySQL database
 * Uses Retrofit for HTTP communication with backend server
 */
public interface ApiService {
    
    // Base URL should be configured in NetworkModule
    String BASE_URL = "https://your-backend-server.com/api/";
    
    // GET all crimes from MySQL database
    @GET("crimes")
    Call<List<Crime>> getAllCrimes();
    
    // GET crimes with pagination
    @GET("crimes")
    Call<List<Crime>> getCrimes(@Query("page") int page, @Query("limit") int limit);
    
    // GET crime by ID
    @GET("crimes/{id}")
    Call<Crime> getCrimeById(@Path("id") String crimeId);
    
    // Search crimes by any field
    @GET("crimes/search")
    Call<List<Crime>> searchCrimes(@Query("q") String searchTerm);
    
    // Search crimes by specific field
    @GET("crimes/search")
    Call<List<Crime>> searchCrimesByField(@Query("field") String field, @Query("value") String value);
    
    // POST new crime (Admin only)
    @POST("crimes")
    Call<Crime> createCrime(@Body Crime crime, @Header("Authorization") String token);
    
    // PUT update crime (Admin only)
    @PUT("crimes/{id}")
    Call<Crime> updateCrime(@Path("id") String crimeId, @Body Crime crime, @Header("Authorization") String token);
    
    // DELETE crime (Admin only)
    @DELETE("crimes/{id}")
    Call<Void> deleteCrime(@Path("id") String crimeId, @Header("Authorization") String token);
    
    // POST bulk import from CSV (Admin only)
    @POST("crimes/import")
    Call<ImportResponse> importCrimes(@Body List<Crime> crimes, @Header("Authorization") String token);
    
    // GET crimes by location bounds (for map)
    @GET("crimes/bounds")
    Call<List<Crime>> getCrimesByBounds(
        @Query("north") double north,
        @Query("south") double south,
        @Query("east") double east,
        @Query("west") double west
    );
    
    // Authentication endpoints
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);
    
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);
    
    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Header("Authorization") String refreshToken);
}