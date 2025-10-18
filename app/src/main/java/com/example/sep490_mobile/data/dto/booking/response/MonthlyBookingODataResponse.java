package com.example.sep490_mobile.data.dto.booking.response;

import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;

// DTO này CHỈ dùng để nhận response từ API /monthlyBooking
public class MonthlyBookingODataResponse {
    @SerializedName("value")
    private List<MonthlyBookingReadDTO> value;

    public List<MonthlyBookingReadDTO> getValue() {
        return value;
    }

    public void setValue(List<MonthlyBookingReadDTO> value) {
        this.value = value;
    }
}