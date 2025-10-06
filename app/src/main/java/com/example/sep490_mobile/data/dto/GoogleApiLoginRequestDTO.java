package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class GoogleApiLoginRequestDTO {
    @SerializedName("idToken")
    private String idToken;

    public GoogleApiLoginRequestDTO(String idToken) {
        this.idToken = idToken;
    }
}
