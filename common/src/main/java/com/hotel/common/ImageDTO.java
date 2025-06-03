package com.hotel.common;

import java.io.Serializable;

/**
 * DTO для передачи данных об изображениях между клиентом и сервером
 */
public class ImageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer imageId;
    private Integer roomId;
    private String filePath;
    private String description;
    private byte[] imageData; // Для передачи бинарных данных изображения
    
    // Конструкторы
    public ImageDTO() {
    }
    
    public ImageDTO(Integer imageId, Integer roomId, String filePath, String description) {
        this.imageId = imageId;
        this.roomId = roomId;
        this.filePath = filePath;
        this.description = description;
    }
    
    // Геттеры и сеттеры
    public Integer getImageId() {
        return imageId;
    }
    
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public byte[] getImageData() {
        return imageData;
    }
    
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
} 