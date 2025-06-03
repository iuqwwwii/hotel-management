package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на удаление пользователя
 */
public class DeleteUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;
    
    // Конструкторы
    public DeleteUserRequest() {
    }
    
    public DeleteUserRequest(Integer userId) {
        this.userId = userId;
    }
    
    // Геттеры и сеттеры
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
} 