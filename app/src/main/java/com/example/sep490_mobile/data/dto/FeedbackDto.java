package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class FeedbackDto {
    @SerializedName("Id") // Khớp với C# Model
    private int id;

    @SerializedName("UserId")
    private int userId;

    @SerializedName("StadiumId")
    private int stadiumId;

    @SerializedName("Rating")
    private int rating;

    @SerializedName("Comment")
    private String comment;

    @SerializedName("ImagePath")
    private String imagePath;

    @SerializedName("CreatedAt")
    private String createdAt; // Nhận dưới dạng String từ API

    // --- Getters ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getStadiumId() { return stadiumId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getImagePath() { return imagePath; }
    public String getCreatedAt() { return createdAt; }
}