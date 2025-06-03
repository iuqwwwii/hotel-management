package com.hotel.common;

import java.io.Serializable;
import java.util.List;

public class FetchBookingsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<BookingDTO> bookings;
    private String message;

    public FetchBookingsResponse(List<BookingDTO> bookings, String ok, boolean b) {}

    public FetchBookingsResponse(List<BookingDTO> bookings, String message) {
        this.bookings = bookings;
        this.message  = message;
    }

    public List<BookingDTO> getBookings() {
        return bookings;
    }

    public void setBookings(List<BookingDTO> bookings) {
        this.bookings = bookings;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
