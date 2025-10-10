package com.example.sep490_mobile.data.dto;

public class ResetPasswordResponseDTO {
    private String message;

    public ResetPasswordResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
