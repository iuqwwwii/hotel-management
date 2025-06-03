package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос добавления изображения комнаты
 */
public class AddRoomImageResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private ImageDTO image;
    
    public AddRoomImageResponse() {
    }
    
    public AddRoomImageResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AddRoomImageResponse(boolean success, String message, ImageDTO image) {
        this.success = success;
        this.message = message;
        this.image = image;
    }
    
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
    
    public ImageDTO getImage() {
        return image;
    }
    
    public void setImage(ImageDTO image) {
        this.image = image;
    }
} 