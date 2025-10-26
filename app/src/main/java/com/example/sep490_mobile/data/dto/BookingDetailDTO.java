package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class BookingDetailDTO {
    @SerializedName("Id")
    private int id;

    @SerializedName("BookingId")
    private int bookingId;

    @SerializedName("courtId")
    private int courtId;

    @SerializedName("StartTime")
    private String startTime;

    @SerializedName("EndTime")
    private String endTime;

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
