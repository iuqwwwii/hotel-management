package com.hotel.server.dao;

import com.hotel.server.model.Image;
import com.hotel.server.util.DatabaseUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с изображениями комнат
 */
public class ImageDao {
    private static final String IMAGES_DIRECTORY = "server/src/main/resources/uploads/rooms/";
    
    /**
     * Получает все изображения для комнаты
     */
    public List<Image> findByRoomId(int roomId) {
        String sql = "SELECT image_id, room_id, file_path, description FROM image WHERE room_id = ?";
        List<Image> images = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, roomId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Image image = new Image();
                    image.setImageId(rs.getInt("image_id"));
                    image.setRoomId(rs.getInt("room_id"));
                    image.setFilePath(rs.getString("file_path"));
                    image.setDescription(rs.getString("description"));
                    images.add(image);
                }
            }
            
            return images;
        } catch (SQLException e) {
            System.err.println("ImageDao: Ошибка при получении изображений комнаты: " + e.getMessage());
            e.printStackTrace();
            return images;
        }
    }
    
    /**
     * Добавляет новое изображение для комнаты
     */
    public Image create(int roomId, String description, byte[] imageData, String fileName) {
        // Создаем директорию для хранения файлов, если она не существует
        File directory = new File(IMAGES_DIRECTORY + roomId);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Генерируем уникальное имя файла
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String filePath = IMAGES_DIRECTORY + roomId + "/" + uniqueFileName;
        
        // Сохраняем файл на диск
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(imageData);
        } catch (IOException e) {
            System.err.println("ImageDao: Ошибка при сохранении файла: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        // Сохраняем информацию о файле в базу данных
        String sql = "INSERT INTO image (room_id, file_path, description) VALUES (?, ?, ?) RETURNING image_id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, roomId);
            ps.setString(2, filePath);
            ps.setString(3, description);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Image image = new Image();
                    image.setImageId(rs.getInt("image_id"));
                    image.setRoomId(roomId);
                    image.setFilePath(filePath);
                    image.setDescription(description);
                    return image;
                }
            }
        } catch (SQLException e) {
            System.err.println("ImageDao: Ошибка при добавлении изображения в БД: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Удаляет изображение по идентификатору
     */
    public boolean delete(int imageId) {
        // Сначала получаем информацию о файле
        String selectSql = "SELECT file_path FROM image WHERE image_id = ?";
        String deleteSql = "DELETE FROM image WHERE image_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            String filePath = null;
            
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, imageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        filePath = rs.getString("file_path");
                    }
                }
            }
            
            // Удаляем запись из базы данных
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, imageId);
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0 && filePath != null) {
                    // Удаляем файл с диска
                    try {
                        Files.deleteIfExists(Paths.get(filePath));
                        return true;
                    } catch (IOException e) {
                        System.err.println("ImageDao: Ошибка при удалении файла: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("ImageDao: Ошибка при удалении изображения: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Получает изображение по идентификатору
     */
    public Image findById(int imageId) {
        String sql = "SELECT image_id, room_id, file_path, description FROM image WHERE image_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, imageId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Image image = new Image();
                    image.setImageId(rs.getInt("image_id"));
                    image.setRoomId(rs.getInt("room_id"));
                    image.setFilePath(rs.getString("file_path"));
                    image.setDescription(rs.getString("description"));
                    return image;
                }
            }
        } catch (SQLException e) {
            System.err.println("ImageDao: Ошибка при получении изображения: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Получает содержимое изображения в виде массива байт
     */
    public byte[] getImageData(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("ImageDao: Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 