package com.example.sep490_mobile.data.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StadiumVideosDTO implements Serializable {
    public int id;
    public int stadiumId;
    public String videoUrl;
    public LocalDateTime uploadedAt;
    public StadiumDTO stadium;

    public StadiumVideosDTO() {
    }
    public StadiumVideosDTO(int id, int stadiumId, String videoUrl, LocalDateTime uploadedAt, StadiumDTO stadium) {
        this.id = id;
        this.stadiumId = stadiumId;
        this.videoUrl = videoUrl;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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
