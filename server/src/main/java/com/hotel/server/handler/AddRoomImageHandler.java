package com.hotel.server.handler;

import com.hotel.common.AddRoomImageRequest;
import com.hotel.common.AddRoomImageResponse;
import com.hotel.common.ImageDTO;
import com.hotel.server.service.ImageService;

/**
 * Обработчик запроса на добавление изображения комнаты
 */
public class AddRoomImageHandler {
    private final ImageService imageService = new ImageService();
    
    public AddRoomImageResponse handle(AddRoomImageRequest request) {
        if (request.getRoomId() == null || request.getRoomId() <= 0) {
            return new AddRoomImageResponse(false, "Не указан ID комнаты");
        }
        
        if (request.getImageData() == null || request.getImageData().length == 0) {
            return new AddRoomImageResponse(false, "Изображение не может быть пустым");
        }
        
        if (request.getFileName() == null || request.getFileName().isEmpty()) {
            return new AddRoomImageResponse(false, "Не указано имя файла");
        }
        
        try {
            ImageDTO image = imageService.addRoomImage(
                request.getRoomId(),
                request.getDescription(),
                request.getImageData(),
                request.getFileName()
            );
            
            if (image != null) {
                return new AddRoomImageResponse(true, "Изображение успешно добавлено", image);
            } else {
                return new AddRoomImageResponse(false, "Не удалось добавить изображение");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AddRoomImageResponse(false, "Ошибка при добавлении изображения: " + e.getMessage());
        }
    }
} 