package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CourtsDTO implements Serializable {
    @SerializedName("Id")
    public int id;
    @SerializedName("StadiumId")
    public int stadiumId;
    @SerializedName("Name")
    public String name;
    @SerializedName("SportType")
    public String sportType;
    @SerializedName("PricePerHour")
    public long pricePerHour;
    @SerializedName("IsAvailable")
    public boolean isAvailable;
    @SerializedName("CreatedAt")
    public String createdAt;
    @SerializedName("UpdatedAt")
    public String updatedAt;
    @SerializedName("Stadium")
    public StadiumDTO stadium;

    public CourtsDTO() {

    }

    public CourtsDTO(int id, int stadiumId, String name, String sportType, long pricePerHour, boolean isAvailable, String createdAt, String updatedAt, StadiumDTO stadiumDTO) {
        this.id = id;
        this.stadiumId = stadiumId;
        this.name = name;
        this.sportType = sportType;
        this.pricePerHour = pricePerHour;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stadium = stadiumDTO;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public long getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(long pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
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

    public StadiumDTO getStadium() {
        return stadium;
    }

    public void setStadium(StadiumDTO stadium) {
        this.stadium = stadium;
    }
}
