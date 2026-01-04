package com.uni.crimes.network;

/**
 * Response model for CSV import API calls
 */
public class ImportResponse {
    private int importedCount;
    private int totalCount;
    private int duplicateCount;
    private String message;
    private boolean success;
    
    public ImportResponse() {}
    
    public ImportResponse(int importedCount, int totalCount, int duplicateCount, String message, boolean success) {
        this.importedCount = importedCount;
        this.totalCount = totalCount;
        this.duplicateCount = duplicateCount;
        this.message = message;
        this.success = success;
    }
    
    // Getters and Setters
    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
    
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    
    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}