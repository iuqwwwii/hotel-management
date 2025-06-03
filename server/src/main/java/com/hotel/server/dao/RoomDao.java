// server/src/main/java/com/hotel/server/dao/RoomDao.java
package com.hotel.server.dao;

import com.hotel.server.model.Room;
import com.hotel.server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {
    public List<Room> findAll() {
        String sql = "SELECT r.room_id, r.number, r.status, t.type_name, t.base_price, r.description " +
                "FROM room r JOIN room_type t ON r.type_id = t.type_id";
        List<Room> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setNumber(rs.getString("number"));
                room.setStatus(rs.getString("status"));
                room.setTypeName(rs.getString("type_name"));
                room.setBasePrice(rs.getDouble("base_price"));
                room.setDescription(rs.getString("description"));
                list.add(room);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    
    public Room findById(int roomId) {
        String sql = "SELECT r.room_id, r.number, r.status, t.type_name, t.base_price, r.description " +
                "FROM room r JOIN room_type t ON r.type_id = t.type_id " +
                "WHERE r.room_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, roomId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    Room room = new Room();
                    room.setRoomId(rs.getInt("room_id"));
                    room.setNumber(rs.getString("number"));
                    room.setStatus(rs.getString("status"));
                    room.setTypeName(rs.getString("type_name"));
                    room.setBasePrice(rs.getDouble("base_price"));
                    room.setDescription(rs.getString("description"));
                    return room;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    
    /**
     * Проверяет существование комнаты с указанным ID
     * @param roomId ID комнаты
     * @return true если комната существует, иначе false
     */
    public boolean existsById(int roomId) {
        String sql = "SELECT 1 FROM room WHERE room_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, roomId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int roomId) {
        String sql = "DELETE FROM room WHERE room_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, roomId);
            int affectedRows = p.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int create(String number, String typeName, String status, double basePrice, String description) {
        // Сначала найдем id типа номера по имени
        int typeId = findTypeIdByName(typeName);
        if (typeId == -1) {
            return -1; // Тип номера не найден
        }
        
        // Извлекаем номер этажа из номера комнаты (предполагается, что первая цифра - это этаж)
        int floor;
        try {
            // Если номер начинается с цифры, то берем первую цифру как этаж
            floor = Character.getNumericValue(number.charAt(0));
            if (floor < 1) {
                floor = 1; // Дефолтное значение, если не удалось определить
            }
        } catch (Exception e) {
            floor = 1; // Дефолтное значение, если формат номера не стандартный
        }

        String sql = "INSERT INTO room (number, type_id, status, description, floor) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, number);
            p.setInt(2, typeId);
            p.setString(3, status);
            p.setString(4, description);
            p.setInt(5, floor);
            
            int affectedRows = p.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            try (ResultSet generatedKeys = p.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public boolean update(int roomId, String number, String typeName, String status, double basePrice, String description) {
        // Сначала найдем id типа номера по имени
        int typeId = findTypeIdByName(typeName);
        if (typeId == -1) {
            return false; // Тип номера не найден
        }

        // Извлекаем номер этажа из номера комнаты
        int floor;
        try {
            floor = Character.getNumericValue(number.charAt(0));
            if (floor < 1) {
                floor = 1; // Дефолтное значение
            }
        } catch (Exception e) {
            floor = 1; // Дефолтное значение
        }

        String sql = "UPDATE room SET number = ?, type_id = ?, status = ?, description = ?, floor = ? WHERE room_id = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, number);
            p.setInt(2, typeId);
            p.setString(3, status);
            p.setString(4, description);
            p.setInt(5, floor);
            p.setInt(6, roomId);
            
            int affectedRows = p.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private int findTypeIdByName(String typeName) {
        String sql = "SELECT type_id FROM room_type WHERE type_name = ?";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, typeName);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("type_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
