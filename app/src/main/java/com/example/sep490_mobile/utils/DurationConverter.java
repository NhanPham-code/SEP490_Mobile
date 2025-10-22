package com.example.sep490_mobile.utils;

import android.os.Build;

import java.time.Duration;
import java.util.Locale;

public class DurationConverter {
    public static String convertDuration(String isoDuration) {
        try {
            // 1. Parse the ISO 8601 duration string (e.g., "PT8H")
            Duration duration = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                duration = Duration.parse(isoDuration);
            }

            // 2. Extract total components
            long totalSeconds = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                totalSeconds = duration.getSeconds();
            }

            // 3. Convert total seconds into standard H:M:S format
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            // Sử dụng String.format để định dạng chuỗi
            String formattedTime = String.format("%02d:%02d", hours, minutes);


            return formattedTime;

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể phân tích chuỗi thời lượng (Duration): " + isoDuration);
            System.err.println("Đảm bảo chuỗi tuân thủ định dạng ISO 8601, ví dụ: 'PT8H', 'PT1H30M', 'P3D'.");
        }
        return "";
    }

    public static int parseHour(Duration duration) {
        if (duration == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0;
        }
        return (int) duration.toHours();
    }

    public static String convertDuration(Duration duration) {
        if (duration == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "N/A";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }
}
