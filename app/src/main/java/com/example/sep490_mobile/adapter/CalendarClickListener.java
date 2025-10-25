package com.example.sep490_mobile.adapter;

import java.time.DayOfWeek;
import java.time.LocalDate;

// Interface để Fragment lắng nghe sự kiện click
public interface CalendarClickListener {
    void onDayCellClicked(LocalDate date);
    void onDayHeaderClicked(DayOfWeek dayOfWeek);
}