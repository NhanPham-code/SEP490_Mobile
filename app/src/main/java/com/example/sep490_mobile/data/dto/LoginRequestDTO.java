package com.example.sep490_mobile.data.dto;

public class LoginRequestDTO {
    private String email;
    private String password;
    private String role;

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = "Customer"; // mobile app only for customer
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
