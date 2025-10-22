package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdateTeamMemberDTO implements Serializable {
    @SerializedName("id")
    public int id;
    @SerializedName("role")
    public String role;

    // Constructor


    public UpdateTeamMemberDTO(int id, String role) {
        this.id = id;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
