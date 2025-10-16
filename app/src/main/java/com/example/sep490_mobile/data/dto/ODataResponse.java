package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ODataResponse<T> {
    @SerializedName("value")
    private List<T> items;

    // Ánh xạ tới khóa "@odata.count" (lưu ý ký tự @)
    // Dùng kiểu Long hoặc Integer để lưu số lượng
    @SerializedName("@odata.count")
    private Long count;

    // Ánh xạ tới "@odata.context" (Không bắt buộc)
    @SerializedName("@odata.context")
    private String odataContext;

    // --- Getters ---

    public List<T> getItems() {
        return items;
    }

    public Long getCount() {
        return count;
    }
}
