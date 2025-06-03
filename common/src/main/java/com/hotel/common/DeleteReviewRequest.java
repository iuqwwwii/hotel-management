package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на удаление отзыва
 */
public class DeleteReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer reviewId;
    private Integer userId;
    
    // Конструкторы
    public DeleteReviewRequest() {
    }
    
    public DeleteReviewRequest(Integer reviewId, Integer userId) {
        this.reviewId = reviewId;
        this.userId = userId;
    }
    
    // Геттеры и сеттеры
    public Integer getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
} 