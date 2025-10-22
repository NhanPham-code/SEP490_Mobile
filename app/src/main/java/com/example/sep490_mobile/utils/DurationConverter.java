package com.example.sep490_mobile.utils;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DurationConverter {
    public static String createCurrentISOString() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 1. Định nghĩa múi giờ bạn muốn (ví dụ: +07:00 cho Việt Nam)
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        // 2. Lấy thời điểm hiện tại trong múi giờ đó
        OffsetDateTime now = OffsetDateTime.now(zoneId);

        // 3. Định nghĩa Formatter
        // Mẫu: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
        // 'T' là ký tự cố định, SSS là mili giây, XXX là định dạng offset (+07:00)
        DateTimeFormatter formatter = null;

            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


        // 4. Định dạng và trả về chuỗi
        return now.format(formatter);
        }
        return "";
    }

    public static String createCurrentISOStringToSearch() {
        OffsetDateTime now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME); // e.g. 2025-10-22T14:43:05.472+07:00
        }
        return "";
    }
    public static OffsetDateTime createCurrentISOSDateTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Định nghĩa múi giờ bạn muốn (ví dụ: +07:00 cho Việt Nam)
            ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

            // 2. Lấy thời điểm hiện tại trong múi giờ đó
            OffsetDateTime now = OffsetDateTime.now(zoneId);




            // 4. Định dạng và trả về chuỗi
            return now;
        }
        return null;
    }
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
                formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
            // Sử dụng String.format để định dạng chuỗi



            return formattedTime;

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể phân tích chuỗi thời lượng (Duration): " + isoDuration);
            System.err.println("Đảm bảo chuỗi tuân thủ định dạng ISO 8601, ví dụ: 'PT8H', 'PT1H30M', 'P3D'.");
        }
        return "";
    }
    public static String convertCustomToReadable(String inputDateTimeString, String outputPattern) {
        // 1. Định nghĩa Mẫu (Pattern) của chuỗi đầu vào
        // EEE: Tên viết tắt của ngày (Thu)
        // MMM: Tên viết tắt của tháng (Feb)
        // dd: Ngày trong tháng (19)
        // HH: Giờ (0-23)
        // mm: Phút
        // ss: Giây
        // z: Múi giờ chung (GMT+07:00)
        // yyyy: Năm (2026)
        String inputPattern = "EEE MMM dd HH:mm:ss z yyyy";

        // 2. Định nghĩa Mẫu của chuỗi đầu ra mong muốn
//        String outputPattern = "HH:mm:ss, dd/MM/yyyy";

        // Sử dụng Locale.ENGLISH vì tên ngày và tháng trong chuỗi đầu vào là tiếng Anh
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);

        // Đặt múi giờ cho đầu vào để đảm bảo chuỗi "GMT+07:00" được hiểu chính xác
        try {
            // Phân tích chuỗi đầu vào thành đối tượng Date
            Date date = inputFormat.parse(inputDateTimeString);

            // Định dạng lại đối tượng Date
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());

            // LƯU Ý: Nếu bạn muốn hiển thị kết quả theo múi giờ địa phương (của thiết bị),
            // bạn không cần setTimeZone. Date object lưu trữ thời điểm (instant)
            // và outputFormat sẽ chuyển đổi nó sang múi giờ mặc định của hệ thống.

            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return "Lỗi phân tích cú pháp ngày giờ.";
        }
    }
    // Formatter NGHIÊM NGẶT NHẤT cho định dạng đầu ra (Ví dụ: 22/10/2025 - 15:22)
    public static LocalDateTime parseLocalTime(String localTimeString) {
        // Pattern: YYYY-MM-DD T HH:mm:ss . <Fractional Seconds/Nanoseconds>
        // 'n' is the nanosecond-of-second field, which handles 1 to 9 digits.
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDateTime.parse(localTimeString, formatter);
        }
        return null;
    }

    // This method would be where your existing code is failing:
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertLocalTimeStringToDisplayString(String localTimeString) {
        try {
            // 1. Parse the string using the custom formatter
            LocalDateTime localDateTime = parseLocalTime(localTimeString);

            // 2. Format the LocalDateTime for display (example format)
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return localDateTime.format(displayFormatter);

        } catch (DateTimeParseException e) {
            System.err.println("Lỗi phân tích cú pháp: " + e.getMessage());
            // Log the exception (which you are already doing)
            // Handle the error gracefully, maybe return an empty string or default message
            return "Invalid Date";
        }
    }
    public static String convertDateToIsoDateTime(String dateStr, String inputFormat, ZoneId zoneId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return dateStr; // Cần giải pháp khác cho API < 26
        }

        try {
            // Định dạng đầu vào (ví dụ: "dd/MM/yyyy")
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat, Locale.ROOT);
            LocalDate date = LocalDate.parse(dateStr, inputFormatter);

            // Kết hợp với thời gian (00:00:00) tại múi giờ địa phương
            ZonedDateTime zonedDateTime = date.atStartOfDay(zoneId);

            // Định dạng đầu ra: 2026-02-19T00:00:00+07:00
            return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        } catch (Exception e) {
            System.err.println("Lỗi chuyển đổi ngày sang ISO DateTime: " + e.getMessage());
            return dateStr;
        }
    }

    public static String formatJoinDateFindTeam(String dateStr){
        OffsetDateTime dateTime = null;
        String formattedTime = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateTime = OffsetDateTime.parse(dateStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", new Locale("vi", "VN"));

// Áp dụng định dạng
            formattedTime = dateTime.format(formatter);
        }

        return formattedTime;
    }

    public static String convertTimeToIsoDuration(String formattedTime) {
        if (formattedTime == null || formattedTime.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "";
        }

        // Định nghĩa mẫu Regex để phân tích HH:mm hoặc HH:mm:ss
        // Group 1: Giờ, Group 2: Phút, Group 3: Giây (tùy chọn)
        Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
        Matcher matcher = pattern.matcher(formattedTime.trim());

        if (!matcher.matches()) {
            System.err.println("Lỗi: Chuỗi thời gian không đúng định dạng (HH:mm hoặc HH:mm:ss): " + formattedTime);
            return "";
        }

        try {
            long hours = Long.parseLong(matcher.group(1));
            long minutes = Long.parseLong(matcher.group(2));
            long seconds = 0;

            // Kiểm tra xem giây có tồn tại không
            if (matcher.group(3) != null) {
                seconds = Long.parseLong(matcher.group(3));
            }

            // Tạo đối tượng Duration từ các thành phần
            Duration duration = Duration.ofHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);

            // Chuyển đổi đối tượng Duration sang chuỗi ISO 8601
            // Ví dụ: PT1H30M
            return duration.toString();

        } catch (NumberFormatException e) {
            System.err.println("Lỗi: Giá trị giờ/phút/giây không hợp lệ trong chuỗi: " + formattedTime);
            return "";
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
