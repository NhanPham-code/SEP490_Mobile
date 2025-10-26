package com.example.sep490_mobile.data.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Data class chính cho mỗi ô trong RecyclerView
 */
public class CalendarCell {

    private CalendarCellType type;
    private LocalDate date;
    private String dayHeader;
    private DayOfWeek dayOfWeek;
    private boolean isSelected;
    private boolean isWeekdaySelected;
    private boolean isBooked;
    private boolean isDisabled;

    // Constructor cho ô trống
    public CalendarCell(CalendarCellType type) {
        this.type = type;
    }

    // Constructor cho ô Header (T2, T3...)
    public CalendarCell(String dayHeader, DayOfWeek dayOfWeek, boolean isSelected) {
        this.type = CalendarCellType.DAY_HEADER;
        this.dayHeader = dayHeader;
        this.dayOfWeek = dayOfWeek;
        this.isSelected = isSelected;
    }

    // Constructor cho ô Ngày (1, 2, 3...)
    public CalendarCell(LocalDate date, DayOfWeek dayOfWeek, boolean isSelected, boolean isWeekdaySelected, boolean isBooked, boolean isDisabled) {
        this.type = CalendarCellType.DAY_CELL;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.isSelected = isSelected;
        this.isWeekdaySelected = isWeekdaySelected;
        this.isBooked = isBooked;
        this.isDisabled = isDisabled;
    }

    // Getters
    public CalendarCellType getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDayHeader() { return dayHeader; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public boolean isSelected() { return isSelected; }
    public boolean isWeekdaySelected() { return isWeekdaySelected; }
    public boolean isBooked() { return isBooked; }
    public boolean isDisabled() { return isDisabled; }
}