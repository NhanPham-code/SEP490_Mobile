package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class BookingSlotRequest {

    @SerializedName("CourtId")
    private int courtId;

    @SerializedName("StartTime")
    private String startTime; // Format: "yyyy-MM-dd'T'HH:mm:ss"

    @SerializedName("EndTime")
    private String endTime; // Format: "yyyy-MM-dd'T'HH:mm:ss"

    public BookingSlotRequest(int courtId, String startTime, String endTime) {
        this.courtId = courtId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters if needed
}