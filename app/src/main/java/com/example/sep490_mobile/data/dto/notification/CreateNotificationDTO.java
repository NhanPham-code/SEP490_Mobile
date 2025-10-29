package com.example.sep490_mobile.data.dto.notification;

import com.google.gson.annotations.SerializedName;

public class CreateNotificationDTO {

    private Integer userId; // Sử dụng Integer để có thể là null

    private String type;

    private String title;

    private String message;

    private String parameters; // Giữ ở dạng JSON String


    public CreateNotificationDTO(Integer userId, String type, String title, String message, String parameters) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.parameters = parameters;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
