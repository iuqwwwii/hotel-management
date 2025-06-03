package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на получение изображений комнаты
 */
public class FetchRoomImagesRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer roomId;
    
    public FetchRoomImagesRequest() {
    }
    
    public FetchRoomImagesRequest(Integer roomId) {
        this.roomId = roomId;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
} 