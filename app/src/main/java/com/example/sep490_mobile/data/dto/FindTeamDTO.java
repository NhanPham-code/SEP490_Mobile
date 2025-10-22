package com.example.sep490_mobile.data.dto;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;

public class FindTeamDTO implements Serializable {
    public List<ReadTeamPostDTO> teamPostDTOS;
    public Dictionary<Integer, StadiumDTO> stadiums;
    public Dictionary<Integer, PublicProfileDTO> users;

    public FindTeamDTO(List<ReadTeamPostDTO> teamPostDTOS, Dictionary<Integer, StadiumDTO> stadiums, Dictionary<Integer, PublicProfileDTO> users) {
        this.teamPostDTOS = teamPostDTOS;
        this.stadiums = stadiums;
        this.users = users;
    }

    public List<ReadTeamPostDTO> getTeamPostDTOS() {
        return teamPostDTOS;
    }

    public void setTeamPostDTOS(List<ReadTeamPostDTO> teamPostDTOS) {
        this.teamPostDTOS = teamPostDTOS;
    }

    public Dictionary<Integer, StadiumDTO> getStadiums() {
        return stadiums;
    }

    public void setStadiums(Dictionary<Integer, StadiumDTO> stadiums) {
        this.stadiums = stadiums;
    }

    public Dictionary<Integer, PublicProfileDTO> getUsers() {
        return users;
    }

    public void setUsers(Dictionary<Integer, PublicProfileDTO> users) {
        this.users = users;
    }
}
