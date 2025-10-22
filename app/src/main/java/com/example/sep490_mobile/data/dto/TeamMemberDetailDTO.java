package com.example.sep490_mobile.data.dto;

import java.util.Dictionary;
import java.util.List;

public class TeamMemberDetailDTO {
    public List<ReadTeamMemberForDetailDTO> member;
    public Dictionary<Integer, PublicProfileDTO> user;

    public TeamMemberDetailDTO() {
    }

    public List<ReadTeamMemberForDetailDTO> getMember() {
        return member;
    }

    public void setMember(List<ReadTeamMemberForDetailDTO> member) {
        this.member = member;
    }

    public Dictionary<Integer, PublicProfileDTO> getUser() {
        return user;
    }

    public void setUser(Dictionary<Integer, PublicProfileDTO> user) {
        this.user = user;
    }
}
