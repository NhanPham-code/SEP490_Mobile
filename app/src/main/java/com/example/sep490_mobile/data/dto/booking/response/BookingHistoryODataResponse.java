package com.example.sep490_mobile.data.dto.booking.response;

import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;

// DTO này CHỈ dùng để nhận response từ API /bookings/history
public class BookingHistoryODataResponse {


    @SerializedName("@odata.count")
    private int count;


    @SerializedName("value")
    private List<BookingReadDTO> value;


    public int getCount() {
        return count;
    }

    public List<BookingReadDTO> getValue() {
        return value;
    }

    // Setter không cần thiết cho count
    public void setValue(List<BookingReadDTO> value) {
        this.value = value;
    }
}