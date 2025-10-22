package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ReadTeamPostResponse implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("location")
    public String location;

    @SerializedName("sportType")
    public String sportType;

    @SerializedName("joinedPlayers")
    public int joinedPlayers;

    @SerializedName("neededPlayers")
    public int neededPlayers;

    @SerializedName("pricePerPerson")
    public double pricePerPerson;

    @SerializedName("description")
    public String description;

    @SerializedName("timePlay")
    public String timePlay;

    @SerializedName("playDate")
    public String playDate;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("createdBy")
    public int createdBy;

    @SerializedName("stadiumName")
    public String stadiumName;

    @SerializedName("stadiumId")
    public int stadiumId;

    @SerializedName("bookingId")
    public int bookingId;


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public int getJoinedPlayers() { return joinedPlayers; }
    public void setJoinedPlayers(int joinedPlayers) { this.joinedPlayers = joinedPlayers; }

    public int getNeededPlayers() { return neededPlayers; }
    public void setNeededPlayers(int neededPlayers) { this.neededPlayers = neededPlayers; }

    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimePlay() { return timePlay; }
    public void setTimePlay(String timePlay) { this.timePlay = timePlay; }

    public String getPlayDate() { return playDate; }
    public void setPlayDate(String playDate) { this.playDate = playDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getStadiumName() { return stadiumName; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }

    public int getStadiumId() { return stadiumId; }
    public void setStadiumId(int stadiumId) { this.stadiumId = stadiumId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

}
