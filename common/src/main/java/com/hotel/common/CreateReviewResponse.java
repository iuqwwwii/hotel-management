package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос создания отзыва
 */
public class CreateReviewResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private ReviewDTO review;
    
    // Конструкторы
    public CreateReviewResponse() {
    }
    
    public CreateReviewResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public CreateReviewResponse(boolean success, String message, ReviewDTO review) {
        this.success = success;
        this.message = message;
        this.review = review;
    }
    
    // Геттеры и сеттеры
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
    
    public ReviewDTO getReview() {
        return review;
    }
    
    public void setReview(ReviewDTO review) {
        this.review = review;
    }
} 