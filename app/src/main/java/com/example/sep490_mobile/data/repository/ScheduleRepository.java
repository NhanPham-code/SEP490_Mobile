package com.example.sep490_mobile.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDetailDTO;
import com.example.sep490_mobile.data.dto.ScheduleCourtDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingODataResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleStadiumDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleRepository {
    private ApiService apiService;
    private static volatile ScheduleRepository instance;
    private static final String TAG = "ScheduleRepository";

    private ScheduleRepository(Context context) {
        this.apiService = ApiClient.getInstance(context.getApplicationContext()).getApiService();
    }

    public static ScheduleRepository getInstance(Context context) {
        if (instance == null) instance = new ScheduleRepository(context);
        return instance;
    }

    public LiveData<List<ScheduleBookingDTO>> getBookings(String filter) {
        MutableLiveData<List<ScheduleBookingDTO>> finalData = new MutableLiveData<>();
        Log.d(TAG, "Bước 1: Đang gọi API getBookings với filter: " + filter);

        // 1. GỌI API LẤY BOOKING
        apiService.getBookings(filter).enqueue(new Callback<ScheduleBookingODataResponseDTO>() {
            @Override
            public void onResponse(Call<ScheduleBookingODataResponseDTO> call, Response<ScheduleBookingODataResponseDTO> bookingResponse) {
                if (bookingResponse.isSuccessful() && bookingResponse.body() != null) {
                    List<ScheduleBookingDTO> bookings = bookingResponse.body().getValue();
                    if (bookings != null && !bookings.isEmpty()) {
                        Log.d(TAG, "Bước 1 THÀNH CÔNG! Số lượng booking: " + bookings.size());
                        fetchStadiumsForBookings(bookings, finalData);
                    } else {
                        Log.d(TAG, "Bước 1: Không có booking nào.");
                        finalData.setValue(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "Bước 1 LỖI. Code: " + bookingResponse.code());
                    finalData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ScheduleBookingODataResponseDTO> call, Throwable t) {
                Log.e(TAG, "Bước 1 THẤT BẠI HOÀN TOÀN!", t);
                finalData.setValue(new ArrayList<>());
            }
        });
        return finalData;
    }

    private void fetchStadiumsForBookings(List<ScheduleBookingDTO> bookings, MutableLiveData<List<ScheduleBookingDTO>> finalData) {
        Set<Integer> stadiumIds = new HashSet<>();
        for (ScheduleBookingDTO booking : bookings) {
            stadiumIds.add(booking.getStadiumId());
        }

        if (stadiumIds.isEmpty()) {
            finalData.setValue(bookings);
            return;
        }

        StringBuilder filterBuilder = new StringBuilder("Id in (");
        int count = 0;
        for (Integer id : stadiumIds) {
            filterBuilder.append(id);
            if (++count < stadiumIds.size()) {
                filterBuilder.append(",");
            }
        }
        filterBuilder.append(")");
        String stadiumFilter = filterBuilder.toString();

        Log.d(TAG, "Bước 2: Đang gọi API getStadiums với filter: " + stadiumFilter);

        // 2. GỌI API LẤY STADIUM
        apiService.getStadiums(stadiumFilter, "Courts").enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(Call<ScheduleODataStadiumResponseDTO> call, Response<ScheduleODataStadiumResponseDTO> stadiumResponse) {
                if (stadiumResponse.isSuccessful() && stadiumResponse.body() != null && stadiumResponse.body().getValue() != null) {
                    Log.d(TAG, "Bước 2 THÀNH CÔNG!");
                    Map<Integer, ScheduleStadiumDTO> stadiumMap = new HashMap<>();
                    for (ScheduleStadiumDTO stadium : stadiumResponse.body().getValue()) {
                        stadiumMap.put(stadium.getId(), stadium);
                    }
                    enrichBookingData(bookings, stadiumMap, finalData);
                } else {
                    Log.e(TAG, "Bước 2 LỖI. Code: " + stadiumResponse.code());
                    finalData.setValue(bookings);
                }
            }

            @Override
            public void onFailure(Call<ScheduleODataStadiumResponseDTO> call, Throwable t) {
                Log.e(TAG, "Bước 2 THẤT BẠI HOÀN TOÀN!", t);
                finalData.setValue(bookings);
            }
        });
    }

    private void enrichBookingData(List<ScheduleBookingDTO> bookings, Map<Integer, ScheduleStadiumDTO> stadiumMap, MutableLiveData<List<ScheduleBookingDTO>> finalData) {
        Log.d(TAG, "Bước 3: Bắt đầu gộp dữ liệu...");

        for (ScheduleBookingDTO booking : bookings) {
            ScheduleStadiumDTO stadium = stadiumMap.get(booking.getStadiumId());
            if (stadium != null) {
                booking.setStadiumName(stadium.getName());

                if (booking.getBookingDetails() != null && stadium.getCourts() != null) {
                    // Tạo map tra cứu cho sân con của sân vận động này
                    Map<Integer, ScheduleCourtDTO> courtMap = new HashMap<>();
                    for (ScheduleCourtDTO court : stadium.getCourts()) {
                        courtMap.put(court.getId(), court);
                    }

                    // Lặp qua các chi tiết và gán tên + giá
                    for (ScheduleBookingDetailDTO detail : booking.getBookingDetails()) {
                        ScheduleCourtDTO foundCourt = courtMap.get(detail.getCourtId());
                        if (foundCourt != null) {
                            detail.setCourtName(foundCourt.getName());
                            detail.setPricePerHour(foundCourt.getPricePerHour());
                        } else {
                            detail.setCourtName("Sân " + detail.getCourtId());
                            detail.setPricePerHour(0); // Không tìm thấy giá
                        }
                    }
                }
            } else {
                booking.setStadiumName("Sân ID: " + booking.getStadiumId());
            }
        }

        finalData.setValue(bookings);
        Log.d(TAG, "Bước 3 HOÀN TẤT!");
    }
}