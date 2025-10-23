package com.example.sep490_mobile.ui.booking;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleStadiumDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.repository.BookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryViewModel extends AndroidViewModel {

    // --- Constants ---
    private static final String TAG = "BookingHistoryVM_Log";
    private static final int PAGE_SIZE = 5;
    public enum BookingType { DAILY, MONTHLY }

    // --- Repositories and SharedPreferences ---
    private final BookingRepository repository;
    private final SharedPreferences sharedPreferences;

    // --- LiveData ---
    private final MutableLiveData<List<DailyBookingDTO>> dailyBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<MonthlyBookingDTO>> monthlyBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);

    // --- Pagination State ---
    private int currentDailyPage = 1;
    private int totalDailyCount = 0;
    private boolean isFetchingDaily = false;

    private int currentMonthlyPage = 1;
    private int totalMonthlyCount = 0;
    private boolean isFetchingMonthly = false;

    // <<< ADDED: Temporary storage for newly fetched items >>>
    private List<DailyBookingDTO> lastFetchedDaily = null;
    private List<MonthlyBookingDTO> lastFetchedMonthly = null;


    public BookingHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingRepository(application);
        sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Log.d(TAG, "ViewModel initialized");
    }

    // --- Getters ---
    public LiveData<List<DailyBookingDTO>> getDailyBookings() { return dailyBookings; }
    public LiveData<List<MonthlyBookingDTO>> getMonthlyBookings() { return monthlyBookings; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }

    // <<< ADDED: Getters for newly fetched items >>>
    public List<DailyBookingDTO> getLastFetchedDaily() { return lastFetchedDaily; }
    public List<MonthlyBookingDTO> getLastFetchedMonthly() { return lastFetchedMonthly; }


    // --- Public Methods ---
    public void fetchInitialBookings(BookingType type) {
        Log.i(TAG, "fetchInitialBookings called for type: " + type);
        isLoading.setValue(true);
        errorMessage.setValue(null);
        isLastPage.setValue(false);
        lastFetchedDaily = null; // Reset temp list
        lastFetchedMonthly = null; // Reset temp list

        if (type == BookingType.DAILY) {
            currentDailyPage = 1;
            totalDailyCount = 0;
            isFetchingDaily = false;
            dailyBookings.setValue(new ArrayList<>());
            fetchDailyBookingsPage();
        } else {
            currentMonthlyPage = 1;
            totalMonthlyCount = 0;
            isFetchingMonthly = false;
            monthlyBookings.setValue(new ArrayList<>());
            fetchMonthlyBookingsPage();
        }
    }

    public void fetchMoreBookings(BookingType type) {
        Log.i(TAG, "fetchMoreBookings called for type: " + type);
        errorMessage.setValue(null);
        lastFetchedDaily = null; // Reset temp list before fetching more
        lastFetchedMonthly = null; // Reset temp list before fetching more

        if (type == BookingType.DAILY) {
            boolean canLoadMore = !isFetchingDaily && (currentDailyPage * PAGE_SIZE < totalDailyCount);
            if (!canLoadMore) {
                Log.d(TAG, "fetchMoreBookings (Daily): Cannot load more.");
                return;
            }
            currentDailyPage++;
            fetchDailyBookingsPage();
        } else {
            boolean canLoadMore = !isFetchingMonthly && (currentMonthlyPage * PAGE_SIZE < totalMonthlyCount);
            if (!canLoadMore) {
                Log.d(TAG, "fetchMoreBookings (Monthly): Cannot load more.");
                return;
            }
            currentMonthlyPage++;
            fetchMonthlyBookingsPage();
        }
    }

    // --- Private Fetching ---
    private void fetchDailyBookingsPage() {
        if (isFetchingDaily) return;
        isFetchingDaily = true;
        if (currentDailyPage == 1) {
            isLoading.setValue(true);
        }
        Log.i(TAG, "Fetching DAILY bookings page: " + currentDailyPage);

        int userId = sharedPreferences.getInt("user_id", -1);
        if (!isValidUserId(userId)) {
            isFetchingDaily = false; return;
        }

        repository.getDailyBookingsHistory(userId, currentDailyPage, PAGE_SIZE)
                .enqueue(new Callback<BookingHistoryODataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                        Log.d(TAG, "fetchDailyBookingsPage: onResponse - Code: " + response.code() + " for page " + currentDailyPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<BookingReadDTO> fetchedData = response.body().getValue();
                            totalDailyCount = response.body().getCount();
                            Log.i(TAG, "fetchDailyBookingsPage: Success! Fetched " + fetchedData.size() + ". Total daily: " + totalDailyCount);

                            boolean isLast = (currentDailyPage * PAGE_SIZE >= totalDailyCount);
                            isLastPage.postValue(isLast);
                            Log.i(TAG, "fetchDailyBookingsPage: Is last page? " + isLast);

                            processStadiumAndCourtNamesForDaily(fetchedData);
                        } else {
                            handleApiError("Lỗi tải lịch đặt ngày", response.code(), BookingType.DAILY);
                            isLastPage.postValue(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải lịch đặt ngày", t, BookingType.DAILY);
                        isLastPage.postValue(true);
                    }
                });
    }

    private void fetchMonthlyBookingsPage() {
        if (isFetchingMonthly) return;
        isFetchingMonthly = true;
        if (currentMonthlyPage == 1) {
            isLoading.setValue(true);
        }
        Log.i(TAG, "Fetching MONTHLY bookings page: " + currentMonthlyPage);

        int userId = sharedPreferences.getInt("user_id", -1);
        if (!isValidUserId(userId)) {
            isFetchingMonthly = false; return;
        }

        repository.getMonthlyBookingsHistory(userId, currentMonthlyPage, PAGE_SIZE)
                .enqueue(new Callback<MonthlyBookingODataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Response<MonthlyBookingODataResponse> response) {
                        Log.d(TAG, "fetchMonthlyBookingsPage: onResponse - Code: " + response.code() + " for page " + currentMonthlyPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<MonthlyBookingReadDTO> fetchedData = response.body().getValue();
                            totalMonthlyCount = response.body().getCount();
                            Log.i(TAG, "fetchMonthlyBookingsPage: Success! Fetched " + fetchedData.size() + ". Total monthly: " + totalMonthlyCount);

                            boolean isLast = (currentMonthlyPage * PAGE_SIZE >= totalMonthlyCount);
                            isLastPage.postValue(isLast);
                            Log.i(TAG, "fetchMonthlyBookingsPage: Is last page? " + isLast);

                            processStadiumNamesForMonthly(fetchedData);
                        } else {
                            handleApiError("Lỗi tải lịch đặt tháng", response.code(), BookingType.MONTHLY);
                            isLastPage.postValue(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải lịch đặt tháng", t, BookingType.MONTHLY);
                        isLastPage.postValue(true);
                    }
                });
    }

    // --- Processing Names ---
    private void processStadiumAndCourtNamesForDaily(List<BookingReadDTO> bookings) {
        Log.d(TAG, "processStadiumAndCourtNamesForDaily: Processing " + bookings.size() + " bookings.");
        if (bookings.isEmpty()) {
            finalizeDailyUpdate(bookings, new HashMap<>(), new HashMap<>());
            return;
        }
        Set<Integer> stadiumIds = bookings.stream().map(BookingReadDTO::getStadiumId).collect(Collectors.toSet());
        Log.d(TAG, "processStadiumAndCourtNamesForDaily: Stadium IDs: " + stadiumIds);
        Call<ScheduleODataStadiumResponseDTO> stadiumCall = repository.getStadiumsByIds(new ArrayList<>(stadiumIds));
        if (stadiumCall == null) {
            Log.w(TAG, "processStadiumAndCourtNamesForDaily: No stadium IDs, finalizing without names.");
            finalizeDailyUpdate(bookings, new HashMap<>(), new HashMap<>());
            return;
        }
        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Log.d(TAG, "getStadiumsByIds (Daily): onResponse - Code: " + response.code());
                Map<Integer, String> stadiumNameLookup = new HashMap<>();
                Map<Integer, String> courtNameLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumNameLookup.put(stadium.getId(), stadium.getName());
                        // Populate court names IF expand=Courts was used
                        // if (stadium.getCourts() != null) { ... }
                    }
                    Log.d(TAG, "getStadiumsByIds (Daily): Success! Stadium map size: " + stadiumNameLookup.size());
                } else {
                    Log.e(TAG, "getStadiumsByIds (Daily): Error! Code: " + response.code());
                }
                finalizeDailyUpdate(bookings, stadiumNameLookup, courtNameLookup);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "getStadiumsByIds (Daily): onFailure - Error: " + t.getMessage());
                finalizeDailyUpdate(bookings, new HashMap<>(), new HashMap<>());
            }
        });
    }

    private void processStadiumNamesForMonthly(List<MonthlyBookingReadDTO> monthlyBookingsList) {
        Log.d(TAG, "processStadiumNamesForMonthly: Processing " + monthlyBookingsList.size() + " bookings.");
        if (monthlyBookingsList.isEmpty()) {
            finalizeMonthlyUpdate(monthlyBookingsList, new HashMap<>());
            return;
        }
        Set<Integer> stadiumIds = monthlyBookingsList.stream().map(MonthlyBookingReadDTO::getStadiumId).collect(Collectors.toSet());
        Log.d(TAG, "processStadiumNamesForMonthly: Stadium IDs: " + stadiumIds);
        Call<ScheduleODataStadiumResponseDTO> stadiumCall = repository.getStadiumsByIds(new ArrayList<>(stadiumIds));
        if (stadiumCall == null) {
            Log.w(TAG, "processStadiumNamesForMonthly: No stadium IDs, finalizing without names.");
            finalizeMonthlyUpdate(monthlyBookingsList, new HashMap<>());
            return;
        }
        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Log.d(TAG, "getStadiumsByIds (Monthly): onResponse - Code: " + response.code());
                Map<Integer, String> stadiumNameLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumNameLookup.put(stadium.getId(), stadium.getName());
                    }
                    Log.d(TAG, "getStadiumsByIds (Monthly): Success! Stadium map size: " + stadiumNameLookup.size());
                } else {
                    Log.e(TAG, "getStadiumsByIds (Monthly): Error! Code: " + response.code());
                }
                finalizeMonthlyUpdate(monthlyBookingsList, stadiumNameLookup);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "getStadiumsByIds (Monthly): onFailure - Error: " + t.getMessage());
                finalizeMonthlyUpdate(monthlyBookingsList, new HashMap<>());
            }
        });
    }

    // --- Finalize and Update ---
    private void finalizeDailyUpdate(List<BookingReadDTO> newBookingsFromApi, Map<Integer, String> stadiumNameLookup, Map<Integer, String> courtNameLookup) {
        Log.i(TAG, "finalizeDailyUpdate: Processing " + newBookingsFromApi.size() + " new daily bookings.");
        List<DailyBookingDTO> newDailyList = new ArrayList<>(); // List of NEW items for this page
        for (BookingReadDTO booking : newBookingsFromApi) {
            booking.setStadiumName(stadiumNameLookup.getOrDefault(booking.getStadiumId(), "Sân ID: " + booking.getStadiumId()));
            if (booking.getBookingDetails() != null) {
                booking.getBookingDetails().forEach(detail ->
                        detail.setCourtName(courtNameLookup.getOrDefault(detail.getCourtId(), "Sân con ID: " + detail.getCourtId()))
                );
            }
            newDailyList.add(new DailyBookingDTO(booking));
        }

        // <<< Store the NEWLY processed items >>>
        lastFetchedDaily = new ArrayList<>(newDailyList);

        // Get current full list, append new items, post the combined list
        List<DailyBookingDTO> currentList = dailyBookings.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(newDailyList);
        dailyBookings.postValue(currentList);
        Log.i(TAG, "finalizeDailyUpdate: Posted COMBINED Daily bookings. New total size: " + currentList.size());

        isLoading.postValue(false);
        isFetchingDaily = false;
    }

    private void finalizeMonthlyUpdate(List<MonthlyBookingReadDTO> newMonthlyBookingsFromApi, Map<Integer, String> stadiumNameLookup) {
        Log.i(TAG, "finalizeMonthlyUpdate: Processing " + newMonthlyBookingsFromApi.size() + " new monthly bookings.");
        List<MonthlyBookingDTO> newMonthlyList = new ArrayList<>(); // List of NEW items
        for (MonthlyBookingReadDTO mb : newMonthlyBookingsFromApi) {
            mb.setStadiumName(stadiumNameLookup.getOrDefault(mb.getStadiumId(), "Sân ID: " + mb.getStadiumId()));
            newMonthlyList.add(new MonthlyBookingDTO(mb, Collections.emptyList()));
        }

        // <<< Store the NEWLY processed items >>>
        lastFetchedMonthly = new ArrayList<>(newMonthlyList);

        List<MonthlyBookingDTO> currentList = monthlyBookings.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(newMonthlyList); // Append new data
        monthlyBookings.postValue(currentList); // Post combined list
        Log.i(TAG, "finalizeMonthlyUpdate: Posted COMBINED Monthly bookings. New total size: " + currentList.size());

        isLoading.postValue(false);
        isFetchingMonthly = false;
    }

    // --- Utilities ---
    private boolean isValidUserId(int userId) {
        if (userId <= 0) {
            Log.e(TAG, "Invalid User ID: " + userId + ". Cannot fetch history.");
            errorMessage.postValue("Không tìm thấy người dùng. Vui lòng đăng nhập lại.");
            isLoading.postValue(false);
            return false;
        }
        return true;
    }

    private void handleApiError(String contextMessage, int code, BookingType type) {
        Log.e(TAG, contextMessage + " - Code: " + code);
        errorMessage.postValue(contextMessage + ": " + code);
        isLoading.postValue(false);
        if (type == BookingType.DAILY) isFetchingDaily = false;
        else isFetchingMonthly = false;
    }

    private void handleNetworkError(String contextMessage, Throwable t, BookingType type) {
        Log.e(TAG, contextMessage + " - Error: " + t.getMessage(), t);
        errorMessage.postValue(contextMessage + ": " + t.getMessage());
        isLoading.postValue(false);
        if (type == BookingType.DAILY) isFetchingDaily = false;
        else isFetchingMonthly = false;
    }
}