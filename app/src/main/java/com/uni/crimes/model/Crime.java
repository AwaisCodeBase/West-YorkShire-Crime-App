package com.uni.crimes.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Crime Entity representing a crime record from the Yorkshire crimes dataset
 * Matches the CSV structure: crimeId, crimeType, reportedBy, lsoaName, latitude, longitude, outcomeCategory
 */
@Entity(tableName = "crimes")
public class Crime {
    @PrimaryKey
    @NonNull
    private String crimeId;
    private String crimeType;
    private String reportedBy;
    private String lsoaName;
    private double latitude;
    private double longitude;
    private String outcomeCategory; // Changed from 'outcome' to match CSV
    private String month; // Additional field for temporal data

    // Default constructor required by Room
    public Crime() {}

    // Full constructor
    @Ignore
    public Crime(@NonNull String crimeId, String crimeType, String reportedBy, String lsoaName, 
                 double latitude, double longitude, String outcomeCategory, String month) {
        this.crimeId = crimeId;
        this.crimeType = crimeType;
        this.reportedBy = reportedBy;
        this.lsoaName = lsoaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.outcomeCategory = outcomeCategory;
        this.month = month;
    }

    // Getters and Setters
    @NonNull
    public String getCrimeId() { return crimeId; }
    public void setCrimeId(@NonNull String crimeId) { this.crimeId = crimeId; }

    public String getCrimeType() { return crimeType; }
    public void setCrimeType(String crimeType) { this.crimeType = crimeType; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getLsoaName() { return lsoaName; }
    public void setLsoaName(String lsoaName) { this.lsoaName = lsoaName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getOutcomeCategory() { return outcomeCategory; }
    public void setOutcomeCategory(String outcomeCategory) { this.outcomeCategory = outcomeCategory; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    // Backward compatibility - keep getOutcome() for existing code
    public String getOutcome() { return outcomeCategory; }
    public void setOutcome(String outcome) { this.outcomeCategory = outcome; }

    @Override
    public String toString() {
        return "Crime{" +
                "crimeId='" + crimeId + '\'' +
                ", crimeType='" + crimeType + '\'' +
                ", lsoaName='" + lsoaName + '\'' +
                ", outcomeCategory='" + outcomeCategory + '\'' +
                '}';
    }
}
