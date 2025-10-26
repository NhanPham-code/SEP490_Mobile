package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class BookingDetailDTO {
    @SerializedName(value = "id", alternate = "Id")
    private int id;

    @SerializedName(value = "bookingId", alternate = "BookingId")
    private int bookingId;

    @SerializedName(value = "courtId", alternate = "CourtId")
    private int courtId;

    @SerializedName(value = "startTime", alternate = "StartTime")
    private String startTime;

    @SerializedName(value = "endTime", alternate = "EndTime")
    private String endTime;

    // Constructor và Getters/Setters không thay đổi
    public BookingDetailDTO(int id, int bookingId, int courtId, String startTime, String endTime) {
        this.id = id;
        this.bookingId = bookingId;
        this.courtId = courtId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}