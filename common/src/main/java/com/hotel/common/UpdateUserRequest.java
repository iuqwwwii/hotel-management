package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на обновление данных пользователя
 */
public class UpdateUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String roleName;
    
    // Конструкторы
    public UpdateUserRequest() {
    }
    
    // Геттеры и сеттеры
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
} 