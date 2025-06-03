package com.hotel.common;

import java.io.Serializable;
import java.util.List;

public class GetGuestBookingsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<BookingDTO> bookings;
    private boolean success;
    private String message;
    
    public GetGuestBookingsResponse(List<BookingDTO> bookings, boolean success, String message) {
        this.bookings = bookings;
        this.success = success;
        this.message = message;
    }
    
    public GetGuestBookingsResponse(List<BookingDTO> bookings) {
        this.bookings = bookings;
        this.success = true;
        this.message = "Бронирования успешно загружены";
    }
    
    public GetGuestBookingsResponse(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }
    
    public List<BookingDTO> getBookings() {
        return bookings;
    }
    
    public void setBookings(List<BookingDTO> bookings) {
        this.bookings = bookings;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
} 