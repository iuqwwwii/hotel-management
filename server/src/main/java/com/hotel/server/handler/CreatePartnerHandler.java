package com.hotel.server.handler;

import com.hotel.common.CreatePartnerRequest;
import com.hotel.common.CreatePartnerResponse;
import com.hotel.common.PartnerDTO;
import com.hotel.server.service.PartnerService;

/**
 * Обработчик запроса на создание нового партнера
 */
public class CreatePartnerHandler {
    private final PartnerService partnerService = new PartnerService();
    
    public CreatePartnerResponse handle(CreatePartnerRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return new CreatePartnerResponse(false, "Название партнера не может быть пустым");
        }
        
        if (request.getContactInfo() == null || request.getContactInfo().trim().isEmpty()) {
            return new CreatePartnerResponse(false, "Контактная информация не может быть пустой");
        }
        
        try {
            PartnerDTO partner = partnerService.createPartner(
                request.getName().trim(),
                request.getContactInfo().trim()
            );
            
            if (partner != null) {
                return new CreatePartnerResponse(true, "Партнер успешно создан", partner);
            } else {
                return new CreatePartnerResponse(false, "Не удалось создать партнера");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CreatePartnerResponse(false, "Ошибка при создании партнера: " + e.getMessage());
        }
    }
} 