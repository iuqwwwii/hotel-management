package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на обновление статуса бронирования
 */
public class UpdateBookingStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int bookingId;
    private String newStatus;
    
    public UpdateBookingStatusRequest() {
        // Пустой конструктор
    }
    
    public UpdateBookingStatusRequest(int bookingId, String newStatus) {
        this.bookingId = bookingId;
        this.newStatus = newStatus;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
} 