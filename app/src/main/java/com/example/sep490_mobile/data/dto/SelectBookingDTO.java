package com.example.sep490_mobile.data.dto;

import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;

import java.util.Dictionary;
import java.util.List;

public class SelectBookingDTO {
    public List<BookingReadDTO> bookingReadDTOS;
    public Dictionary<Integer, StadiumDTO> stadiums;
    // Constructors, getters, and setters

    public SelectBookingDTO(List<BookingReadDTO> bookingReadDTOS, Dictionary<Integer, StadiumDTO> stadiums) {
        this.bookingReadDTOS = bookingReadDTOS;
        this.stadiums = stadiums;
    }

    public List<BookingReadDTO> getBookingReadDTOS() {
        return bookingReadDTOS;
    }

    public void setBookingReadDTOS(List<BookingReadDTO> bookingReadDTOS) {
        this.bookingReadDTOS = bookingReadDTOS;
    }

    public Dictionary<Integer, StadiumDTO> getStadiums() {
        return stadiums;
    }

    public void setStadiums(Dictionary<Integer, StadiumDTO> stadiums) {
        this.stadiums = stadiums;
    }
}
