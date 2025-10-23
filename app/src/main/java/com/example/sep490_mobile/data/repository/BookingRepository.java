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

    /**
     * Lấy lịch sử đặt sân THEO NGÀY (không thuộc gói tháng) theo trang.
     * @param userId ID người dùng
     * @param page Trang hiện tại (bắt đầu từ 1)
     * @param pageSize Số lượng item mỗi trang
     * @return Call object
     */
    public Call<BookingHistoryODataResponse> getDailyBookingsHistory(int userId, int page, int pageSize) {
        String filter = String.format(Locale.US, "UserId eq %d and MonthlyBookingId eq null", userId);
        String orderBy = "Date desc";
        int skip = (page - 1) * pageSize;
        int top = pageSize;
        // Truyền true cho count
        return apiService.getBookingsHistory(filter, orderBy, true, skip, top);
    }

    public Call<MonthlyBookingODataResponse> getMonthlyBookingsHistory(int userId, int page, int pageSize) {
        String filter = String.format(Locale.US, "UserId eq %d", userId);
        String orderBy = "Year desc, Month desc";
        int skip = (page - 1) * pageSize;
        int top = pageSize;
        // Truyền true cho count
        return apiService.getMonthlyBookings(filter, orderBy, true, skip, top);
    }

    // Sử dụng lại DTO có sẵn cho Stadium
    public Call<ScheduleODataStadiumResponseDTO> getStadiumsByIds(List<Integer> stadiumIds) {
        String filter = stadiumIds.stream()
                .map(id -> "Id eq " + id)
                .collect(Collectors.joining(" or "));
        return apiService.getStadiums(filter, "Courts");
    }
}