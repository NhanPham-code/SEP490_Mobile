package com.example.sep490_mobile.ui.booking;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ScheduleCourtDTO;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleStadiumDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.BookingManagementDTO;
import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.repository.BookingRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryViewModel extends AndroidViewModel {

    private final BookingRepository repository;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<BookingManagementDTO> processedBookingHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BookingHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingRepository(application);
        sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
    }

    public LiveData<BookingManagementDTO> getProcessedBookingHistory() { return processedBookingHistory; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchAndProcessBookingHistory() {
        isLoading.setValue(true);

        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId <= 0) {
            Log.e("BookingHistoryVM", "User ID không hợp lệ, không thể tải lịch sử.");
            errorMessage.postValue("Không tìm thấy người dùng. Vui lòng đăng nhập lại.");
            isLoading.postValue(false);
            return;
        }

        final CountDownLatch latch = new CountDownLatch(2);
        final List<BookingReadDTO> allBookings = new ArrayList<>();
        final List<MonthlyBookingReadDTO> allMonthlyBookings = new ArrayList<>();

        repository.getBookingsHistory(userId).enqueue(new Callback<BookingHistoryODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    allBookings.addAll(response.body().getValue());
                }
                latch.countDown();
            }
            @Override public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) { latch.countDown(); }
        });

        repository.getMonthlyBookings(userId).enqueue(new Callback<MonthlyBookingODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Response<MonthlyBookingODataResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    allMonthlyBookings.addAll(response.body().getValue());
                }
                latch.countDown();
            }
            @Override public void onFailure(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Throwable t) { latch.countDown(); }
        });

        new Thread(() -> {
            try {
                latch.await();
                processStadiumsAndFinalize(allBookings, allMonthlyBookings);
            } catch (InterruptedException e) {
                errorMessage.postValue("Quá trình tải dữ liệu bị gián đoạn.");
                isLoading.postValue(false);
            }
        }).start();
    }

    private void processStadiumsAndFinalize(List<BookingReadDTO> allBookings, List<MonthlyBookingReadDTO> allMonthlyBookings) {
        Set<Integer> stadiumIds = new HashSet<>();
        allBookings.forEach(b -> stadiumIds.add(b.getStadiumId()));
        allMonthlyBookings.forEach(mb -> stadiumIds.add(mb.getStadiumId()));

        if (stadiumIds.isEmpty()) {
            processedBookingHistory.postValue(new BookingManagementDTO(new ArrayList<>(), new ArrayList<>()));
            isLoading.postValue(false);
            return;
        }

        repository.getStadiumsByIds(new ArrayList<>(stadiumIds)).enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Map<Integer, ScheduleStadiumDTO> stadiumLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumLookup.put(stadium.getId(), stadium);
                    }
                }
                finalizeData(allBookings, allMonthlyBookings, stadiumLookup);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                finalizeData(allBookings, allMonthlyBookings, new HashMap<>());
            }
        });
    }

    private void finalizeData(List<BookingReadDTO> allBookings, List<MonthlyBookingReadDTO> allMonthlyBookings, Map<Integer, ScheduleStadiumDTO> stadiumLookup) {
        Map<Integer, String> courtNameLookup = stadiumLookup.values().stream()
                .filter(s -> s.getCourts() != null)
                .flatMap(s -> s.getCourts().stream())
                .collect(Collectors.toMap(ScheduleCourtDTO::getId, ScheduleCourtDTO::getName, (name1, name2) -> name1));

        allBookings.forEach(booking -> {
            ScheduleStadiumDTO stadium = stadiumLookup.get(booking.getStadiumId());
            if (stadium != null) {
                booking.setStadiumName(stadium.getName());
            }
            if (booking.getBookingDetails() != null) {
                booking.getBookingDetails().forEach(detail -> {
                    detail.setCourtName(courtNameLookup.getOrDefault(detail.getCourtId(), "Sân " + detail.getCourtId()));
                });
            }
        });

        allMonthlyBookings.forEach(mb -> {
            ScheduleStadiumDTO stadium = stadiumLookup.get(mb.getStadiumId());
            if (stadium != null) {
                mb.setStadiumName(stadium.getName());
            }
        });

        List<DailyBookingDTO> dailyList = allBookings.stream()
                .filter(b -> b.getMonthlyBookingId() == null)
                .map(DailyBookingDTO::new)
                .collect(Collectors.toList());

        Map<Integer, List<BookingReadDTO>> bookingsInPlan = allBookings.stream()
                .filter(b -> b.getMonthlyBookingId() != null)
                .collect(Collectors.groupingBy(BookingReadDTO::getMonthlyBookingId));

        List<MonthlyBookingDTO> monthlyList = allMonthlyBookings.stream()
                .map(mb -> new MonthlyBookingDTO(mb, bookingsInPlan.getOrDefault(mb.getId(), new ArrayList<>())))
                .collect(Collectors.toList());

        BookingManagementDTO finalData = new BookingManagementDTO(dailyList, monthlyList);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonData = gson.toJson(finalData);
        Log.d("BookingHistoryVM_Data", "Dữ liệu cuối cùng:\n" + jsonData);

        processedBookingHistory.postValue(finalData);
        isLoading.postValue(false);
    }
}