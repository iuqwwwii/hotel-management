package com.hotel.server.handler;

import com.hotel.common.FetchPartnersRequest;
import com.hotel.common.FetchPartnersResponse;
import com.hotel.common.PartnerDTO;
import com.hotel.server.service.PartnerService;

import java.util.List;

/**
 * Обработчик запроса на получение списка партнеров
 */
public class FetchPartnersHandler {
    private final PartnerService partnerService = new PartnerService();
    
    public FetchPartnersResponse handle(FetchPartnersRequest request) {
        try {
            List<PartnerDTO> partners = partnerService.getAllPartners();
            return new FetchPartnersResponse(true, "Партнеры успешно получены", partners);
        } catch (Exception e) {
            e.printStackTrace();
            return new FetchPartnersResponse(false, "Ошибка при получении списка партнеров: " + e.getMessage());
        }
    }
} 