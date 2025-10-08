package com.example.sep490_mobile.model;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CourtsData implements Serializable {
    public int id;
    public int stadiumId;
    public String name;
    public String sportType;
    public long pricePerHour;
    public boolean isAvailable;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public StadiumData stadiumData;

    public CourtsData() {

    }

    public CourtsData(int id, int stadiumId, String name, String sportType, long pricePerHour, boolean isAvailable, LocalDateTime createdAt, LocalDateTime updatedAt, StadiumData stadiumData) {
        this.id = id;
        this.stadiumId = stadiumId;
        this.name = name;
        this.sportType = sportType;
        this.pricePerHour = pricePerHour;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stadiumData = stadiumData;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public StadiumData getStadiumData() {
        return stadiumData;
    }

    public void setStadiumData(StadiumData stadiumData) {
        this.stadiumData = stadiumData;
    }
}
