package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import java.util.Collections;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DailyBookingDTO implements IBookingHistoryItem {
    @SerializedName("booking") private BookingViewDTO booking;

    public DailyBookingDTO(BookingViewDTO booking) { this.booking = booking; }

    // Triển khai "hợp đồng"
    @Override
    public String getStadiumName() { return booking != null ? booking.getStadiumName() : "Không có"; }
    @Override
    public String getStatus() { return booking != null ? booking.getStatus() : "Không có"; }
    @Override
    public double getTotalPrice() { return booking != null && booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0; }
    @Override
    public double getOriginalPrice() { return booking != null && booking.getOriginalPrice() != null ? booking.getOriginalPrice() : getTotalPrice(); } // Nếu không có giá gốc thì bằng giá cuối
    @Override
    public Integer getDiscountId() { return booking != null ? booking.getDiscountId() : null; }
    @Override
    public List<BookingViewDTO> getBookingItems() { return booking != null ? Collections.singletonList(booking) : Collections.emptyList(); }

    // Parcelable
    protected DailyBookingDTO(Parcel in) { booking = in.readParcelable(BookingViewDTO.class.getClassLoader()); }
    @Override
    public void writeToParcel(Parcel dest, int flags) { dest.writeParcelable(booking, flags); }
    @Override
    public int describeContents() { return 0; }
    public static final Creator<DailyBookingDTO> CREATOR = new Creator<DailyBookingDTO>() {
        @Override
        public DailyBookingDTO createFromParcel(Parcel in) { return new DailyBookingDTO(in); }
        @Override
        public DailyBookingDTO[] newArray(int size) { return new DailyBookingDTO[size]; }
    };

    public BookingViewDTO getBooking() { return booking; }
    public void setBooking(BookingViewDTO booking) { this.booking = booking; }
}