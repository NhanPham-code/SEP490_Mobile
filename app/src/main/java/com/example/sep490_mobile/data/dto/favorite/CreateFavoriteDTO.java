package com.example.sep490_mobile.data.dto.favorite;

public class CreateFavoriteDTO {
    private int userId;
    private int stadiumId;

    public CreateFavoriteDTO() {}

    public CreateFavoriteDTO(int userId, int stadiumId) {
        this.userId = userId;
        this.stadiumId = stadiumId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }
}
