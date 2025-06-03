package com.hotel.server.handler;

import com.hotel.common.DeleteRoomImageRequest;
import com.hotel.common.DeleteRoomImageResponse;
import com.hotel.server.service.ImageService;

/**
 * Обработчик запроса на удаление изображения комнаты
 */
public class DeleteRoomImageHandler {
    private final ImageService imageService = new ImageService();
    
    public DeleteRoomImageResponse handle(DeleteRoomImageRequest request) {
        if (request.getImageId() == null || request.getImageId() <= 0) {
            return new DeleteRoomImageResponse(false, "Не указан ID изображения");
        }
        
        try {
            boolean deleted = imageService.deleteImage(request.getImageId());
            
            if (deleted) {
                return new DeleteRoomImageResponse(true, "Изображение успешно удалено");
            } else {
                return new DeleteRoomImageResponse(false, "Не удалось удалить изображение");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DeleteRoomImageResponse(false, "Ошибка при удалении изображения: " + e.getMessage());
        }
    }
} 