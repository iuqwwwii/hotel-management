package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на удаление изображения комнаты
 */
public class DeleteRoomImageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer imageId;
    
    public DeleteRoomImageRequest() {
    }
    
    public DeleteRoomImageRequest(Integer imageId) {
        this.imageId = imageId;
    }
    
    public Integer getImageId() {
        return imageId;
    }
    
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
} 