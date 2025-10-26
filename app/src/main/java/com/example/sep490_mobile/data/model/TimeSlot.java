package com.example.sep490_mobile.data.model;

import java.util.Objects;

public class TimeSlot {
    private final int hour;
    private boolean isSelected;

    public TimeSlot(int hour, boolean isSelected) {
        this.hour = hour;
        this.isSelected = isSelected;
    }

    public int getHour() {
        return hour;
    }

    public boolean isSelected() {
        return isSelected;
    }

    // Dùng cho DiffUtil
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return hour == timeSlot.hour && isSelected == timeSlot.isSelected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, isSelected);
    }
}