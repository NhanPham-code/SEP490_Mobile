package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StadiumVideosDTO implements Serializable {
    @SerializedName("Id")
    public int id;
    @SerializedName("StadiumId")
    public int stadiumId;
    @SerializedName("VideoUrl")
    public String videoUrl;
    @SerializedName("UploadedAt")
    public String uploadedAt;
    @SerializedName("Stadium")
    public StadiumDTO stadium;

    public StadiumVideosDTO() {
    }
    public StadiumVideosDTO(int id, int stadiumId, String videoUrl, String uploadedAt, StadiumDTO stadium) {
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
