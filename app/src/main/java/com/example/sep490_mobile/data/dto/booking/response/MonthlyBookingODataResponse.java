package com.example.sep490_mobile.data.dto.booking.response;

import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MonthlyBookingODataResponse {

    @SerializedName("@odata.count")
    private int count;

    @SerializedName("value")
    private List<MonthlyBookingReadDTO> value;


    public int getCount() {
        return count;
    }

    public List<MonthlyBookingReadDTO> getValue() {
        return value;
    }

    public void setValue(List<MonthlyBookingReadDTO> value) {
        this.value = value;
    }
}