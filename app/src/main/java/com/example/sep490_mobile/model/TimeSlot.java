package com.example.sep490_mobile.model;

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

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}