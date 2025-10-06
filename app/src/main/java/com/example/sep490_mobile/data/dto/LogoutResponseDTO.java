package com.example.sep490_mobile.data.dto;

public class LogoutResponseDTO {
    private boolean isValid;
    private String message;

    public LogoutResponseDTO(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
