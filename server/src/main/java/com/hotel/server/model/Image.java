package com.hotel.server.model;

/**
 * Модель для изображений комнат
 */
public class Image {
    private Integer imageId;
    private Integer roomId;
    private String filePath;
    private String description;
    
    public Image() {
    }
    
    public Image(Integer imageId, Integer roomId, String filePath, String description) {
        this.imageId = imageId;
        this.roomId = roomId;
        this.filePath = filePath;
        this.description = description;
    }
    
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
} 