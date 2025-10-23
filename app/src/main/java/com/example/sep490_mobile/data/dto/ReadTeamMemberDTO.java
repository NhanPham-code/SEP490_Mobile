package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReadTeamMemberDTO implements Serializable {
    @SerializedName("Id")
    public int id;

    @SerializedName("TeamPostId")
    public int teamPostId;

    @SerializedName("UserId")
    public int userId;

    @SerializedName("JoinedAt")
    public String joinedAt;

    // Giả sử trong JSON, trường này là "Role" để nhất quán
    @SerializedName("role")
    public String role;

    // Constructor rỗng cần thiết cho Gson
    public ReadTeamMemberDTO() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeamPostId() {
        return teamPostId;
    }

    public void setTeamPostId(int teamPostId) {
        this.teamPostId = teamPostId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
