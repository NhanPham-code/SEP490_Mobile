package com.example.sep490_mobile.Adapter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalTypeAdapter extends TypeAdapter<BigDecimal> {
    @Override
    public void write(JsonWriter out, BigDecimal value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Ghi giá trị (Serialize)
        out.value(value);
    }

    @Override
    public BigDecimal read(JsonReader in) throws IOException {
        // Sửa lỗi: Đổi 'reader' thành 'in'
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // Đọc giá trị JSON (NUMBER) dưới dạng chuỗi (STRING) để tránh lỗi và mất độ chính xác
        String numberString = in.nextString();

        try {
            // SỬ DỤNG java.math.BigDecimal
            return new BigDecimal(numberString);
        } catch (NumberFormatException e) {
            // Ném ngoại lệ JsonSyntaxException để Gson/Retrofit có thể bắt được
            throw new JsonSyntaxException("Invalid BigDecimal format: " + numberString, e);
        }
    }
}
