package com.example.sep490_mobile.model;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DailyBookingSummary {

    // --- Vietnamese Locale for formatting ---
    private static final Locale vietnameseLocale = new Locale("vi", "VN");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'tháng' M, yyyy", vietnameseLocale);

    // --- Properties for Monthly Booking ---
    private int numberOfCourts;
    private int numberOfDays;

    // --- Properties for Daily Booking ---
    private LocalDate localDate;
    private int startTime; // e.g., 19
    private int endTime;   // e.g., 22

    // --- Common Properties ---
    private int duration;
    private double totalCost;
    private boolean isButtonEnabled;

    /**
     * Default constructor for an empty summary.
     */
    public DailyBookingSummary() {
        this.isButtonEnabled = false;
        this.totalCost = 0;
    }

    /**
     * Constructor for Monthly Booking (BookingCalendarViewModel).
     */
    public DailyBookingSummary(int numberOfCourts, int numberOfDays, int duration, double totalCost) {
        this.numberOfCourts = numberOfCourts;
        this.numberOfDays = numberOfDays;
        this.duration = duration;
        this.totalCost = totalCost;
        this.isButtonEnabled = (numberOfCourts > 0 && numberOfDays > 0 && duration > 0);
    }

    /**
     * Constructor for Daily Booking (DailyBookingViewModel).
     */
    public DailyBookingSummary(LocalDate localDate, int startTime, int endTime, int duration, double totalCost) {
        this.localDate = localDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.totalCost = totalCost;
        this.isButtonEnabled = true; // Chỉ cần có dữ liệu là bật
    }

    public boolean isButtonEnabled() {
        return isButtonEnabled;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public String getSummaryText() {
        if (!isButtonEnabled) {
            return "Vui lòng chọn ngày, khung giờ và sân để xem tổng hợp";
        }

        // --- Daily Booking Format (MỚI) ---
        if (this.localDate != null) {
            // "Chủ Nhật, 26 tháng 10, 2025"
            String dayOfWeek = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, vietnameseLocale);
            dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1); // Viết hoa chữ cái đầu
            String datePart = dayOfWeek + ", " + localDate.format(dateFormatter);

            // "Từ 19:00 đến 22:00 (3 giờ)"
            String timePart = String.format(Locale.getDefault(), "Từ %02d:00 đến %02d:00 (%d giờ)",
                    this.startTime, this.endTime, this.duration);

            // "Tổng: 600.000 VNĐ"
            String totalPart = "Tổng: " + formatCurrency(this.totalCost, true); // Dùng VNĐ

            return datePart + "  " + timePart + "  " + totalPart;
        }

        // --- Monthly Booking Format (CŨ) ---
        return String.format(Locale.getDefault(),
                "%d sân | %d ngày | %d giờ/ngày | Tổng cộng: %s",
                this.numberOfCourts,
                this.numberOfDays,
                this.duration,
                formatCurrency(this.totalCost, false) // Dùng ₫
        );
    }

    private String formatCurrency(double amount, boolean useVND) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
        String formatted = currencyFormatter.format(amount);

        if (useVND) {
            // Thay "₫" bằng "VNĐ"
            return formatted.replace("₫", "VNĐ").trim();
        }
        // Giữ nguyên "₫"
        return formatted;
    }
}