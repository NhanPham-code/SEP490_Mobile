package com.example.sep490_mobile.adapter;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DurationTypeAdapter extends TypeAdapter<Duration> {

    // Pattern để tìm số giờ trong chuỗi "PT<số>H"
    private static final Pattern DURATION_PATTERN = Pattern.compile("PT(\\d+)H");

    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String durationString = in.nextString(); // Ví dụ: "PT6H"

        if (durationString == null || durationString.isEmpty()) {
            return null;
        }

        // SỬA LỖI: Phân tích chuỗi "PT...H" thủ công để đảm bảo hoạt động
        Matcher matcher = DURATION_PATTERN.matcher(durationString);
        if (matcher.matches()) {
            try {
                // Lấy ra nhóm số (ví dụ: "6" từ "PT6H")
                String hoursString = matcher.group(1);
                if (hoursString != null) {
                    long hours = Long.parseLong(hoursString);
                    return Duration.ofHours(hours);
                }
            } catch (NumberFormatException e) {
                System.err.println("Lỗi NumberFormatException khi parse duration: " + durationString);
                e.printStackTrace();
                return null; // Trả về null nếu số không hợp lệ
            }
        }

        // Nếu không khớp pattern, ghi log và trả về null
        System.err.println("Chuỗi duration không khớp pattern 'PT<n>H': " + durationString);
        return null;
    }
}