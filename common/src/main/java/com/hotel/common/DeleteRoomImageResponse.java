package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос удаления изображения комнаты
 */
public class DeleteRoomImageResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    
    public DeleteRoomImageResponse() {
    }
    
    public DeleteRoomImageResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
} 