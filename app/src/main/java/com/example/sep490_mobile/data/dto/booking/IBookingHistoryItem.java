package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcelable;
import java.util.List;

public interface IBookingHistoryItem extends Parcelable {
    String getStadiumName();
    String getStatus();

    // Cập nhật các phương thức về giá
    double getTotalPrice();
    double getOriginalPrice();
    Integer getDiscountId();

    List<BookingViewDTO> getBookingItems();
}