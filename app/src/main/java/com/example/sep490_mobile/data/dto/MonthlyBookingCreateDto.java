package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MonthlyBookingCreateDto {

    @SerializedName("stadiumId")
    private int stadiumId;

    @SerializedName("discountId")
    private Integer discountId; // Sử dụng Integer để có thể là null

    @SerializedName("originalPrice")
    private float originalPrice;

    @SerializedName("totalPrice")
    private float totalPrice;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("dates")
    private List<Integer> dates;

    @SerializedName("courtIds")
    private List<Integer> courtIds;

    // Constructor
    public MonthlyBookingCreateDto(int stadiumId, float originalPrice, float totalPrice, String paymentMethod, String startTime, String endTime, int month, int year, List<Integer> dates, List<Integer> courtIds) {
        this.stadiumId = stadiumId;
        this.originalPrice = originalPrice;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.startTime = startTime;
        this.endTime = endTime;
        this.month = month;
        this.year = year;
        this.dates = dates;
        this.courtIds = courtIds;
    }

    public void setDiscountId(Integer discountId) {
        this.discountId = discountId;
    }
}