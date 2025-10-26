package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.sep490_mobile.data.dto.BookingDetailDTO;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.model.CourtDisplayItem;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
// SỬA 1: Đổi import
import com.example.sep490_mobile.model.DailyBookingSummary;
import com.example.sep490_mobile.model.TimeZone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailyBookingViewModel extends AndroidViewModel {

    interface CourtRelationsCallback {
        void onComplete(Set<Integer> relationIds);
    }

    private final ApiService apiService;

    private final MutableLiveData<StadiumDTO> _stadium = new MutableLiveData<>();
    public LiveData<StadiumDTO> stadium = _stadium;

    private final MutableLiveData<LocalDate> _selectedDate = new MutableLiveData<>();
    public LiveData<LocalDate> selectedDate = _selectedDate;

    public final MediatorLiveData<List<TimeZone>> timeZones = new MediatorLiveData<>();
    private final MutableLiveData<Integer> _startTime = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> _endTime = new MutableLiveData<>(null);

    public LiveData<Boolean> isCourtSelectionEnabled = Transformations.map(_endTime, end -> end != null);

    private final MutableLiveData<List<CourtDisplayItem>> _courtList = new MutableLiveData<>();
    public LiveData<List<CourtDisplayItem>> courtList = _courtList;
    private final MediatorLiveData<Set<Integer>> _bookedCourtIds = new MediatorLiveData<>();
    public LiveData<Set<Integer>> bookedCourtIds = _bookedCourtIds;
    private final MutableLiveData<Set<Integer>> _selectedCourtIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> selectedCourtIds = _selectedCourtIds;

    private final MutableLiveData<Set<Integer>> _bookedCourtRelationIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> bookedCourtRelationIds = _bookedCourtRelationIds;
    private final MutableLiveData<Set<Integer>> _selectedCourtRelationIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> selectedCourtRelationIds = _selectedCourtRelationIds;

    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToLogin = _navigateToLogin;
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;
    private final MutableLiveData<Bundle> _navigateToCheckout = new MutableLiveData<>();
    public LiveData<Bundle> navigateToCheckout = _navigateToCheckout;

    // SỬA 2: Đổi kiểu dữ liệu
    private final MediatorLiveData<DailyBookingSummary> _bookingSummary = new MediatorLiveData<>();
    // SỬA 3: Đổi kiểu dữ liệu
    public LiveData<DailyBookingSummary> bookingSummary = _bookingSummary;

    public DailyBookingViewModel(@NonNull Application application) {
        super(application);
        this.apiService = ApiClient.getInstance(application.getApplicationContext()).getApiService();

        MediatorLiveData<Object> timeZoneTrigger = new MediatorLiveData<>();
        timeZoneTrigger.addSource(_stadium, value -> timeZoneTrigger.setValue(value));
        timeZoneTrigger.addSource(_selectedDate, value -> timeZoneTrigger.setValue(value));
        timeZoneTrigger.addSource(_startTime, value -> timeZoneTrigger.setValue(value));
        timeZoneTrigger.addSource(_endTime, value -> timeZoneTrigger.setValue(value));
        timeZones.addSource(timeZoneTrigger, value -> updateAvailableTimeZones());

        _bookedCourtIds.addSource(_stadium, s -> filterBookedCourts());
        _bookedCourtIds.addSource(_selectedDate, date -> filterBookedCourts());
        _bookedCourtIds.addSource(_endTime, end -> filterBookedCourts());

        _bookingSummary.addSource(_selectedDate, date -> updateBookingSummary());
        _bookingSummary.addSource(_startTime, start -> updateBookingSummary());
        _bookingSummary.addSource(_endTime, end -> updateBookingSummary());
        _bookingSummary.addSource(_selectedCourtIds, courts -> updateBookingSummary());
        _bookingSummary.addSource(_stadium, s -> updateBookingSummary());
    }

    public void fetchStadiumData(String stadiumId) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("$filter", "Id eq " + stadiumId);
        options.put("$expand", "Courts");
        apiService.getStadiumsOdata(options).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                    StadiumDTO fetchedStadium = response.body().getItems().get(0);
                    _stadium.postValue(fetchedStadium);
                    processCourtList(fetchedStadium.getCourts());
                } else {
                    _toastMessage.postValue("Lỗi tải dữ liệu sân.");
                }
            }
            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) {
                _toastMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void processCourtList(Set<CourtsDTO> courts) {
        if (courts == null || courts.isEmpty()) {
            _courtList.postValue(new ArrayList<>());
            return;
        }
        Map<String, List<CourtsDTO>> groupedMap = new LinkedHashMap<>();
        for (CourtsDTO court : courts) {
            groupedMap.computeIfAbsent(court.getSportType(), k -> new ArrayList<>()).add(court);
        }
        List<CourtDisplayItem> displayItems = new ArrayList<>();
        for (Map.Entry<String, List<CourtsDTO>> entry : groupedMap.entrySet()) {
            displayItems.add(new CourtDisplayItem.Header(entry.getKey()));
            for (CourtsDTO court : entry.getValue()) {
                displayItems.add(new CourtDisplayItem.CourtItem(court));
            }
        }
        _courtList.postValue(displayItems);
    }

    public void onDayCellClicked(LocalDate date) {
        _selectedDate.setValue(date);
        // Reset khung giờ khi chọn ngày mới
        _startTime.setValue(null);
        _endTime.setValue(null);
    }

    private void updateAvailableTimeZones() {
        LocalDate selectedDate = _selectedDate.getValue();
        StadiumDTO stadium = _stadium.getValue();
        if (stadium == null || stadium.getOpenTime() == null || stadium.getCloseTime() == null || selectedDate == null) {
            timeZones.setValue(new ArrayList<>());
            return;
        }

        int openHour = (int) stadium.getOpenTime().toHours();
        int closeHour = (int) stadium.getCloseTime().toHours();

        List<TimeZone> zones = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();

        for (int hour = openHour; hour < closeHour; hour++) {
            TimeZone timeZone = new TimeZone(hour);
            // Vô hiệu hóa các giờ trong quá khứ (bao gồm giờ hiện tại)
            if (selectedDate.isEqual(today) && hour <= now.getHour()) {
                timeZone.setPast(true);
            }

            boolean isSelected = (start != null && end != null && hour >= start && hour <= end) ||
                    (start != null && end == null && hour == start);

            timeZone.setSelected(isSelected);
            zones.add(timeZone);
        }
        timeZones.setValue(zones);
    }

    public void onTimeZoneClicked(TimeZone clickedZone) {
        if (clickedZone.isPast()) {
            _toastMessage.postValue("Khung giờ này đã qua, vui lòng chọn giờ khác.");
            return;
        }

        int hour = clickedZone.getHour();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();

        if (start == null || end != null) {
            _startTime.setValue(hour);
            _endTime.setValue(null);
        } else {
            if (hour > start) {
                _endTime.setValue(hour);
            } else {
                _startTime.setValue(hour);
                _endTime.setValue(null);
            }
        }
    }

    public void onCourtClicked(int courtId) {
        Set<Integer> currentSelected = _selectedCourtIds.getValue() != null ? new HashSet<>(_selectedCourtIds.getValue()) : new HashSet<>();
        if (currentSelected.contains(courtId)) {
            currentSelected.remove(courtId);
        } else {
            currentSelected.add(courtId);
        }
        _selectedCourtIds.setValue(currentSelected);
        fetchCourtRelations(currentSelected, _selectedCourtRelationIds, null);
    }

    private void filterBookedCourts() {
        LocalDate date = _selectedDate.getValue();
        Integer startHour = _startTime.getValue();
        Integer endHour = _endTime.getValue();
        StadiumDTO stadiumDto = _stadium.getValue();

        if (date == null || startHour == null || endHour == null || stadiumDto == null) {
            _bookedCourtIds.postValue(new HashSet<>());
            _bookedCourtRelationIds.postValue(new HashSet<>());
            return;
        }

        LocalDateTime myStartTime = date.atTime(startHour, 0);
        LocalDateTime myEndTime = date.atTime(endHour + 1, 0);

        DateTimeFormatter odataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        String startOdata = myStartTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);
        String endOdata = myEndTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);

        String statusFilter = "(Status eq 'waiting' or Status eq 'completed' or Status eq 'accepted')";
        String detailFilter = String.format(Locale.US, "d/StartTime lt %s and d/EndTime gt %s", endOdata, startOdata);
        String filterQuery = String.format(Locale.US,
                "StadiumId eq %d and BookingDetails/any(d: %s) and %s",
                stadiumDto.getId(),
                detailFilter,
                statusFilter
        );
        String expandQuery = String.format(Locale.US,
                "BookingDetails($filter=%s)",
                detailFilter.replace("d/", "")
        );

        apiService.getBookedCourtsByDay(filterQuery, expandQuery)
                .enqueue(new Callback<ODataResponse<BookingReadDto>>() {
                    @Override
                    public void onResponse(Call<ODataResponse<BookingReadDto>> call, Response<ODataResponse<BookingReadDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                            Set<Integer> newBookedIds = response.body().getItems().stream()
                                    .filter(booking -> booking.getBookingDetails() != null)
                                    .flatMap(booking -> booking.getBookingDetails().stream())
                                    .map(BookingDetailDTO::getCourtId)
                                    .collect(Collectors.toSet());
                            _bookedCourtIds.postValue(newBookedIds);
                            fetchCourtRelations(newBookedIds, _bookedCourtRelationIds, null);
                        } else {
                            _bookedCourtIds.postValue(new HashSet<>());
                            _bookedCourtRelationIds.postValue(new HashSet<>());
                            Log.e("API_ERROR", "GetBookedCourts failed with code: " + response.code() + " for query: " + call.request().url());
                        }
                    }

                    @Override
                    public void onFailure(Call<ODataResponse<BookingReadDto>> call, Throwable t) {
                        _bookedCourtIds.postValue(new HashSet<>());
                        _bookedCourtRelationIds.postValue(new HashSet<>());
                        _toastMessage.postValue("Lỗi mạng khi kiểm tra sân đã đặt: " + t.getMessage());
                        Log.e("API_FAILURE", "GetBookedCourts network error for query " + call.request().url(), t);
                    }
                });
    }

    private void fetchCourtRelations(Set<Integer> sourceCourtIds, MutableLiveData<Set<Integer>> targetRelationLiveData, @Nullable CourtRelationsCallback onCompleteCallback) {
        if (sourceCourtIds == null || sourceCourtIds.isEmpty()) {
            targetRelationLiveData.postValue(new HashSet<>());
            if (onCompleteCallback != null) {
                onCompleteCallback.onComplete(new HashSet<>());
            }
            return;
        }

        final Set<Integer> allRelationIds = new HashSet<>();
        final AtomicInteger counter = new AtomicInteger(sourceCourtIds.size() * 2);

        Runnable checkCompletion = () -> {
            if (counter.decrementAndGet() == 0) {
                allRelationIds.removeAll(sourceCourtIds);
                targetRelationLiveData.postValue(allRelationIds);
                if (onCompleteCallback != null) {
                    onCompleteCallback.onComplete(allRelationIds);
                }
            }
        };

        Callback<List<ReadCourtRelationDTO>> parentCallback = new Callback<List<ReadCourtRelationDTO>>() {
            @Override
            public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ReadCourtRelationDTO relation : response.body()) {
                        if (relation.getParentCourtId() > 0) {
                            allRelationIds.add(relation.getParentCourtId());
                        }
                    }
                }
                checkCompletion.run();
            }
            @Override
            public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) {
                checkCompletion.run();
            }
        };

        Callback<List<ReadCourtRelationDTO>> childCallback = new Callback<List<ReadCourtRelationDTO>>() {
            @Override
            public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ReadCourtRelationDTO relation : response.body()) {
                        if (relation.getChildCourtId() > 0) {
                            allRelationIds.add(relation.getChildCourtId());
                        }
                    }
                }
                checkCompletion.run();
            }
            @Override
            public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) {
                checkCompletion.run();
            }
        };

        for (int courtId : sourceCourtIds) {
            apiService.getAllCourtRelationByChildId(courtId).enqueue(parentCallback);
            apiService.getAllCourtRelationByParentId(courtId).enqueue(childCallback);
        }
    }

    private void updateBookingSummary() {
        LocalDate date = _selectedDate.getValue();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        Set<Integer> selectedCourts = _selectedCourtIds.getValue();
        StadiumDTO currentStadium = _stadium.getValue();

        if (date == null || start == null || end == null || selectedCourts == null || selectedCourts.isEmpty() || currentStadium == null) {
            // SỬA 4: Đổi tên class
            _bookingSummary.postValue(new DailyBookingSummary()); // <-- Gọi constructor rỗng
            return;
        }

        int duration = (end - start) + 1;
        double totalPricePerHour = 0;
        if (currentStadium.getCourts() != null) {
            for (CourtsDTO court : currentStadium.getCourts()) {
                if (selectedCourts.contains(court.getId())) {
                    totalPricePerHour += court.getPricePerHour();
                }
            }
        }
        double totalCost = duration * totalPricePerHour;

        // SỬA 4: Đổi tên class
        _bookingSummary.postValue(new DailyBookingSummary(date, start, end + 1, duration, totalCost));
    }

    public void onToastShown() {
        _toastMessage.setValue(null);
    }

    public void onCheckoutNavigated() {
        _navigateToCheckout.setValue(null);
    }

    private boolean isLoggedIn() {
        // Sử dụng đúng tên SharedPreferences và key bạn cung cấp
        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        // Nếu userId khác -1 (giá trị default), nghĩa là người dùng đã đăng nhập
        return currentUserId != -1;
    }

    public void onContinueClicked() {
        // ===> BƯỚC 1: KIỂM TRA LOGIN TRƯỚC TIÊN <===
        if (!isLoggedIn()) {
            _toastMessage.postValue("Vui lòng đăng nhập để tiếp tục");
            _navigateToLogin.postValue(true);
            return; // Dừng thực thi
        }
        // ===========================================

        // BƯỚC 2: Validate dữ liệu (như cũ)
        LocalDate date = _selectedDate.getValue();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        Set<Integer> selectedCourts = _selectedCourtIds.getValue();
        StadiumDTO currentStadium = _stadium.getValue();

        if (date == null) {
            _toastMessage.postValue("Vui lòng chọn ngày");
            return;
        }
        if (start == null || end == null) {
            _toastMessage.postValue("Vui lòng chọn khung giờ");
            return;
        }
        if (selectedCourts == null || selectedCourts.isEmpty()) {
            _toastMessage.postValue("Vui lòng chọn ít nhất một sân");
            return;
        }
        if (currentStadium == null) {
            _toastMessage.postValue("Lỗi dữ liệu sân, vui lòng thử lại");
            return;
        }

        // Tính toán lại tổng tiền
        int duration = (end - start) + 1;
        double totalPricePerHour = 0;
        if (currentStadium.getCourts() != null) {
            for (CourtsDTO court : currentStadium.getCourts()) {
                if (selectedCourts.contains(court.getId())) {
                    totalPricePerHour += court.getPricePerHour();
                }
            }
        }
        double totalCost = duration * totalPricePerHour;
        float totalPriceFloat = (float) totalCost;

        // Chuẩn bị dữ liệu cho Bundle
        int stadiumId = currentStadium.getId();
        String stadiumName = currentStadium.getName();
        String dateString = date.toString();
        int startTimeInt = start;
        int endTimeInt = end + 1;

        int[] courtIdsArray = selectedCourts.stream().mapToInt(Integer::intValue).toArray();
        ArrayList<String> courtNamesList = new ArrayList<>();
        if (currentStadium.getCourts() != null) {
            for (CourtsDTO court : currentStadium.getCourts()) {
                if (selectedCourts.contains(court.getId())) {
                    courtNamesList.add(court.getName());
                }
            }
        }
        String[] courtNamesArray = courtNamesList.toArray(new String[0]);


        Bundle bundle = new Bundle();
        bundle.putInt("stadiumId", stadiumId);
        bundle.putString("stadiumName", stadiumName);
        bundle.putIntArray("courtIds", courtIdsArray);
        bundle.putStringArray("courtNames", courtNamesArray);
        bundle.putString("date", dateString);
        bundle.putInt("startTime", startTimeInt);
        bundle.putInt("endTime", endTimeInt);
        bundle.putFloat("totalPrice", totalPriceFloat);

        _navigateToCheckout.postValue(bundle);
    }

    public void onLoginNavigated() {
        _navigateToLogin.setValue(false);
    }
}