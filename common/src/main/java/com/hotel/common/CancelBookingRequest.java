package com.hotel.common;

import java.io.Serializable;

public class CancelBookingRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int bookingId;
    private int userId;
    private String reason;
    
    public CancelBookingRequest(int bookingId, int userId) {
        this.bookingId = bookingId;
        this.userId = userId;
    }
    
    public CancelBookingRequest(int bookingId, int userId, String reason) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.reason = reason;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
} 