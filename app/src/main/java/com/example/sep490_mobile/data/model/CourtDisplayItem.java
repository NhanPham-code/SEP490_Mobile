package com.example.sep490_mobile.data.model;

import com.example.sep490_mobile.data.dto.CourtsDTO;

public abstract class CourtDisplayItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_COURT = 1;

    abstract public int getType();

    public static class Header extends CourtDisplayItem {
        private final String sportType;

        public Header(String sportType) {
            this.sportType = sportType;
        }

        public String getSportType() {
            return sportType;
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    public static class CourtItem extends CourtDisplayItem {
        private final CourtsDTO court;

        public CourtItem(CourtsDTO court) {
            this.court = court;
        }

        public CourtsDTO getCourt() {
            return court;
        }

        @Override
        public int getType() {
            return TYPE_COURT;
        }
    }
}