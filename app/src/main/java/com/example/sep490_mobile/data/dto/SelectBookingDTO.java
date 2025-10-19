package com.example.sep490_mobile.data.dto;

import java.util.Dictionary;
import java.util.List;

public class SelectBookingDTO {
    public List<ScheduleBookingDTO> scheduleBookingDTOS;
    public Dictionary<Integer, StadiumDTO> stadiums;

    public List<ScheduleBookingDTO> getScheduleBookingDTOS() {
        return scheduleBookingDTOS;
    }

    public void setScheduleBookingDTOS(List<ScheduleBookingDTO> scheduleBookingDTOS) {
        this.scheduleBookingDTOS = scheduleBookingDTOS;
    }

    public Dictionary<Integer, StadiumDTO> getStadiums() {
        return stadiums;
    }

    public void setStadiums(Dictionary<Integer, StadiumDTO> stadiums) {
        this.stadiums = stadiums;
    }
}
