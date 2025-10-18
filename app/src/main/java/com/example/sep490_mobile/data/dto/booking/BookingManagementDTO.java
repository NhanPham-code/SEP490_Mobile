package com.example.sep490_mobile.data.dto.booking;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingManagementDTO {
    @SerializedName("dailyBookings")
    private List<DailyBookingDTO> dailyBookings;

    @SerializedName("monthlyBookings")
    private List<MonthlyBookingDTO> monthlyBookings;

    public BookingManagementDTO(List<DailyBookingDTO> dailyBookings, List<MonthlyBookingDTO> monthlyBookings) {
        this.dailyBookings = dailyBookings;
        this.monthlyBookings = monthlyBookings;
    }


    public List<DailyBookingDTO> getDailyBookings() {
        return dailyBookings;
    }

    public void setDailyBookings(List<DailyBookingDTO> dailyBookings) {
        this.dailyBookings = dailyBookings;
    }

    public List<MonthlyBookingDTO> getMonthlyBookings() {
        return monthlyBookings;
    }

    public void setMonthlyBookings(List<MonthlyBookingDTO> monthlyBookings) {
        this.monthlyBookings = monthlyBookings;
    }
}