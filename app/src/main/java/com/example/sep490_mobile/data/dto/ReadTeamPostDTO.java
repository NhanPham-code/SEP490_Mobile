package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ReadTeamPostDTO implements Serializable {
    @SerializedName("Id")
    public int id;

    @SerializedName("Title")
    public String title;

    @SerializedName("Location")
    public String location;

    @SerializedName("SportType")
    public String sportType;

    @SerializedName("JoinedPlayers")
    public int joinedPlayers;

    @SerializedName("NeededPlayers")
    public int neededPlayers;

    @SerializedName("PricePerPerson")
    public double pricePerPerson;

    @SerializedName("Description")
    public String description;

    @SerializedName("TimePlay")
    public String timePlay;

    @SerializedName("PlayDate")
    public String playDate;

    @SerializedName("CreatedAt")
    public String createdAt;

    @SerializedName("UpdatedAt")
    public String updatedAt;

    @SerializedName("CreatedBy")
    public int createdBy;

    @SerializedName("StadiumName")
    public String stadiumName;

    @SerializedName("StadiumId")
    public int stadiumId;

    @SerializedName("BookingId")
    public int bookingId;

    @SerializedName("TeamMembers")
    public List<ReadTeamMemberDTO> teamMembers;

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

    public List<ReadTeamMemberDTO> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<ReadTeamMemberDTO> teamMembers) { this.teamMembers = teamMembers; }
}
