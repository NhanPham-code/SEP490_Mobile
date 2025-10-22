package com.example.sep490_mobile.data.dto;

public class CourtHeaderItem {
    private String courtName;
    private String sportType;
    private boolean showSportType;
    private boolean isCenterOfGroup;

    public CourtHeaderItem(String courtName, String sportType, boolean showSportType, boolean isCenterOfGroup) {
        this.courtName = courtName;
        this.sportType = sportType;
        this.showSportType = showSportType;
        this.isCenterOfGroup = isCenterOfGroup;
    }

    public String getCourtName() { return courtName; }
    public String getSportType() { return sportType; }
    public boolean isShowSportType() { return showSportType; }
    public boolean isCenterOfGroup() { return isCenterOfGroup; }
}