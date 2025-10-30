package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
// DTO chi tiết được dùng trong BookingReadDTO cũ (Giả định BookingDetailViewModelDTO là DTO chi tiết)
import com.example.sep490_mobile.data.dto.BookingDetailDTO;
import java.util.List;

public class BookingReadDto {

    // --- CÁC TRƯỜNG CHUNG ĐƯỢC ĐỒNG BỘ HÓA ---
    @SerializedName(value = "id", alternate = "Id") private int id;
    @SerializedName(value = "userId", alternate = "UserId") private int userId; // Thêm vào để đồng bộ
    @SerializedName(value = "status", alternate = "Status") private String status;
    @SerializedName(value = "date", alternate = "Date") private String date; // String ISO 8601 cho API
    @SerializedName(value = "totalPrice", alternate = "TotalPrice") private Double totalPrice; // Double cho giá trị null
    @SerializedName(value = "originalPrice", alternate = "OriginalPrice") private Double originalPrice; // Double cho giá trị null
    @SerializedName(value = "note", alternate = "Note") private String note;
    @SerializedName(value = "paymentMethod", alternate = "PaymentMethod") private String paymentMethod;
    @SerializedName(value = "stadiumId", alternate = "StadiumId") private int stadiumId;
    @SerializedName(value = "discountId", alternate = "DiscountId") private Integer discountId; // Integer cho giá trị null
    @SerializedName(value = "monthlyBookingId", alternate = "MonthlyBookingId") private Integer monthlyBookingId; // Integer cho giá trị null

    // --- CÁC TRƯỜNG THỜI GIAN VÀ ID KHÁC ---
    @SerializedName(value = "createdById", alternate = "CreatedById") private int createdById;
    @SerializedName(value = "createdAt", alternate = "CreatedAt") private String createdAt;
    @SerializedName(value = "updatedAt", alternate = "UpdatedAt") private String updatedAt;

    // --- TRƯỜNG CHI TIẾT VÀ UI LOCAL ---
    @SerializedName(value = "bookingDetails", alternate = "BookingDetails")
    private List<BookingDetailDTO> bookingDetails;
    private String stadiumName;


    // Constructor rỗng (cần cho Gson)
    public BookingReadDto() {}

    // Constructor đầy đủ (có thể xóa nếu không cần)
    /*
    public BookingReadDto(int id, int userId, int createdById, String status, String date, Double totalPrice, Double originalPrice, String note, String paymentMethod, String createdAt, String updatedAt, List<BookingDetailViewModelDTO> bookingDetails, int stadiumId, Integer discountId, Integer monthlyBookingId) {
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
        this.discountId = discountId;
        this.monthlyBookingId = monthlyBookingId;
    }
    */

    // === GETTERS ===
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
    public String getDate() { return date; } // String ISO
    public Double getTotalPrice() { return totalPrice; }
    public Double getOriginalPrice() { return originalPrice; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getStadiumId() { return stadiumId; }
    public Integer getDiscountId() { return discountId; }
    public Integer getMonthlyBookingId() { return monthlyBookingId; }
    public int getCreatedById() { return createdById; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getStadiumName() { return stadiumName; }
    public List<BookingDetailDTO> getBookingDetails() { return bookingDetails; }


    // === SETTERS ===
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setDate(String date) { this.date = date; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public void setNote(String note) { this.note = note; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStadiumId(int stadiumId) { this.stadiumId = stadiumId; }
    public void setDiscountId(Integer discountId) { this.discountId = discountId; }
    public void setMonthlyBookingId(Integer monthlyBookingId) { this.monthlyBookingId = monthlyBookingId; }
    public void setCreatedById(int createdById) { this.createdById = createdById; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }
    public void setBookingDetails(List<BookingDetailDTO> bookingDetails) { this.bookingDetails = bookingDetails; }
}