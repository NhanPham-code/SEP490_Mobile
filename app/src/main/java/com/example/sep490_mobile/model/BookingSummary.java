package com.example.sep490_mobile.model;

import java.text.NumberFormat;
import java.util.Locale;

public class BookingSummary {
    private final String summaryText;
    private final boolean isButtonEnabled;

    // Constructor cho trạng thái có đầy đủ thông tin
    public BookingSummary(int courtCount, int dayCount, int hourCount, double totalCost) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.summaryText = String.format(Locale.US,
                "%d sân | %d ngày | %d giờ/ngày | Tổng cộng: %s",
                courtCount, dayCount, hourCount, currencyFormat.format(totalCost)
        );
        this.isButtonEnabled = true;
    }

    // Constructor cho trạng thái mặc định (thiếu thông tin)
    public BookingSummary() {
        this.summaryText = "Vui lòng chọn ngày, khung giờ và sân để xem tổng hợp";
        this.isButtonEnabled = false;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public boolean isButtonEnabled() {
        return isButtonEnabled;
    }
}