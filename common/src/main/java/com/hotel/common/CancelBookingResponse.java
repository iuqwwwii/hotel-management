package com.hotel.common;

import java.io.Serializable;

public class CancelBookingResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private BookingDTO updatedBooking;
    
    public CancelBookingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public CancelBookingResponse(boolean success, String message, BookingDTO updatedBooking) {
        this.success = success;
        this.message = message;
        this.updatedBooking = updatedBooking;
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
    
    public BookingDTO getUpdatedBooking() {
        return updatedBooking;
    }
    
    public void setUpdatedBooking(BookingDTO updatedBooking) {
        this.updatedBooking = updatedBooking;
    }
} 