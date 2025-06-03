package com.hotel.server.handler;

import com.hotel.common.FetchRoomImagesRequest;
import com.hotel.common.FetchRoomImagesResponse;
import com.hotel.common.ImageDTO;
import com.hotel.server.service.ImageService;

import java.util.List;

/**
 * Обработчик запроса на получение изображений комнаты
 */
public class FetchRoomImagesHandler {
    private final ImageService imageService = new ImageService();
    
    public FetchRoomImagesResponse handle(FetchRoomImagesRequest request) {
        if (request.getRoomId() == null || request.getRoomId() <= 0) {
            return new FetchRoomImagesResponse(false, "Не указан ID комнаты");
        }
        
        try {
            List<ImageDTO> images = imageService.getRoomImages(request.getRoomId());
            return new FetchRoomImagesResponse(true, "Изображения успешно получены", images);
        } catch (Exception e) {
            e.printStackTrace();
            return new FetchRoomImagesResponse(false, "Ошибка при получении изображений: " + e.getMessage());
        }
    }
} 