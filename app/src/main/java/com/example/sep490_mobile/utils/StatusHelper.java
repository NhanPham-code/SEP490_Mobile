package com.example.sep490_mobile.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.sep490_mobile.R;

public class StatusHelper {

    public static void setStatus(Context context, TextView textView, String status) {
        if (status == null) {
            status = "unknown";
        }

        String translatedStatus;
        int backgroundColorRes;
        int textColorRes;

        switch (status.toLowerCase()) {
            case "pending":
                translatedStatus = "Chờ xử lý";
                backgroundColorRes = R.color.status_pending_bg;
                textColorRes = R.color.status_pending_text;
                break;
            case "accepted":
                translatedStatus = "Đã nhận";
                backgroundColorRes = R.color.status_accepted_bg;
                textColorRes = R.color.status_accepted_text;
                break;
            case "completed":
                translatedStatus = "Hoàn thành";
                backgroundColorRes = R.color.status_completed_bg;
                textColorRes = R.color.status_completed_text;
                break;
            case "cancelled":
            case "denied":
            case "payfail":
                translatedStatus = "Đã hủy/Lỗi";
                backgroundColorRes = R.color.status_cancelled_bg;
                textColorRes = R.color.status_cancelled_text;
                break;
            case "waiting":
                translatedStatus = "Chờ thanh toán";
                backgroundColorRes = R.color.status_waiting_bg;
                textColorRes = R.color.status_waiting_text;
                break;
            default:
                translatedStatus = status;
                backgroundColorRes = R.color.status_default_bg;
                textColorRes = R.color.status_default_text;
                break;
        }

        textView.setText(translatedStatus);

        // Tạo một drawable mới mỗi lần để tránh chia sẻ state
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.status_badge_template).mutate();
        background.setColor(ContextCompat.getColor(context, backgroundColorRes));

        textView.setBackground(background);
        textView.setTextColor(ContextCompat.getColor(context, textColorRes));
    }

    public static String getTranslatedStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status.toLowerCase()) {
            case "pending": return "Chờ xử lý";
            case "accepted": return "Đã nhận";
            case "completed": return "Hoàn thành";
            case "cancelled": case "denied": case "payfail": return "Đã hủy/Lỗi";
            case "waiting": return "Chờ thanh toán";
            default: return status;
        }
    }
}