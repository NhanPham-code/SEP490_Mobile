package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PublicProfileDTO implements Serializable {
    @SerializedName("@odata.type")
    public String odataType;
    @SerializedName("UserId")
    public int id;
    @SerializedName("FullName")
    public String fullName;
    @SerializedName("PhoneNumber")
    public String phoneNumber;
    @SerializedName("Email")
    public String email;
    @SerializedName("AvatarUrl")
    public String avatarUrl;

    public PublicProfileDTO(int id, String fullName, String phoneNumber, String email, String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
