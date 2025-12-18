package com.example.sep490_mobile.adapter;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // value.toString() sẽ tự động tạo chuỗi đúng chuẩn, ví dụ: "PT6H"
            out.value(value.toString());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String durationString = in.nextString();

        try {
            // Dùng phương thức parse() có sẵn, nó sẽ xử lý được "PT6H", "PT21H30M", v.v.
            return Duration.parse(durationString);
        } catch (DateTimeParseException e) {
            // Ghi log lỗi nếu chuỗi không hợp lệ và trả về null
            System.err.println("Lỗi parse chuỗi Duration: " + durationString);
            e.printStackTrace();
            return null;
        }
    }
}