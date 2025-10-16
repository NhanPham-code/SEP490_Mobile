package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class ScheduleCourtDTO {
    @SerializedName("Id")
    private int id;
    @SerializedName("Name")
    private String name;
    @SerializedName("PricePerHour")
    private double pricePerHour;

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPricePerHour() { return pricePerHour; }
}
