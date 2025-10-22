package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class BookingDetailCreateDto {

    @SerializedName("CourtId") // Match C# property name if needed
    private int courtId;

    @SerializedName("StartTime") // Match C# property name if needed
    private String startTime; // Expecting ISO 8601 format: "yyyy-MM-ddTHH:mm:ss"

    @SerializedName("EndTime") // Match C# property name if needed
    private String endTime;   // Expecting ISO 8601 format: "yyyy-MM-ddTHH:mm:ss"

    public BookingDetailCreateDto(int courtId, String startTime, String endTime) {
        this.courtId = courtId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters if needed
    public int getCourtId() { return courtId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}