package com.example.sep490_mobile.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class DurationConverter {
    public static String convertDuration(String isoDuration, int take) {
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
            String formattedTime = "";
            if(take == 1){
                formattedTime = String.format("%02d:%02d", hours, minutes);
            }
            if(take == 2){
                formattedTime = String.format("%02d:%02d", hours, minutes, seconds);
            }
            // Sử dụng String.format để định dạng chuỗi



            return formattedTime;

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể phân tích chuỗi thời lượng (Duration): " + isoDuration);
            System.err.println("Đảm bảo chuỗi tuân thủ định dạng ISO 8601, ví dụ: 'PT8H', 'PT1H30M', 'P3D'.");
        }
        return "";
    }
    private static final String INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX";

    /**
     * Chuyển đổi chuỗi ngày giờ ISO 8601 sang định dạng mong muốn.
     * @param isoDateString Chuỗi ngày giờ ISO 8601.
     * @param outputPattern Định dạng đầu ra mong muốn (ví dụ: "dd/MM/yyyy HH:mm").
     * @return Chuỗi ngày giờ đã định dạng.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertIsoDate(String isoDateString, String outputPattern) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "";
        }

        try {
            // Định nghĩa formatter cho chuỗi đầu vào
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(INPUT_FORMAT);

            // Phân tích chuỗi đầu vào thành OffsetDateTime (chứa cả múi giờ)
            OffsetDateTime dateTime = OffsetDateTime.parse(isoDateString, inputFormatter);

            // Định nghĩa formatter cho định dạng đầu ra mong muốn
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputPattern, Locale.getDefault());

            // Định dạng và trả về
            return dateTime.format(outputFormatter);

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi định dạng ngày";
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertIsoPlayDate(String isoDateString, String outputPattern) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "";
        }

        try {
            // 1. Phân tích chuỗi đầu vào (OffsetDateTime xử lý +07:00)
            OffsetDateTime dateTime = OffsetDateTime.parse(isoDateString, INPUT_FORMATTER);

            // 2. Định nghĩa formatter cho định dạng đầu ra
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputPattern, Locale.getDefault());

            // 3. Định dạng và trả về
            return dateTime.format(outputFormatter);

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi định dạng ngày: " + isoDateString;
        }
    }
}
