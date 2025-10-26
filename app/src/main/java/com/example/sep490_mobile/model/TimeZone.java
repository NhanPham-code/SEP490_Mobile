package com.example.sep490_mobile.model;

public class TimeZone {
    private final int hour;
    private boolean isPast;
    private boolean isSelected;

    public TimeZone(int hour) {
        this.hour = hour;
        this.isPast = false;
        this.isSelected = false;
    }

    // Getters
    public int getHour() {
        return hour;
    }

    public boolean isPast() {
        return isPast;
    }

    public boolean isSelected() {
        return isSelected;
    }

    // Setters
    public void setPast(boolean past) {
        isPast = past;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}