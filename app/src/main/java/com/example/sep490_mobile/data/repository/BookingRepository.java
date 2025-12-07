package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.BookingSlotRequest;
import com.example.sep490_mobile.data.dto.MonthlyBookingCreateDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.booking.BookingUpdateDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingUpdateDTO;
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
     * Lấy lịch sử đặt sân THEO NGÀY (booking thường, không thuộc gói tháng)
     */
    public Call<BookingHistoryODataResponse> getDailyBookingsHistory(int userId, int page, int pageSize) {
        String filter = String.format(Locale.US, "UserId eq %d and MonthlyBookingId eq null", userId);
        String orderBy = "CreatedAt desc";
        int skip = (page - 1) * pageSize;
        int top = pageSize;
        return apiService.getBookingsHistory(filter, orderBy, true, skip, top);
    }

    /**
     * Lấy danh sách gói đặt sân theo tháng (monthly booking)
     */
    public Call<MonthlyBookingODataResponse> getMonthlyBookingsHistory(int userId, int page, int pageSize) {
        String filter = String.format(Locale.US, "UserId eq %d", userId);
        String orderBy = "CreatedAt desc";
        int skip = (page - 1) * pageSize;
        int top = pageSize;
        return apiService.getMonthlyBookings(filter, orderBy, true, skip, top);
    }

    /**
     * Lấy chi tiết 1 monthly booking cụ thể theo ID.
     * (Trả về ODataResponse, ViewModel sẽ tự xử lý)
     */
    public Call<MonthlyBookingODataResponse> getMonthlyBookingById(int monthlyBookingId) {
        String filter = String.format(Locale.US, "Id eq %d", monthlyBookingId);
        // <<< SỬA: Chỗ này phải trả về MonthlyBookingODataResponse,
        // vì ApiService getMonthlyBookings trả về kiểu đó
        return apiService.getMonthlyBookings(filter, null, false, 0, 1);
    }

    /**
     * Lấy danh sách sân theo ID (tái sử dụng DTO Stadium)
     */
    public Call<ScheduleODataStadiumResponseDTO> getStadiumsByIds(List<Integer> stadiumIds) {
        String filter = stadiumIds.stream()
                .map(id -> "Id eq " + id)
                .collect(Collectors.joining(" or "));
        return apiService.getStadiums(filter, "Courts");
    }

    /**
     * Lấy danh sách booking con thuộc một gói đặt tháng (dùng cho việc update tất cả booking con)
     */
    public Call<BookingHistoryODataResponse> getBookingsForMonthlyPlan(int monthlyBookingId) {
        String filter = String.format(Locale.US, "MonthlyBookingId eq %d", monthlyBookingId);
        String orderBy = "Date asc";
        return apiService.getBookingsForMonthlyPlan(filter, orderBy);
    }

    /**
     * Tạo booking mới (đặt sân lẻ)
     */
    public Call<BookingReadDto> createBooking(BookingCreateDto bookingRequestDto) {
        return apiService.createBooking(bookingRequestDto);
    }

    /**
     * Tạo booking mới (gói tháng)
     */
    public Call<BookingReadDto> createMonthlyBooking(MonthlyBookingCreateDto monthlyBookingDto) {
        return apiService.createMonthlyBooking(monthlyBookingDto);
    }

    /**
     * Lấy booking theo OData filter (thường dùng khi xem chi tiết)
     */
    public Call<BookingHistoryODataResponse> getBookingByODataFilter(int id) {
        String filter = String.format(Locale.US, "Id eq %d", id);
        return apiService.getBookingByODataFilter(filter);
    }

    /**
     * Cập nhật booking (single booking)
     */
    public Call<BookingReadDto> updateBooking(int id, BookingUpdateDTO bookingUpdateDTO) {
        return apiService.updateBooking(id, bookingUpdateDTO);
    }

    /**
     * Cập nhật monthly booking (gói tháng)
     * <<< SỬA: Chỗ này phải trả về MonthlyBookingReadDTO theo logic VM,
     * nhưng ApiService trả về Void. Tạm thời giữ Void.
     */
    // Sửa hàm này
    public Call<MonthlyBookingReadDTO> updateMonthlyBooking(int id, MonthlyBookingUpdateDTO monthlyBookingUpdateDTO) {
        return apiService.updateMonthlyBooking(id, monthlyBookingUpdateDTO);
    }

    // === CÁC HÀM BỔ SUNG CHO VIEWMODEL ===

    /**
     * (BỔ SUNG) Lấy các sân đã đặt theo ngày
     */
    public Call<ODataResponse<BookingReadDto>> getBookedCourtsByDay(String filter, String expand) {
        return apiService.getBookedCourtsByDay(filter, expand);
    }

    /**
     * (BỔ SUNG) Lấy sân cha
     */
    public Call<List<ReadCourtRelationDTO>> getAllCourtRelationByParentId(int parentId) {
        return apiService.getAllCourtRelationByParentId(parentId);
    }

    /**
     * (BỔ SUNG) Lấy sân con
     */
    public Call<List<ReadCourtRelationDTO>> getAllCourtRelationByChildId(int childId) {
        return apiService.getAllCourtRelationByChildId(childId);
    }

    /**
     * (BỔ SUNG) Kiểm tra slot có sẵn
     */
    public Call<Void> checkSlotsAvailability(List<BookingSlotRequest> requestedSlots) {
        return apiService.checkSlotsAvailability(requestedSlots);
    }

    // <<< TÔI SẼ COMMENT CÁI NÀY VÌ APISERVICE KHÔNG CÓ NÓ >>>
    // public Call<MonthlyBookingReadDTO> getSingleMonthlyBookingById(int monthlyBookingId) {
    //     // Giả định ApiService có hàm: @GET("monthlyBooking/{id}")
    //     // return apiService.getSingleMonthlyBookingById(monthlyBookingId);
    // }
}
