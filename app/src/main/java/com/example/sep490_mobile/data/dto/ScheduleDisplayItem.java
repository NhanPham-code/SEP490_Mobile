package com.example.sep490_mobile.data.dto;

import java.util.List;

public class ScheduleDisplayItem {
    private final String stadiumName;
    private final String startTime;
    private final String endTime;
    private final String status;
    private final List<String> courtNames; // <-- Sửa lại thành List<String>
    private final ScheduleBookingDTO originalBooking;

    public ScheduleDisplayItem(String stadiumName, String startTime, String endTime, String status, List<String> courtNames, ScheduleBookingDTO originalBooking) {
        this.stadiumName = stadiumName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.courtNames = courtNames;
        this.originalBooking = originalBooking;
    }

    // Getters
    public String getStadiumName() { return stadiumName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public List<String> getCourtNames() { return courtNames; } // <-- Sửa lại getter

    public ScheduleBookingDTO getOriginalBooking() {
        return originalBooking;
    }
}