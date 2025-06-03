package com.hotel.server.service;

import com.hotel.common.ImageDTO;
import com.hotel.server.dao.ImageDao;
import com.hotel.server.model.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с изображениями комнат
 */
public class ImageService {
    private final ImageDao imageDao;
    
    public ImageService() {
        this.imageDao = new ImageDao();
    }
    
    /**
     * Получает все изображения для комнаты
     */
    public List<ImageDTO> getRoomImages(int roomId) {
        List<Image> images = imageDao.findByRoomId(roomId);
        List<ImageDTO> imageDTOs = new ArrayList<>();
        
        for (Image image : images) {
            ImageDTO dto = convertToDTO(image);
            
            // Загружаем данные изображения
            byte[] imageData = imageDao.getImageData(image.getFilePath());
            if (imageData != null) {
                dto.setImageData(imageData);
            }
            
            imageDTOs.add(dto);
        }
        
        return imageDTOs;
    }
    
    /**
     * Добавляет новое изображение для комнаты
     */
    public ImageDTO addRoomImage(int roomId, String description, byte[] imageData, String fileName) {
        Image image = imageDao.create(roomId, description, imageData, fileName);
        
        if (image != null) {
            ImageDTO dto = convertToDTO(image);
            dto.setImageData(imageData);
            return dto;
        }
        
        return null;
    }
    
    /**
     * Удаляет изображение по идентификатору
     */
    public boolean deleteImage(int imageId) {
        return imageDao.delete(imageId);
    }
    
    /**
     * Конвертирует модель в DTO
     */
    private ImageDTO convertToDTO(Image image) {
        ImageDTO dto = new ImageDTO();
        dto.setImageId(image.getImageId());
        dto.setRoomId(image.getRoomId());
        dto.setFilePath(image.getFilePath());
        dto.setDescription(image.getDescription());
        return dto;
    }
} 