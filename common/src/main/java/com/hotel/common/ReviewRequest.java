package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на создание отзыва о проживании
 */
public class ReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private int bookingId;
    private int rating;
    private String title;
    private String reviewText;
    
    public ReviewRequest() {
    }
    
    public ReviewRequest(int userId, int bookingId, int rating, String title, String reviewText) {
        this.userId = userId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.title = title;
        this.reviewText = reviewText;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
} 