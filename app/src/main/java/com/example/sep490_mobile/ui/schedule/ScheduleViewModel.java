package com.example.sep490_mobile.ui.schedule;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDetailDTO;
import com.example.sep490_mobile.data.dto.ScheduleDisplayItem;
import com.example.sep490_mobile.data.repository.ScheduleRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ScheduleViewModel extends AndroidViewModel {

    private final ScheduleRepository scheduleRepository;
    private final MutableLiveData<String> weekRangeText;
    private final MutableLiveData<List<Calendar>> daysOfWeek;
    private final Calendar currentCalendar;
    private final MutableLiveData<String> filterTrigger = new MutableLiveData<>();
    private final LiveData<List<ScheduleBookingDTO>> rawBookings;

    // LiveData mới chứa dữ liệu đã được làm phẳng và nhóm lại
    private final LiveData<Map<String, List<ScheduleDisplayItem>>> groupedAndSortedBookings;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        scheduleRepository = ScheduleRepository.getInstance(application);
        weekRangeText = new MutableLiveData<>();
        daysOfWeek = new MutableLiveData<>();
        currentCalendar = Calendar.getInstance();

        rawBookings = Transformations.switchMap(filterTrigger, scheduleRepository::getBookings);

        groupedAndSortedBookings = Transformations.map(rawBookings, bookings -> {
            Map<String, List<ScheduleDisplayItem>> finalGroupedMap = new LinkedHashMap<>();
            if (bookings == null) return finalGroupedMap;

            SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            for (ScheduleBookingDTO booking : bookings) {
                if (booking.getBookingDetails() == null || booking.getBookingDetails().isEmpty()) continue;

                Map<String, List<ScheduleBookingDetailDTO>> detailsGroupedByTime = new HashMap<>();
                for (ScheduleBookingDetailDTO detail : booking.getBookingDetails()) {
                    String timeKey = detail.getStartTime() + "|" + detail.getEndTime();
                    detailsGroupedByTime.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(detail);
                }

                for (Map.Entry<String, List<ScheduleBookingDetailDTO>> entry : detailsGroupedByTime.entrySet()) {
                    List<ScheduleBookingDetailDTO> detailsInSlot = entry.getValue();
                    if (detailsInSlot.isEmpty()) continue;

                    ScheduleBookingDetailDTO firstDetail = detailsInSlot.get(0);
                    // Lấy ra danh sách các tên sân con
                    List<String> courtNames = detailsInSlot.stream()
                            .map(ScheduleBookingDetailDTO::getCourtName)
                            .collect(Collectors.toList());

                    ScheduleDisplayItem displayItem = new ScheduleDisplayItem(
                            booking.getStadiumName(),
                            firstDetail.getStartTime(),
                            firstDetail.getEndTime(),
                            booking.getStatus(),
                            courtNames,
                            booking
                    );

                    String dateKey = extractDateString(booking.getDate(), keyFormat);
                    if (dateKey != null) {
                        finalGroupedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(displayItem);
                    }
                }
            }

            for (List<ScheduleDisplayItem> dayItems : finalGroupedMap.values()) {
                Collections.sort(dayItems, (item1, item2) -> item1.getStartTime().compareTo(item2.getStartTime()));
            }

            return finalGroupedMap;
        });

        updateWeekData();
    }

    public LiveData<Map<String, List<ScheduleDisplayItem>>> getGroupedAndSortedBookings() {
        return groupedAndSortedBookings;
    }

    private String extractDateString(String dateTimeString, SimpleDateFormat format) {
        if (dateTimeString == null) return null;
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sourceFormat.parse(dateTimeString);
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<String> getWeekRangeText() { return weekRangeText; }
    public LiveData<List<Calendar>> getDaysOfWeek() { return daysOfWeek; }

    public void nextWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        updateWeekData();
    }

    public void previousWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        updateWeekData();
    }

    private void updateWeekData() {
        Calendar cal = (Calendar) currentCalendar.clone();
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String startDateApi = apiFormat.format(cal.getTime());
        String startDateDisplay = displayFormat.format(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        String endDateApi = apiFormat.format(cal.getTime());
        String endDateDisplay = displayFormat.format(cal.getTime());

        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        // Kiểm tra xem userId có hợp lệ không (ví dụ: > 0)
        if (currentUserId <= 0) {
            Log.e("ScheduleViewModel", "User ID không hợp lệ, không thể tải booking.");
            // Bạn có thể set một trạng thái lỗi cho LiveData ở đây nếu muốn
            return; // Dừng lại không gọi API
        }


        String filterQuery = String.format(Locale.US,
                "Date ge %s and Date le %s and (Status eq 'pending' or Status eq 'accepted' or Status eq 'waiting' or Status eq 'completed') and UserId eq %d",
                startDateApi,
                endDateApi,
                currentUserId);

        String weekText = getApplication().getString(R.string.current_week_format, startDateDisplay, endDateDisplay);
        weekRangeText.setValue(weekText);
        filterTrigger.setValue(filterQuery);

        List<Calendar> weekDays = new ArrayList<>();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        for (int i = 0; i < 7; i++) {
            weekDays.add((Calendar) cal.clone());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        daysOfWeek.setValue(weekDays);
    }

    public void setDate(Calendar newDate) {
        this.currentCalendar.setTime(newDate.getTime());
        updateWeekData();
    }
}