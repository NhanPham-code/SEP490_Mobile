package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StadiumImagesDTO implements Serializable {
    // Ánh xạ từ Id (C# PascalCase)
    @SerializedName("Id")
    public int id;
    // Ánh xạ từ StadiumId (C# PascalCase)
    @SerializedName("StadiumId")
    public int stadiumId;
    // Ánh xạ từ ImageUrl (C# PascalCase)
    @SerializedName("ImageUrl")
    public String imageUrl;
    // Ánh xạ từ DateTime UploadedAt (C# PascalCase)
    @SerializedName("UploadedAt")
    public String uploadedAt;
    @SerializedName("Stadium")
    public StadiumDTO stadium;

    public StadiumImagesDTO() {
    }

    public StadiumImagesDTO(int id, int stadiumId, String imageUrl, String uploadedAt, StadiumDTO stadium) {
        this.id = id;
        this.stadiumId = stadiumId;
        this.imageUrl = imageUrl;
        this.uploadedAt = uploadedAt;
        this.stadium = stadium;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public StadiumDTO getStadium() {
        return stadium;
    }

    public void setStadium(StadiumDTO stadium) {
        this.stadium = stadium;
    }
}
