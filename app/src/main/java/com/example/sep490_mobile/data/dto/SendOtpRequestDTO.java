package com.example.sep490_mobile.data.dto;

public class SendOtpRequestDTO {
    private String email;

    public SendOtpRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
