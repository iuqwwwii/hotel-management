package com.hotel.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ответ на запрос получения списка пользователей
 */
public class FetchUsersResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private List<UserDTO> users;
    
    // Конструкторы
    public FetchUsersResponse() {
        this.users = new ArrayList<>();
    }
    
    public FetchUsersResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.users = new ArrayList<>();
    }
    
    public FetchUsersResponse(boolean success, String message, List<UserDTO> users) {
        this.success = success;
        this.message = message;
        this.users = users;
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
    
    public List<UserDTO> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
