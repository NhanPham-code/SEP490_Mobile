package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleStadiumDTO {
    @SerializedName("Id")
    private int id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Courts")
    private List<ScheduleCourtDTO> courts;

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<ScheduleCourtDTO> getCourts() { return courts; }
}
