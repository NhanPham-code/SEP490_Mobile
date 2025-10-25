package com.example.sep490_mobile.model;

import java.util.Date;

public class Feedback {
    private final int id;
    private final int userId;
    private final int stadiumId;
    private final int rating;
    private final String comment;
    private final String imagePath;
    private final Date createdAt;

    public Feedback(int id, int userId, int stadiumId, int rating, String comment, String imagePath, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.stadiumId = stadiumId;
        this.rating = rating;
        this.comment = comment;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getStadiumId() { return stadiumId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getImagePath() { return imagePath; }
    public Date getCreatedAt() { return createdAt; }
}