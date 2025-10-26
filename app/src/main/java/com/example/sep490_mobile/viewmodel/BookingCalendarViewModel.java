package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sep490_mobile.data.dto.BookingDetailDTO;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.model.CalendarCell;
import com.example.sep490_mobile.data.model.CalendarCellType;
import com.example.sep490_mobile.data.model.CourtDisplayItem;
import com.example.sep490_mobile.data.model.TimeSlot;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import com.example.sep490_mobile.model.BookingSummary;
import com.example.sep490_mobile.model.TimeZone;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public class BookingCalendarViewModel extends AndroidViewModel {

    interface CourtRelationsCallback {
        void onComplete(Set<Integer> relationIds);
    }

    private final Locale vietnameseLocale = new Locale("vi", "VN");
    private final List<String> dayNames = Arrays.asList("T2", "T3", "T4", "T5", "T6", "T7", "CN");
    private final MutableLiveData<YearMonth> _currentYearMonth = new MutableLiveData<>();
    public LiveData<YearMonth> currentYearMonth = _currentYearMonth;
    private final MutableLiveData<Set<LocalDate>> _selectedDates = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<LocalDate>> selectedDates = _selectedDates;
    private final MutableLiveData<Set<Integer>> _bookedDaysInMonth = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> bookedDaysInMonth = _bookedDaysInMonth;
    private YearMonth minAllowedYearMonth;
    private final MediatorLiveData<List<CalendarCell>> _calendarCells = new MediatorLiveData<>();
    public LiveData<List<CalendarCell>> calendarCells = _calendarCells;
    private final ApiService apiService;
    private final MutableLiveData<StadiumDTO> _stadium = new MutableLiveData<>();
    public LiveData<StadiumDTO> stadium = _stadium;
    private final MutableLiveData<List<TimeSlot>> _timeSlots = new MutableLiveData<>();
    public LiveData<List<TimeSlot>> timeSlots = _timeSlots;
    private final MutableLiveData<Integer> _startTime = new MutableLiveData<>(null);
    public LiveData<Integer> startTime = _startTime;
    private final MutableLiveData<Integer> _endTime = new MutableLiveData<>(null);
    public LiveData<Integer> endTime = _endTime;
    private final MutableLiveData<List<CourtDisplayItem>> _courtList = new MutableLiveData<>();
    public LiveData<List<CourtDisplayItem>> courtList = _courtList;
    private List<Integer> allHours = new ArrayList<>();
    private final MutableLiveData<Set<Integer>> _bookedCourtIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> bookedCourtIds = _bookedCourtIds;
    private final MutableLiveData<Set<Integer>> _bookedCourtRelationIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> bookedCourtRelationIds = _bookedCourtRelationIds;
    private final MutableLiveData<Set<Integer>> _selectedCourtIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> selectedCourtIds = _selectedCourtIds;
    private final MutableLiveData<Set<Integer>> _selectedCourtRelationIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<Integer>> selectedCourtRelationIds = _selectedCourtRelationIds;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;
    private final MutableLiveData<Bundle> _navigateToCheckout = new MutableLiveData<>();
    public LiveData<Bundle> navigateToCheckout = _navigateToCheckout;
    private final MediatorLiveData<BookingSummary> _bookingSummary = new MediatorLiveData<>();
    public LiveData<BookingSummary> bookingSummary = _bookingSummary;
    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToLogin = _navigateToLogin;

    public LiveData<String> monthYearText = Transformations.map(_currentYearMonth, yearMonth -> {
        if (yearMonth == null) return "";
        String month = yearMonth.getMonth().getDisplayName(TextStyle.FULL, vietnameseLocale);
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        return month + " năm " + yearMonth.getYear();
    });

    public BookingCalendarViewModel(@NonNull Application application) {
        super(application);
        this.apiService = ApiClient.getInstance(application.getApplicationContext()).getApiService();
        calculateMinAllowedMonth();
        _calendarCells.addSource(_currentYearMonth, ym -> refreshCalendar());
        _calendarCells.addSource(_selectedDates, dates -> refreshCalendar());
        _calendarCells.addSource(_bookedDaysInMonth, dates -> refreshCalendar());

        _bookingSummary.addSource(_selectedDates, dates -> updateBookingSummary());
        _bookingSummary.addSource(_startTime, start -> updateBookingSummary());
        _bookingSummary.addSource(_endTime, end -> updateBookingSummary());
        _bookingSummary.addSource(_selectedCourtIds, courts -> updateBookingSummary());
        _bookingSummary.addSource(_bookedDaysInMonth, bookedDays -> updateBookingSummary());
        _bookingSummary.addSource(_stadium, stadium -> updateBookingSummary());

        refreshCalendar();
        updateBookingSummary();
    }

    private void calculateMinAllowedMonth() {
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() > 25) {
            minAllowedYearMonth = YearMonth.from(today).plusMonths(2);
        } else {
            minAllowedYearMonth = YearMonth.from(today).plusMonths(1);
        }
        _currentYearMonth.setValue(minAllowedYearMonth);
    }

    private void refreshCalendar() {
        YearMonth ym = _currentYearMonth.getValue();
        Set<LocalDate> selected = _selectedDates.getValue();
        Set<Integer> booked = _bookedDaysInMonth.getValue();
        if (ym != null && selected != null && booked != null) {
            List<CalendarCell> cells = generateCalendarCells(ym, selected, booked);
            _calendarCells.setValue(cells);
        }
    }

    private List<CalendarCell> generateCalendarCells(YearMonth yearMonth, Set<LocalDate> selected, Set<Integer> booked) {
        List<CalendarCell> cells = new ArrayList<>();
        Set<DayOfWeek> selectedWeekdays = calculateSelectedWeekdays(yearMonth, selected, booked);
        for (int i = 0; i < dayNames.size(); i++) {
            DayOfWeek dayOfWeek = DayOfWeek.of(i + 1);
            cells.add(new CalendarCell(dayNames.get(i), dayOfWeek, selectedWeekdays.contains(dayOfWeek)));
        }
        LocalDate firstDay = yearMonth.atDay(1);
        int firstDayOfWeekValue = firstDay.getDayOfWeek().getValue();
        int emptyCellsAtStart = firstDayOfWeekValue - 1;
        for (int i = 0; i < emptyCellsAtStart; i++) {
            cells.add(new CalendarCell(CalendarCellType.EMPTY_CELL));
        }
        boolean isValidMonth = !yearMonth.isBefore(minAllowedYearMonth);
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            boolean isSelected = selected.contains(date);
            boolean isWeekdaySelected = selectedWeekdays.contains(date.getDayOfWeek());
            boolean isBooked = booked.contains(day);
            boolean isDisabled = !isValidMonth || isBooked;
            cells.add(new CalendarCell(date, date.getDayOfWeek(), isSelected, isWeekdaySelected, isBooked, isDisabled));
        }
        return cells;
    }

    private Set<DayOfWeek> calculateSelectedWeekdays(YearMonth yearMonth, Set<LocalDate> selected, Set<Integer> booked) {
        Set<DayOfWeek> selectedWeekdays = new HashSet<>();
        for (int i = 1; i <= 7; i++) {
            DayOfWeek dayOfWeek = DayOfWeek.of(i);
            if (areAllAvailableWeekdayDatesSelected(yearMonth, dayOfWeek, selected, booked)) {
                selectedWeekdays.add(dayOfWeek);
            }
        }
        return selectedWeekdays;
    }

    private boolean areAllAvailableWeekdayDatesSelected(YearMonth yearMonth, DayOfWeek dayOfWeek, Set<LocalDate> selected, Set<Integer> booked) {
        List<LocalDate> availableDays = new ArrayList<>();
        boolean isValidMonth = !yearMonth.isBefore(minAllowedYearMonth);
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            if (date.getDayOfWeek() == dayOfWeek && !booked.contains(day) && isValidMonth) {
                availableDays.add(date);
            }
        }
        return !availableDays.isEmpty() && selected.containsAll(availableDays);
    }

    private List<LocalDate> getAllAvailableDaysForWeekday(YearMonth yearMonth, DayOfWeek dayOfWeek, Set<Integer> booked) {
        List<LocalDate> availableDays = new ArrayList<>();
        boolean isValidMonth = !yearMonth.isBefore(minAllowedYearMonth);
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            if (date.getDayOfWeek() == dayOfWeek && !booked.contains(day) && isValidMonth) {
                availableDays.add(date);
            }
        }
        return availableDays;
    }

    public void onDayCellClicked(LocalDate date) {
        Set<LocalDate> currentSelected = new HashSet<>(_selectedDates.getValue());
        if (currentSelected.contains(date)) currentSelected.remove(date);
        else currentSelected.add(date);
        _selectedDates.setValue(currentSelected);
    }

    public void onDayHeaderClicked(DayOfWeek dayOfWeek) {
        Set<LocalDate> currentSelected = new HashSet<>(_selectedDates.getValue());
        List<LocalDate> allAvailableDays = getAllAvailableDaysForWeekday(_currentYearMonth.getValue(), dayOfWeek, _bookedDaysInMonth.getValue());
        if (currentSelected.containsAll(allAvailableDays)) currentSelected.removeAll(allAvailableDays);
        else currentSelected.addAll(allAvailableDays);
        _selectedDates.setValue(currentSelected);
    }

    public void goToNextMonth() {
        _currentYearMonth.setValue(_currentYearMonth.getValue().plusMonths(1));
        fetchBookedDaysForSelectedCourts();
    }

    public void goToPreviousMonth() {
        YearMonth current = _currentYearMonth.getValue();
        if (!current.minusMonths(1).isBefore(minAllowedYearMonth)) {
            _currentYearMonth.setValue(current.minusMonths(1));
            fetchBookedDaysForSelectedCourts();
        }
    }

    public void onTimeSlotClicked(int hour) {
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        if (start == null || (end != null)) {
            _startTime.setValue(hour);
            _endTime.setValue(null);
        } else if (hour > start) {
            _endTime.setValue(hour);
        } else {
            _startTime.setValue(hour);
            _endTime.setValue(null);
        }
        updateTimeSlotSelection();
        if (_endTime.getValue() != null) {
            fetchBookedDaysForSelectedCourts();
        }
    }

    public void clearAllSelections() {
        if (_selectedDates.getValue() != null && !_selectedDates.getValue().isEmpty()) {
            _selectedDates.setValue(new HashSet<>());
        }
        if (_selectedCourtIds.getValue() != null && !_selectedCourtIds.getValue().isEmpty()) {
            _selectedCourtIds.setValue(new HashSet<>());
            _selectedCourtRelationIds.setValue(new HashSet<>());
        }
        if (_bookedCourtIds.getValue() != null && !_bookedCourtIds.getValue().isEmpty()) {
            _bookedCourtIds.setValue(new HashSet<>());
            _bookedCourtRelationIds.setValue(new HashSet<>());
        }
        fetchBookedDaysForSelectedCourts();
    }

    public void onCourtClicked(int courtId) {
        Set<Integer> currentSelected = new HashSet<>(_selectedCourtIds.getValue());
        if (currentSelected.contains(courtId)) {
            currentSelected.remove(courtId);
        } else {
            currentSelected.add(courtId);
        }
        _selectedCourtIds.setValue(currentSelected);
        fetchCourtRelations(currentSelected, _selectedCourtRelationIds, this::fetchBookedDaysForSelectedCourts);
    }

    public void onCheckoutNavigated() {
        _navigateToCheckout.postValue(null);
    }

    public void onToastShown() {
        _toastMessage.postValue(null);
    }

    public void fetchStadiumData(String stadiumId) {
        Map<String, String> options = new HashMap<>();
        options.put("$filter", "Id eq " + stadiumId);
        options.put("$expand", "Courts");
        apiService.getStadiumsOdata(options).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                    StadiumDTO fetchedStadium = response.body().getItems().get(0);
                    _stadium.postValue(fetchedStadium);
                    generateTimeSlots(fetchedStadium.getOpenTime(), fetchedStadium.getCloseTime());
                    processCourtList(fetchedStadium.getCourts());
                }
            }
            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) { /* Xử lý lỗi */ }
        });
    }

    private void generateTimeSlots(Duration openTimeDuration, Duration closeTimeDuration) {
        int openTime = 6; int closeTime = 22;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (openTimeDuration != null) openTime = (int) openTimeDuration.toHours();
            if (closeTimeDuration != null) closeTime = (int) closeTimeDuration.toHours();
        }
        allHours.clear();
        for (int i = openTime; i <= closeTime; i++) allHours.add(i);
        updateTimeSlotSelection();
    }

    private void updateTimeSlotSelection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Integer start = _startTime.getValue();
            Integer end = _endTime.getValue();
            List<TimeSlot> newTimeSlots = allHours.stream().map(hour -> {
                boolean isSelected = (start != null && end != null && hour >= start && hour <= end) || (start != null && end == null && hour.equals(start));
                return new TimeSlot(hour, isSelected);
            }).collect(Collectors.toList());
            _timeSlots.postValue(newTimeSlots);
        }
    }

    private void processCourtList(Set<CourtsDTO> courts) {
        if (courts == null || courts.isEmpty()) {
            _courtList.postValue(new ArrayList<>());
            return;
        }
        Map<String, List<CourtsDTO>> groupedMap = new LinkedHashMap<>();
        for (CourtsDTO court : courts) {
            if (!groupedMap.containsKey(court.getSportType())) {
                groupedMap.put(court.getSportType(), new ArrayList<>());
            }
            groupedMap.get(court.getSportType()).add(court);
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

    public void filterBookedCourts() {
        YearMonth currentYm = _currentYearMonth.getValue();
        Set<LocalDate> dates = _selectedDates.getValue();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        StadiumDTO stadiumDto = _stadium.getValue();
        if (currentYm == null || dates == null || dates.isEmpty() || start == null || end == null || stadiumDto == null) return;
        int year = currentYm.getYear();
        int month = currentYm.getMonthValue();
        int stadiumId = stadiumDto.getId();
        List<Integer> days = dates.stream().map(LocalDate::getDayOfMonth).collect(Collectors.toList());
        String startTimeStr = String.format(Locale.US, "%02d:00", start);
        String endTimeStr = String.format(Locale.US, "%02d:00", end);
        apiService.filterByDateAndHour(year, month, days, startTimeStr, endTimeStr, stadiumId)
                .enqueue(new Callback<List<BookingReadDto>>() {
                    @Override
                    public void onResponse(Call<List<BookingReadDto>> call, Response<List<BookingReadDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Set<Integer> newBookedIds = new HashSet<>();
                            for (BookingReadDto booking : response.body()) {
                                if (booking.getBookingDetails() != null) {
                                    for (BookingDetailDTO detail : booking.getBookingDetails()) {
                                        newBookedIds.add(detail.getCourtId());
                                    }
                                }
                            }
                            _bookedCourtIds.postValue(newBookedIds);
                            fetchCourtRelations(newBookedIds, _bookedCourtRelationIds, null);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<BookingReadDto>> call, Throwable t) { /* Xử lý lỗi mạng */ }
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

    private void fetchBookedDaysForSelectedCourts(Set<Integer> passedRelationIds) {
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        YearMonth ym = _currentYearMonth.getValue();

        if (start == null || end == null || ym == null) {
            _bookedDaysInMonth.postValue(new HashSet<>());
            refreshCalendar();
            return;
        }

        Set<Integer> directlySelectedIds = _selectedCourtIds.getValue() != null ? _selectedCourtIds.getValue() : new HashSet<>();
        Set<Integer> allCourtIdsSet = new HashSet<>(directlySelectedIds);
        if (passedRelationIds != null) {
            allCourtIdsSet.addAll(passedRelationIds);
        }
        List<Integer> allCourtIds = new ArrayList<>(allCourtIdsSet);

        if (allCourtIds.isEmpty()) {
            _bookedDaysInMonth.postValue(new HashSet<>());
            refreshCalendar();
            return;
        }

        int month = ym.getMonthValue();
        int year = ym.getYear();
        String startTimeStr = String.format(Locale.US, "%02d:00", start);
        String endTimeStr = String.format(Locale.US, "%02d:00", end);

        apiService.filterByCourtAndHourForCalendar(allCourtIds, year, month, startTimeStr, endTimeStr)
                .enqueue(new Callback<List<BookingReadDto>>() {
                    @Override
                    public void onResponse(Call<List<BookingReadDto>> call, Response<List<BookingReadDto>> response) {
                        Set<Integer> newBookedDaysSet = new HashSet<>();
                        if (response.isSuccessful() && response.body() != null) {
                            for (BookingReadDto booking : response.body()) {
                                if (booking.getDate() == null) continue;
                                try {
                                    LocalDate bookingDate = LocalDate.parse(booking.getDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                    int dayOfMonth = bookingDate.getDayOfMonth();
                                    newBookedDaysSet.add(dayOfMonth);
                                } catch (Exception e) {
                                    Log.e("ViewModel", "Lỗi parse ngày: " + booking.getDate(), e);
                                }
                            }
                        }
                        _bookedDaysInMonth.postValue(newBookedDaysSet);
                    }
                    @Override
                    public void onFailure(Call<List<BookingReadDto>> call, Throwable t) {
                        Log.e("ViewModel", "Lỗi khi lấy danh sách ngày đã đặt", t);
                    }
                });
    }

    private void fetchBookedDaysForSelectedCourts() {
        Set<Integer> relatedIds = _selectedCourtRelationIds.getValue() != null ? _selectedCourtRelationIds.getValue() : new HashSet<>();
        fetchBookedDaysForSelectedCourts(relatedIds);
    }

    private void updateBookingSummary() {
        Set<LocalDate> currentSelectedDates = _selectedDates.getValue();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        Set<Integer> currentSelectedCourtIds = _selectedCourtIds.getValue();
        StadiumDTO currentStadium = _stadium.getValue();
        Set<Integer> currentBookedDays = _bookedDaysInMonth.getValue();

        if (currentSelectedDates == null || start == null || end == null ||
                currentSelectedCourtIds == null || currentSelectedCourtIds.isEmpty() ||
                currentStadium == null || currentStadium.getCourts() == null || currentBookedDays == null) {
            _bookingSummary.postValue(new BookingSummary());
            return;
        }

        Set<LocalDate> actualBookableDates = new HashSet<>();
        for (LocalDate date : currentSelectedDates) {
            if (!currentBookedDays.contains(date.getDayOfMonth())) {
                actualBookableDates.add(date);
            }
        }

        if (actualBookableDates.isEmpty()) {
            _bookingSummary.postValue(new BookingSummary());
            return;
        }

        int numberOfCourts = currentSelectedCourtIds.size();
        int numberOfDays = actualBookableDates.size();
        int duration = end - start;
        double totalPricePerHour = 0;
        for (CourtsDTO court : currentStadium.getCourts()) {
            if (currentSelectedCourtIds.contains(court.getId())) {
                totalPricePerHour += court.getPricePerHour();
            }
        }
        double totalCost = duration * numberOfDays * totalPricePerHour;
        _bookingSummary.postValue(new BookingSummary(numberOfCourts, numberOfDays, duration, totalCost));
    }

    private boolean isLoggedIn() {
        // Sử dụng đúng tên SharedPreferences và key bạn cung cấp
        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        // Nếu userId khác -1 (giá trị default), nghĩa là người dùng đã đăng nhập
        return currentUserId != -1;
    }

    public void onLoginNavigated() {
        _navigateToLogin.setValue(false);
    }

    public void onContinueClicked() {
        // ===> BƯỚC 1: KIỂM TRA LOGIN TRƯỚC TIÊN <===
        if (!isLoggedIn()) {
            _toastMessage.postValue("Vui lòng đăng nhập để tiếp tục");
            _navigateToLogin.postValue(true);
            return; // Dừng thực thi
        }
        // ===========================================

        // BƯỚC 2: Validate dữ liệu (logic cũ)
        Set<LocalDate> currentSelectedDates = _selectedDates.getValue();
        Integer start = _startTime.getValue();
        Integer end = _endTime.getValue();
        Set<Integer> currentSelectedCourtIds = _selectedCourtIds.getValue();
        StadiumDTO stadium = _stadium.getValue();
        Set<Integer> currentBookedDays = _bookedDaysInMonth.getValue();

        if (start == null || end == null) {
            _toastMessage.postValue("Vui lòng chọn khung giờ");
            return;
        }
        if (currentSelectedDates == null || currentSelectedDates.isEmpty()) {
            _toastMessage.postValue("Vui lòng chọn ngày");
            return;
        }
        if (currentSelectedCourtIds == null || currentSelectedCourtIds.isEmpty()) {
            _toastMessage.postValue("Vui lòng chọn ít nhất một sân");
            return;
        }
        if (stadium == null || stadium.getCourts() == null) {
            _toastMessage.postValue("Lỗi, không có dữ liệu sân");
            return;
        }

        // ... (Phần còn lại của hàm giữ nguyên) ...
        Set<LocalDate> actualBookableDates = new HashSet<>();
        if (currentBookedDays != null) {
            for (LocalDate date : currentSelectedDates) {
                if (!currentBookedDays.contains(date.getDayOfMonth())) {
                    actualBookableDates.add(date);
                }
            }
        } else {
            actualBookableDates.addAll(currentSelectedDates);
        }

        if (actualBookableDates.isEmpty()){
            _toastMessage.postValue("Tất cả các ngày bạn chọn đều đã bị đặt cho sân này. Vui lòng chọn lại.");
            return;
        }

        int duration = end - start;
        int numberOfDays = actualBookableDates.size();
        double totalPricePerHour = 0;
        ArrayList<String> selectedCourtNames = new ArrayList<>();

        for (CourtsDTO court : stadium.getCourts()) {
            if (currentSelectedCourtIds.contains(court.getId())) {
                totalPricePerHour += court.getPricePerHour();
                selectedCourtNames.add(court.getName());
            }
        }
        double totalCost = duration * numberOfDays * totalPricePerHour;

        Bundle checkoutBundle = new Bundle();
        checkoutBundle.putString("STADIUM_NAME", stadium.getName());
        checkoutBundle.putFloat("TOTAL_PRICE", (float) totalCost);
        checkoutBundle.putInt("START_TIME", start);
        checkoutBundle.putInt("END_TIME", end);
        checkoutBundle.putInt("YEAR", _currentYearMonth.getValue().getYear());
        checkoutBundle.putInt("MONTH", _currentYearMonth.getValue().getMonthValue());

        int[] courtIdsArray = currentSelectedCourtIds.stream().mapToInt(Integer::intValue).toArray();
        checkoutBundle.putIntArray("COURT_IDS", courtIdsArray);
        checkoutBundle.putStringArray("COURT_NAMES", selectedCourtNames.toArray(new String[0]));

        ArrayList<String> bookableDateStrings = new ArrayList<>();
        for(LocalDate date : actualBookableDates) {
            bookableDateStrings.add(date.toString());
        }
        checkoutBundle.putStringArray("BOOKABLE_DATES", bookableDateStrings.toArray(new String[0]));
        _navigateToCheckout.postValue(checkoutBundle);
    }
}