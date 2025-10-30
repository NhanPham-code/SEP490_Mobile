package com.example.sep490_mobile.data.dto;

import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;

import java.util.Dictionary;
import java.util.List;

public class SelectBookingDTO {
    public List<BookingViewDTO> bookingViewDTOS;
    public Dictionary<Integer, StadiumDTO> stadiums;
    // Constructors, getters, and setters

    public SelectBookingDTO(List<BookingViewDTO> bookingViewDTOS, Dictionary<Integer, StadiumDTO> stadiums) {
        this.bookingViewDTOS = bookingViewDTOS;
        this.stadiums = stadiums;
    }

    public List<BookingViewDTO> getBookingReadDTOS() {
        return bookingViewDTOS;
    }

    public void setBookingReadDTOS(List<BookingViewDTO> bookingViewDTOS) {
        this.bookingViewDTOS = bookingViewDTOS;
    }

    public Dictionary<Integer, StadiumDTO> getStadiums() {
        return stadiums;
    }

    public void setStadiums(Dictionary<Integer, StadiumDTO> stadiums) {
        this.stadiums = stadiums;
    }
}
