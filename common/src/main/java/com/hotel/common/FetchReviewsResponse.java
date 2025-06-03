package com.hotel.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ответ на запрос получения отзывов
 */
public class FetchReviewsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private List<ReviewDTO> reviews;
    
    // Конструкторы
    public FetchReviewsResponse() {
        this.reviews = new ArrayList<>();
    }
    
    public FetchReviewsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.reviews = new ArrayList<>();
    }
    
    public FetchReviewsResponse(boolean success, String message, List<ReviewDTO> reviews) {
        this.success = success;
        this.message = message;
        this.reviews = reviews;
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
    
    public List<ReviewDTO> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }
} 