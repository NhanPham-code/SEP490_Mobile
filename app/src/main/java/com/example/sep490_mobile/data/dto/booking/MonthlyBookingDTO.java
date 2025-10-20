package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MonthlyBookingDTO implements IBookingHistoryItem {
    @SerializedName("monthlyBooking") private MonthlyBookingReadDTO monthlyBooking;
    @SerializedName("bookings") private List<BookingReadDTO> bookings;

    public MonthlyBookingDTO(MonthlyBookingReadDTO monthlyBooking, List<BookingReadDTO> bookings) {
        this.monthlyBooking = monthlyBooking;
        this.bookings = bookings;
    }

    // Triển khai "hợp đồng"
    @Override
    public String getStadiumName() { return monthlyBooking != null ? monthlyBooking.getStadiumName() : "Không có"; }
    @Override
    public String getStatus() { return monthlyBooking != null ? monthlyBooking.getStatus() : "Không có"; }
    @Override
    public double getTotalPrice() { return monthlyBooking != null && monthlyBooking.getTotalPrice() != null ? monthlyBooking.getTotalPrice() : 0.0; }
    @Override
    public double getOriginalPrice() { return monthlyBooking != null && monthlyBooking.getOriginalPrice() != null ? monthlyBooking.getOriginalPrice() : getTotalPrice(); }
    @Override
    public Integer getDiscountId() { return monthlyBooking != null ? monthlyBooking.getDiscountId() : null; }
    @Override
    public List<BookingReadDTO> getBookingItems() { return bookings; }

    // Parcelable
    protected MonthlyBookingDTO(Parcel in) {
        monthlyBooking = in.readParcelable(MonthlyBookingReadDTO.class.getClassLoader());
        bookings = in.createTypedArrayList(BookingReadDTO.CREATOR);
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(monthlyBooking, flags);
        dest.writeTypedList(bookings);
    }
    @Override
    public int describeContents() { return 0; }
    public static final Creator<MonthlyBookingDTO> CREATOR = new Creator<MonthlyBookingDTO>() {
        @Override
        public MonthlyBookingDTO createFromParcel(Parcel in) { return new MonthlyBookingDTO(in); }
        @Override
        public MonthlyBookingDTO[] newArray(int size) { return new MonthlyBookingDTO[size]; }
    };

    public MonthlyBookingReadDTO getMonthlyBooking() { return monthlyBooking; }
    public void setMonthlyBooking(MonthlyBookingReadDTO monthlyBooking) { this.monthlyBooking = monthlyBooking; }
    public List<BookingReadDTO> getBookings() { return bookings; }
    public void setBookings(List<BookingReadDTO> bookings) { this.bookings = bookings; }
}