package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class FeedbackRequestDto {
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

    public FeedbackRequestDto(int userId, int stadiumId, int rating, String comment, String imagePath) {
        this.userId = userId;
        this.stadiumId = stadiumId;
        this.rating = rating;
        this.comment = comment;
        this.imagePath = imagePath;
    }
}