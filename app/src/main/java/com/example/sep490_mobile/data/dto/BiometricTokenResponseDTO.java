package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class BiometricTokenResponseDTO {
    @SerializedName("biometricToken")
    private String biometricToken;

    public String getBiometricToken() {
        return biometricToken;
    }

    public void setBiometricToken(String biometricToken) {
        this.biometricToken = biometricToken;
    }
}
