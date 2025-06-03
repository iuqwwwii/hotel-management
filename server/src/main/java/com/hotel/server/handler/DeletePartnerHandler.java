package com.hotel.server.handler;

import com.hotel.common.DeletePartnerRequest;
import com.hotel.common.DeletePartnerResponse;
import com.hotel.server.service.PartnerService;

/**
 * Обработчик запроса на удаление партнера
 */
public class DeletePartnerHandler {
    private final PartnerService partnerService = new PartnerService();
    
    public DeletePartnerResponse handle(DeletePartnerRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId() <= 0) {
            return new DeletePartnerResponse(false, "Не указан ID партнера");
        }
        
        try {
            boolean deleted = partnerService.deletePartner(request.getPartnerId());
            
            if (deleted) {
                return new DeletePartnerResponse(true, "Партнер успешно удален");
            } else {
                return new DeletePartnerResponse(false, 
                        "Не удалось удалить партнера. Возможно, он связан с бронированиями.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DeletePartnerResponse(false, "Ошибка при удалении партнера: " + e.getMessage());
        }
    }
} 