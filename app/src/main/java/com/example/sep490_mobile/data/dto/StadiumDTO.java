package com.example.sep490_mobile.data.dto;

import android.icu.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class StadiumDTO implements Serializable {
    // Ánh xạ từ Id (C# PascalCase)
    @SerializedName("Id")
    public int id;

    // Ánh xạ từ Name
    @SerializedName("Name")
    public String name;

    @SerializedName("NameUnsigned")
    public String nameUnsigned;

    @SerializedName("Address")
    public String address;

    @SerializedName("AddressUnsigned")
    public String addressUnsigned;

    @SerializedName("Description")
    public String description;

    // Ánh xạ từ TimeSpan OpenTime -> Sử dụng LocalTime trong Java
    @SerializedName("OpenTime")
    public Duration openTime;

    // Ánh xạ từ TimeSpan CloseTime -> Sử dụng LocalTime trong Java
    @SerializedName("CloseTime")
    public Duration closeTime;

    // Ánh xạ từ decimal? Latitude -> Sử dụng BigDecimal trong Java
    @SerializedName("Latitude")
    public Double  latitude;

    // Ánh xạ từ decimal? Longitude -> Sử dụng BigDecimal trong Java
    @SerializedName("Longitude")
    public Double  longitude;

    @SerializedName("IsApproved")
    public boolean isApproved = false;

    @SerializedName("CreatedBy")
    public int createdBy;

    // Ánh xạ từ DateTime CreatedAt -> Sử dụng LocalDateTime trong Java
    @SerializedName("CreatedAt")
    public String createdAt;

    // Ánh xạ từ DateTime UpdatedAt -> Sử dụng LocalDateTime trong Java
    @SerializedName("UpdatedAt")
    public String updatedAt;

    @SerializedName("IsLocked")
    public boolean isLocked;

    // Ánh xạ từ ICollection<Courts>
    @SerializedName("Courts")
    public Set<CourtsDTO> courts;

    @SerializedName("StadiumImages")
    public Set<StadiumImagesDTO> stadiumImages;

    @SerializedName("StadiumVideos")
    public Set<StadiumVideosDTO> stadiumVideos;

    public StadiumDTO() {

    }

    public StadiumDTO(int id, String name, String nameUnsigned, String address, String addressUnsigned, String description, Duration  openTime, Duration  closeTime, Double  latitude, Double  longitude, boolean isApproved, int createdBy, String createdAt, String updatedAt, boolean isLocked, Set<CourtsDTO> courts, Set<StadiumImagesDTO> stadiumImages, Set<StadiumVideosDTO> stadiumVideos) {
        this.id = id;
        this.name = name;
        this.nameUnsigned = nameUnsigned;
        this.address = address;
        this.addressUnsigned = addressUnsigned;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isApproved = isApproved;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLocked = isLocked;
        this.courts = courts;
        this.stadiumImages = stadiumImages;
        this.stadiumVideos = stadiumVideos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameUnsigned() {
        return nameUnsigned;
    }

    public void setNameUnsigned(String nameUnsigned) {
        this.nameUnsigned = nameUnsigned;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressUnsigned() {
        return addressUnsigned;
    }

    public void setAddressUnsigned(String addressUnsigned) {
        this.addressUnsigned = addressUnsigned;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration  getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Duration  openTime) {
        this.openTime = openTime;
    }

    public Duration  getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Duration  closeTime) {
        this.closeTime = closeTime;
    }

    public Double  getLatitude() {
        return latitude;
    }

    public void setLatitude(Double  latitude) {
        this.latitude = latitude;
    }

    public Double  getLongitude() {
        return longitude;
    }

    public void setLongitude(Double  longitude) {
        this.longitude = longitude;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Set<CourtsDTO> getCourts() {
        return courts;
    }

    public void setCourts(Set<CourtsDTO> courts) {
        this.courts = courts;
    }

    public Set<StadiumImagesDTO> getStadiumImages() {
        return stadiumImages;
    }

    public void setStadiumImages(Set<StadiumImagesDTO> stadiumImages) {
        this.stadiumImages = stadiumImages;
    }

    public Set<StadiumVideosDTO> getStadiumVideos() {
        return stadiumVideos;
    }

    public void setStadiumVideos(Set<StadiumVideosDTO> stadiumVideos) {
        this.stadiumVideos = stadiumVideos;
    }
}
