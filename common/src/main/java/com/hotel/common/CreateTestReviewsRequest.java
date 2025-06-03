package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на создание тестовых отзывов
 */
public class CreateTestReviewsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer count;
    
    public CreateTestReviewsRequest() {
        // Пустой конструктор для сериализации
    }
    
    public CreateTestReviewsRequest(Integer count) {
        this.count = count;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
} 