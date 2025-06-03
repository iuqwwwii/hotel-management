package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос обновления данных пользователя
 */
public class UpdateUserResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private UserDTO user;
    
    // Конструкторы
    public UpdateUserResponse() {
    }
    
    public UpdateUserResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public UpdateUserResponse(boolean success, String message, UserDTO user) {
        this.success = success;
        this.message = message;
        this.user = user;
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
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
} 