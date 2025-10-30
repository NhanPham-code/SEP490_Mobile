package com.example.sep490_mobile.data.dto.booking;

import java.util.List;

// DTO này khớp với backend DTO BookingUpdateDto
public class BookingUpdateDTO {
    public int UserId; // int
    public String Status; // String
    public String Date; // String
    public Double TotalPrice; // Double
    public Double OriginalPrice; // Double
    public String Note; // String
    public Integer DiscountId; // Integer
    public int StadiumId; // int

    // Bỏ qua BookingDetails nếu API update không cần hoặc không hỗ trợ cập nhật chi tiết

    // Constructor mặc định (cần cho Gson)
    public BookingUpdateDTO() {}

    // Constructor tiện lợi (để tạo DTO update từ BookingReadDto)
    public BookingUpdateDTO(int userId, String status, String date, Double totalPrice, Double originalPrice, String note, Integer discountId, int stadiumId) {
        UserId = userId;
        Status = status;
        Date = date; // Format phải đúng yêu cầu API
        TotalPrice = totalPrice;
        OriginalPrice = originalPrice;
        Note = note;
        DiscountId = discountId;
        StadiumId = stadiumId;
    }

    // === GETTERS VÀ SETTERS ===

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public Double getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        TotalPrice = totalPrice;
    }

    public Double getOriginalPrice() {
        return OriginalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        OriginalPrice = originalPrice;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public Integer getDiscountId() {
        return DiscountId;
    }

    public void setDiscountId(Integer discountId) {
        DiscountId = discountId;
    }

    public int getStadiumId() {
        return StadiumId;
    }

    public void setStadiumId(int stadiumId) {
        StadiumId = stadiumId;
    }
}