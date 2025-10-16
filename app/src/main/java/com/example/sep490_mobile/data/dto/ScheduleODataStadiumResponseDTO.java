package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleODataStadiumResponseDTO {
    @SerializedName("value")
    private List<ScheduleStadiumDTO> value;

    // Getter
    public List<ScheduleStadiumDTO> getValue() { return value; }
}
