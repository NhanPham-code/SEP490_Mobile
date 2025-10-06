package com.example.sep490_mobile.model;

import java.io.Serializable;

public class RegistrationData implements Serializable {
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String password;

    // Constructors, Getters và Setters
    public RegistrationData(String fullName, String email, String phone, String dateOfBirth, String password) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getPassword() { return password; }
}
