package com.example.sep490_mobile.data.repository;

import android.content.Context;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO; // SỬ DỤNG LẠI
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import retrofit2.Call;

public class BookingRepository {
    private final ApiService apiService;

    public BookingRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    // UserId là kiểu int, giống như trong ScheduleViewModel của bạn
    public Call<BookingHistoryODataResponse> getBookingsHistory(int userId) {
        String filter = String.format(Locale.US, "UserId eq %d", userId);
        return apiService.getBookingsHistory(filter);
    }

    // UserId là kiểu int
    public Call<MonthlyBookingODataResponse> getMonthlyBookings(int userId) {
        String filter = String.format(Locale.US, "UserId eq %d", userId);
        return apiService.getMonthlyBookings(filter);
    }

    // Sử dụng lại DTO có sẵn cho Stadium
    public Call<ScheduleODataStadiumResponseDTO> getStadiumsByIds(List<Integer> stadiumIds) {
        String filter = stadiumIds.stream()
                .map(id -> "Id eq " + id)
                .collect(Collectors.joining(" or "));
        return apiService.getStadiums(filter, "Courts");
    }
}