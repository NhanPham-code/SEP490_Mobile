package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// DTO chung cho bất kỳ phản hồi OData nào có $count=true
public class OdataHaveCountResponse<T> {

    @SerializedName("@odata.context")
    private String oDataContext;

    @SerializedName("@odata.count")
    private int count;

    @SerializedName("value")
    private List<T> value;

    public String getODataContext() { return oDataContext; }
    public int getCount() { return count; }
    public List<T> getValue() { return value; }
}