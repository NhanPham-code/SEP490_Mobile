package com.example.sep490_mobile.data.dto;

// DTO này có thể không cần import gì cả, hoặc có thể cần SerializedName nếu tên biến khác với JSON
import com.google.gson.annotations.SerializedName;

/**
 * Lớp này dùng để đóng gói dữ liệu khi gửi request tạo hoặc cập nhật Feedback
 * ở dạng JSON (không phải multipart/form-data).
 */
public class FeedbackRequestDto {

    @SerializedName("UserId")
    private int userId;

    @SerializedName("StadiumId")
    private int stadiumId;

    @SerializedName("Rating")
    private int rating;

    @SerializedName("Comment")
    private String comment;

    // Constructor, Getters và Setters

    public FeedbackRequestDto(int userId, int stadiumId, int rating, String comment) {
        this.userId = userId;
        this.stadiumId = stadiumId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}