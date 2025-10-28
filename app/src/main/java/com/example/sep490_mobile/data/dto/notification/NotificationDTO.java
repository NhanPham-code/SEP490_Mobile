package com.example.sep490_mobile.data.dto.notification;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class NotificationDTO {
    @SerializedName("Id")
    private int id;

    @SerializedName("UserId")
    private Integer userId; // Sử dụng Integer để có thể là null

    @SerializedName("Type")
    private String type;

    @SerializedName("Title")
    private String title;

    @SerializedName("Message")
    private String message;

    @SerializedName("Parameters")
    private String parameters; // Giữ ở dạng JSON String

    @SerializedName("IsRead")
    private boolean isRead;

    @SerializedName("CreatedAt")
    private Date createdAt; // Thời gian tạo thông báo


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
