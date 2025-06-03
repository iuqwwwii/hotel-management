package com.hotel.server.handler;

import com.hotel.common.PartnerDTO;
import com.hotel.common.UpdatePartnerRequest;
import com.hotel.common.UpdatePartnerResponse;
import com.hotel.server.service.PartnerService;

import java.util.Optional;

/**
 * Обработчик запроса на обновление информации о партнере
 */
public class UpdatePartnerHandler {
    private final PartnerService partnerService = new PartnerService();
    
    public UpdatePartnerResponse handle(UpdatePartnerRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId() <= 0) {
            return new UpdatePartnerResponse(false, "Не указан ID партнера");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return new UpdatePartnerResponse(false, "Название партнера не может быть пустым");
        }
        
        if (request.getContactInfo() == null || request.getContactInfo().trim().isEmpty()) {
            return new UpdatePartnerResponse(false, "Контактная информация не может быть пустой");
        }
        
        try {
            // Проверяем существование партнера
            Optional<PartnerDTO> partnerOpt = partnerService.getPartnerById(request.getPartnerId());
            
            if (!partnerOpt.isPresent()) {
                return new UpdatePartnerResponse(false, "Партнер не найден");
            }
            
            // Создаем DTO для обновления
            PartnerDTO partnerDTO = new PartnerDTO(
                request.getPartnerId(),
                request.getName().trim(),
                request.getContactInfo().trim()
            );
            
            boolean updated = partnerService.updatePartner(partnerDTO);
            
            if (updated) {
                // Получаем обновленного партнера
                Optional<PartnerDTO> updatedPartnerOpt = partnerService.getPartnerById(request.getPartnerId());
                
                if (updatedPartnerOpt.isPresent()) {
                    return new UpdatePartnerResponse(true, "Информация о партнере успешно обновлена", 
                                                    updatedPartnerOpt.get());
                } else {
                    return new UpdatePartnerResponse(true, "Информация о партнере успешно обновлена");
                }
            } else {
                return new UpdatePartnerResponse(false, "Не удалось обновить информацию о партнере");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new UpdatePartnerResponse(false, "Ошибка при обновлении информации о партнере: " + e.getMessage());
        }
    }
} 