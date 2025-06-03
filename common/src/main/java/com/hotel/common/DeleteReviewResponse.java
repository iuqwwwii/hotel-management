package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос удаления отзыва
 */
public class DeleteReviewResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    
    // Конструкторы
    public DeleteReviewResponse() {
    }
    
    public DeleteReviewResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
} 