package com.hotel.common;

import java.io.Serializable;
import java.util.List;

/**
 * Ответ на запрос получения всех бронирований
 */
public class GetAllBookingsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<BookingDTO> bookings;
    private boolean success;
    private String message;
    
    public GetAllBookingsResponse() {
        // Пустой конструктор
    }
    
    public GetAllBookingsResponse(List<BookingDTO> bookings) {
        this.bookings = bookings;
        this.success = true;
    }
    
    public GetAllBookingsResponse(String errorMessage) {
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