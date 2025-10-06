package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ApiErrorResponseDTO {
    @SerializedName("errors")
    private Map<String, List<String>> errors;

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    /**
     * Lấy ra thông báo lỗi đầu tiên từ danh sách lỗi.
     * @return Một chuỗi thông báo lỗi hoặc null nếu không có.
     */
    public String getFirstErrorMessage() {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        // Lấy ra giá trị (danh sách các chuỗi lỗi) của entry đầu tiên trong map
        List<String> errorMessages = errors.values().iterator().next();
        if (errorMessages != null && !errorMessages.isEmpty()) {
            return errorMessages.get(0);
        }
        return null;
    }
}
