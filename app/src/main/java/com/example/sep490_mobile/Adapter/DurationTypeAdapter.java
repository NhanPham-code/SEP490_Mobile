package com.example.sep490_mobile.Adapter;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonDeserializer<Duration> {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Đảm bảo lấy chuỗi (STRING) trước khi gọi parse
        String durationString = json.getAsString();

        // Sử dụng phương thức chuẩn của Java Time API để phân tích chuỗi ISO 8601

        return Duration.parse(durationString);

    }
}
