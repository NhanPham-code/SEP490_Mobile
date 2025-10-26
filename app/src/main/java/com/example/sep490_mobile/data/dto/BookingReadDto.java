package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookingReadDto {
    @SerializedName(value = "id", alternate = "Id")
    private int id;

    @SerializedName(value = "userId", alternate = "UserId")
    private int userId;

    @SerializedName(value = "createdById", alternate = "CreatedById")
    private int createdById;

    @SerializedName(value = "status", alternate = "Status")
    private String status;

    // "date" (thường) là ưu tiên, "Date" (hoa) là dự phòng
    @SerializedName(value = "date", alternate = "Date")
    private String date;

    @SerializedName(value = "totalPrice", alternate = "TotalPrice")
    private double totalPrice;

    @SerializedName(value = "originalPrice", alternate = "OriginalPrice")
    private double originalPrice;

    @SerializedName(value = "note", alternate = "Note")
    private String note;

    @SerializedName(value = "paymentMethod", alternate = "PaymentMethod")
    private String paymentMethod;

    @SerializedName(value = "createdAt", alternate = "CreatedAt")
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = "UpdatedAt")
    private String updatedAt;

    @SerializedName(value = "bookingDetails", alternate = "BookingDetails")
    private List<BookingDetailDTO> bookingDetails;

    @SerializedName(value = "stadiumId", alternate = "StadiumId")
    private int stadiumId;

    // Constructor và Getters/Setters không thay đổi
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