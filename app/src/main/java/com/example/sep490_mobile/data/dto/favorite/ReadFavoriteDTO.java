package com.example.sep490_mobile.data.dto.favorite;

import com.google.gson.annotations.SerializedName;

// DTO đơn giản để nhận ID sân yêu thích từ API
public class ReadFavoriteDTO {

    // Tên trường cần khớp với JSON trả về từ API /myFavoriteStadium
    @SerializedName("favoriteId") // Trước đây có thể là "FavoriteId"
    private int favoriteId;

    @SerializedName("userId") // Trước đây có thể là "UserId"
    private int userId;

    @SerializedName("stadiumId") // Trước đây có thể là "StadiumId"
    private int stadiumId;
    // --- KẾT THÚC SỬA ---


    // Getters (không đổi)
    public int getFavoriteId() { return favoriteId; }
    public int getUserId() { return userId; }
    public int getStadiumId() { return stadiumId; }

    // Setters (không bắt buộc)
    public void setFavoriteId(int favoriteId) { this.favoriteId = favoriteId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setStadiumId(int stadiumId) { this.stadiumId = stadiumId; }
}