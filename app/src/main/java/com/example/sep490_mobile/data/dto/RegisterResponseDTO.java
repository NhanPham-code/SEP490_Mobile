package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterResponseDTO {
    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private PrivateUserProfileDTO user;

    public String getMessage() {
        return message;
    }

    public PrivateUserProfileDTO getUser() {
        return user;
    }
}
