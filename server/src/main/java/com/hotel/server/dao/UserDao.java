// File: server/src/main/java/com/hotel/server/dao/UserDao.java
package com.hotel.server.dao;

import com.hotel.server.model.User;
import com.hotel.server.util.DatabaseUtil;
import com.hotel.common.UserDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.email, u.phone, u.full_name, " +
                "u.card_number, u.card_expiry, u.cvv, r.role_id, r.role_name " +
                "FROM \"user\" u JOIN role r ON u.role_id = r.role_id " +
                "WHERE u.username = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setFullName(rs.getString("full_name"));
                    u.setCardNumber(rs.getString("card_number"));
                    u.setCardExpiry(rs.getString("card_expiry"));
                    u.setCvv(rs.getString("cvv"));
                    u.setRoleId(rs.getInt("role_id"));
                    u.setRoleName(rs.getString("role_name"));
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByUsername", e);
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM \"user\" WHERE username = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in existsByUsername", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM \"user\" WHERE email = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in existsByEmail", e);
        }
    }

    public int findRoleIdByName(String roleName) {
        String sql = "SELECT role_id FROM role WHERE role_name = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("role_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findRoleIdByName", e);
        }
        throw new RuntimeException("Role not found: " + roleName);
    }

    public boolean create(User u) {
        String sql = "INSERT INTO \"user\" (username, password_hash, email, phone, full_name, card_number, card_expiry, cvv, role_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getFullName());
            ps.setString(6, u.getCardNumber());
            ps.setString(7, u.getCardExpiry());
            ps.setString(8, u.getCvv());
            ps.setInt   (9, u.getRoleId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create user", e);
        }
    }
    
    /**
     * Получает список всех пользователей из базы данных
     * @return список пользователей в виде DTO
     */
    public List<UserDTO> getAllUsers() {
        String sql = "SELECT u.user_id, u.username, u.email, u.full_name, r.role_name " +
                "FROM \"user\" u JOIN role r ON u.role_id = r.role_id " +
                "ORDER BY u.user_id";
        
        List<UserDTO> users = new ArrayList<>();
        
        try (Connection c = DatabaseUtil.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                UserDTO user = new UserDTO();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setRoleName(rs.getString("role_name"));
                users.add(user);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getAllUsers", e);
        }
        
        return users;
    }
    
    /**
     * Получает список пользователей с определенной ролью
     * @param roleName название роли для фильтрации
     * @return список пользователей в виде DTO
     */
    public List<UserDTO> getUsersByRole(String roleName) {
        String sql = "SELECT u.user_id, u.username, u.email, u.full_name, r.role_name " +
                "FROM \"user\" u JOIN role r ON u.role_id = r.role_id " +
                "WHERE r.role_name = ? " +
                "ORDER BY u.user_id";
        
        List<UserDTO> users = new ArrayList<>();
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, roleName);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRoleName(rs.getString("role_name"));
                    users.add(user);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getUsersByRole", e);
        }
        
        return users;
    }
}

