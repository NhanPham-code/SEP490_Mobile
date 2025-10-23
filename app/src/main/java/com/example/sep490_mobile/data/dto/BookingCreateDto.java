package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingCreateDto {

    @SerializedName("UserId")
    private int userId;

    @SerializedName("Status")
    private String status;

    @SerializedName("Date")
    private String date; // "yyyy-MM-dd"

    @SerializedName("TotalPrice")
    private Double totalPrice; // Use Double for flexibility

    @SerializedName("OriginalPrice")
    private Double originalPrice; // Use Double

    @SerializedName("Note")
    private String note;

    @SerializedName("PaymentMethod")
    private String paymentMethod;

    @SerializedName("DiscountId")
    private Integer discountId; // Integer for nullable int

    @SerializedName("StadiumId")
    private int stadiumId;

    @SerializedName("BookingDetails")
    private List<BookingDetailCreateDto> bookingDetails;

    // Constructor to easily create the object
    public BookingCreateDto(int userId, String status, String date, Double totalPrice, Double originalPrice, String paymentMethod, Integer discountId, int stadiumId, List<BookingDetailCreateDto> bookingDetails) {
        this.userId = userId;
        this.status = status;
        this.date = date;
        this.totalPrice = totalPrice;
        this.originalPrice = originalPrice;
        this.paymentMethod = paymentMethod;
        this.discountId = discountId;
        this.stadiumId = stadiumId;
        this.bookingDetails = bookingDetails;
        this.note = ""; // Default note to empty string if needed
    }

    // Getters if needed
}