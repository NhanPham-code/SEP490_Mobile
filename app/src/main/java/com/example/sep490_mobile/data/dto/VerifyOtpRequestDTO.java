package com.example.sep490_mobile.data.dto;

public class VerifyOtpRequestDTO {
    private String email;
    private String code;

    public VerifyOtpRequestDTO(String email, String otp) {
        this.email = email;
        this.code = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
