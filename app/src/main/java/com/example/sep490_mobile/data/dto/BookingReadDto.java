package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookingReadDto {
    @SerializedName("Id")
    private int id;

    @SerializedName("UserId")
    private int userId;

    @SerializedName("CreatedById")
    private int createdById;

    @SerializedName("Status")
    private String status;

    @SerializedName("Date")
    private String date;

    @SerializedName("TotalPrice")
    private double totalPrice;

    @SerializedName("OriginalPrice")
    private double originalPrice;

    @SerializedName("Note")
    private String note;

    @SerializedName("PaymentMethod")
    private String paymentMethod;

    @SerializedName("CreatedAt")
    private String createdAt;

    @SerializedName("UpdatedAt")
    private String updatedAt;

    // Key này cũng cần là PascalCase
    @SerializedName("BookingDetails")
    private List<BookingDetailDTO> bookingDetails;

    @SerializedName("StadiumId")
    private int stadiumId;

    public BookingReadDto(int id, int userId, int createdById, String status, String date, double totalPrice, double originalPrice, String note, String paymentMethod, String createdAt, String updatedAt, List<BookingDetailDTO> bookingDetails, int stadiumId) {
        this.id = id;
        this.userId = userId;
        this.createdById = createdById;
        this.status = status;
        this.date = date;
        this.totalPrice = totalPrice;
        this.originalPrice = originalPrice;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookingDetails = bookingDetails;
        this.stadiumId = stadiumId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BookingDetailDTO> getBookingDetails() {
        return bookingDetails;
    }

    public void setBookingDetails(List<BookingDetailDTO> bookingDetails) {
        this.bookingDetails = bookingDetails;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }
}
