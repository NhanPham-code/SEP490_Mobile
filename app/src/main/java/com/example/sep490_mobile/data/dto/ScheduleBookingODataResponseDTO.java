package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ScheduleBookingODataResponseDTO {
    @SerializedName("value")
    private List<ScheduleBookingDTO> value;

    // Getter
    public List<ScheduleBookingDTO> getValue() {
        return value;
    }

    public void setValue(List<ScheduleBookingDTO> value) {
        this.value = value;
    }
}
