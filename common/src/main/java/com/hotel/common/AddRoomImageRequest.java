package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на добавление изображения комнаты
 */
public class AddRoomImageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer roomId;
    private String description;
    private byte[] imageData;
    private String fileName;
    
    public AddRoomImageRequest() {
    }
    
    public AddRoomImageRequest(Integer roomId, String description, byte[] imageData, String fileName) {
        this.roomId = roomId;
        this.description = description;
        this.imageData = imageData;
        this.fileName = fileName;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
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
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
} 