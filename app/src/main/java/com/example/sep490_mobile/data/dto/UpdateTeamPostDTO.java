package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdateTeamPostDTO implements Serializable {

        // Ánh xạ "Id"
        @SerializedName("id")
        private int id;

        // Ánh xạ "Title"
        @SerializedName("title")
        private String title = "";

        // Ánh xạ "JoinedPlayers"
        @SerializedName("joinedPlayers")
        private int joinedPlayers;

        // Ánh xạ "NeededPlayers"
        @SerializedName("neededPlayers")
        private int neededPlayers;

        // Ánh xạ "PricePerPerson" (sử dụng double cho Decimal)
        @SerializedName("pricePerPerson")
        private double pricePerPerson;

        // Ánh xạ "Description"
        @SerializedName("description")
        private String description = "";

        // Ánh xạ "UpdatedAt" (sử dụng Date cho DateTime)
        @SerializedName("updatedAt")
        private String updatedAt;

        // --- Constructor (Hàm khởi tạo) ---
        // Constructor đầy đủ (có thể bỏ qua nếu chỉ dùng Gson cho deserialization)
        public UpdateTeamPostDTO(int id, String title, int joinedPlayers, int neededPlayers, double pricePerPerson, String description, String updatedAt) {
            this.id = id;
            this.title = title;
            this.joinedPlayers = joinedPlayers;
            this.neededPlayers = neededPlayers;
            this.pricePerPerson = pricePerPerson;
            this.description = description;
            this.updatedAt = updatedAt;
        }

        // Constructor rỗng cần thiết cho việc deserialize JSON
        public UpdateTeamPostDTO() {}


        // --- Getters và Setters ---

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getJoinedPlayers() {
            return joinedPlayers;
        }

        public void setJoinedPlayers(int joinedPlayers) {
            this.joinedPlayers = joinedPlayers;
        }

        public int getNeededPlayers() {
            return neededPlayers;
        }

        public void setNeededPlayers(int neededPlayers) {
            this.neededPlayers = neededPlayers;
        }

        public double getPricePerPerson() {
            return pricePerPerson;
        }

        public void setPricePerPerson(double pricePerPerson) {
            this.pricePerPerson = pricePerPerson;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
}
