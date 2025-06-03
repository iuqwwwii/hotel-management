package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private int roomId;
    private Integer bookingId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private String username;
    private String userFullName;
    private Integer roomNumber;
    
    public ReviewDTO() {
    }
    
    public ReviewDTO(int id, int userId, int roomId, Integer bookingId, int rating, String comment, LocalDateTime createdAt, String username) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.username = username;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    // Добавленные методы для совместимости с клиентским кодом
    
    public Integer getReviewId() {
        return id;
    }
    
    public void setReviewId(int reviewId) {
        this.id = reviewId;
    }
    
    public LocalDateTime getReviewDate() {
        return createdAt;
    }
    
    public void setReviewDate(LocalDateTime reviewDate) {
        this.createdAt = reviewDate;
    }
    
    public String getUserFullName() {
        return userFullName != null ? userFullName : username;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public Integer getRoomNumber() {
        return roomNumber != null ? roomNumber : roomId;
    }
    
    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }
} 