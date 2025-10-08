package com.example.sep490_mobile.model;

import android.icu.math.BigDecimal;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class StadiumData implements Serializable {
    public int id;

    // [Required], [MaxLength(255)]
    public String name;

    public String nameUnsigned;

    public String address;
    public String addressUnsigned;

    public String description;

    // [Required] - Sử dụng LocalTime cho TimeSpan
    public LocalTime openTime;

    // [Required] - Sử dụng LocalTime cho TimeSpan
    public LocalTime closeTime;

    // [Column(TypeName = "decimal(9,6)")] - Sử dụng BigDecimal cho Decimal
    public BigDecimal latitude;

    // [Column(TypeName = "decimal(9,6)")] - Sử dụng BigDecimal cho Decimal
    public BigDecimal longitude;

    public boolean isApproved = false; // Mặc định là false

    public int createdBy;

    // Sử dụng LocalDateTime cho DateTime
    public LocalDateTime createdAt;

    // Sử dụng LocalDateTime cho DateTime
    public LocalDateTime updatedAt;

    public boolean isLocked;

    // Các mối quan hệ (Relationships) - Sử dụng Set/List cho ICollection
    public Set<CourtsData> courts; // Cần tạo class Courts tương ứng
    public Set<StadiumImagesData> stadiumImages; // Cần tạo class StadiumImages tương ứng
    public Set<StadiumVideosData> stadiumVideos; // Cần tạo class StadiumVideos tương ứng

    public StadiumData() {

    }

    public StadiumData(int id, String name, String nameUnsigned, String address, String addressUnsigned, String description, LocalTime openTime, LocalTime closeTime, BigDecimal latitude, BigDecimal longitude, boolean isApproved, int createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isLocked, Set<CourtsData> courts, Set<StadiumImagesData> stadiumImages, Set<StadiumVideosData> stadiumVideos) {
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

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
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

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Set<CourtsData> getCourts() {
        return courts;
    }

    public void setCourts(Set<CourtsData> courts) {
        this.courts = courts;
    }

    public Set<StadiumImagesData> getStadiumImages() {
        return stadiumImages;
    }

    public void setStadiumImages(Set<StadiumImagesData> stadiumImages) {
        this.stadiumImages = stadiumImages;
    }

    public Set<StadiumVideosData> getStadiumVideos() {
        return stadiumVideos;
    }

    public void setStadiumVideos(Set<StadiumVideosData> stadiumVideos) {
        this.stadiumVideos = stadiumVideos;
    }
}
