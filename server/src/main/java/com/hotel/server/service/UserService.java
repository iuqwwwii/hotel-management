package com.hotel.server.service;

import com.hotel.common.UserDTO;
import com.hotel.server.dao.UserDao;
import com.hotel.server.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями
 */
public class UserService {
    private final UserDao userDao;
    
    public UserService() {
        this.userDao = new UserDao();
    }
    
    /**
     * Получает список всех пользователей
     */
    public List<UserDTO> getAllUsers() {
        return userDao.getAllUsers();
    }
    
    /**
     * Получает список пользователей с фильтрацией по роли
     */
    public List<UserDTO> getUsersByRole(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return getAllUsers();
        }
        return userDao.getUsersByRole(roleName);
    }
    
    /**
     * Находит пользователя по имени пользователя
     */
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }
    
    /**
     * Проверяет, существует ли пользователь с указанным именем
     */
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }
    
    /**
     * Проверяет, существует ли пользователь с указанным email
     */
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }
} 