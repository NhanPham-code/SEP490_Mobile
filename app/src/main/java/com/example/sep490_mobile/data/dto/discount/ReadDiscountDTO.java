package com.example.sep490_mobile.data.dto.discount;


import com.google.gson.annotations.SerializedName;

public class ReadDiscountDTO {
    @SerializedName("Id") private int id;
    @SerializedName("Code") private String code;
    @SerializedName("Description") private String description;
    @SerializedName("PercentValue") private double percentValue;

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public double getPercentValue() { return percentValue; }
}