package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponseDTO {

    @SerializedName("verified")
    private boolean isVerified;

    @SerializedName("message")
    private String message;

    public boolean isVerified() {
        return isVerified;
    }

    public String getMessage() {
        return message;
    }
}
