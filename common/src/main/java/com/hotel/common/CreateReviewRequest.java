package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на создание отзыва
 */
public class CreateReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;
    private Integer roomId;
    private Integer bookingId;
    private Integer rating;
    private String comment;
    
    // Конструкторы
    public CreateReviewRequest() {
    }
    
    public CreateReviewRequest(Integer userId, Integer roomId, Integer bookingId, Integer rating, String comment) {
        this.userId = userId;
        this.roomId = roomId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Геттеры и сеттеры
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
} 