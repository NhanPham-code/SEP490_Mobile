package com.example.sep490_mobile.data.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StadiumImagesDTO implements Serializable {
    public int id;
    public int stadiumId;
    public String imageUrl;
    public LocalDateTime uploadedAt;
    public StadiumDTO stadium;

    public StadiumImagesDTO() {
    }

    public StadiumImagesDTO(int id, int stadiumId, String imageUrl, LocalDateTime uploadedAt, StadiumDTO stadium) {
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public StadiumDTO getStadium() {
        return stadium;
    }

    public void setStadium(StadiumDTO stadium) {
        this.stadium = stadium;
    }
}
