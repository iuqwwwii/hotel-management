package com.hotel.common;

import java.io.Serializable;
import java.util.List;

/**
 * Ответ на запрос получения изображений комнаты
 */
public class FetchRoomImagesResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private List<ImageDTO> images;
    
    public FetchRoomImagesResponse() {
    }
    
    public FetchRoomImagesResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public FetchRoomImagesResponse(boolean success, String message, List<ImageDTO> images) {
        this.success = success;
        this.message = message;
        this.images = images;
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
    
    public List<ImageDTO> getImages() {
        return images;
    }
    
    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }
} 