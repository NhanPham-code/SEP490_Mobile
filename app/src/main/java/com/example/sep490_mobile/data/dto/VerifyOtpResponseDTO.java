package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponseDTO {

    @SerializedName("verified")
    private boolean isVerified;

    public boolean isVerified() {
        return isVerified;
    }
}
