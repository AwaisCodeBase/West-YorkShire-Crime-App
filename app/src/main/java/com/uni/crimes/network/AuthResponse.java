package com.uni.crimes.network;

/**
 * Response model for authentication API calls
 */
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String userRole;
    private String userName;
    private String message;
    private boolean success;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, String refreshToken, String userRole, String userName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userRole = userRole;
        this.userName = userName;
        this.success = true;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}