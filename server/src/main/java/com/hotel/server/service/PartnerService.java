package com.hotel.server.service;

import com.hotel.common.PartnerDTO;
import com.hotel.server.dao.PartnerDao;
import com.hotel.server.model.Partner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с партнерами
 */
public class PartnerService {
    private final PartnerDao partnerDao;
    
    public PartnerService() {
        this.partnerDao = new PartnerDao();
    }
    
    /**
     * Получает список всех партнеров с их статистикой
     */
    public List<PartnerDTO> getAllPartners() {
        List<Partner> partners = partnerDao.findAll();
        List<PartnerDTO> partnerDTOs = new ArrayList<>();
        
        for (Partner partner : partners) {
            PartnerDTO dto = convertToDTO(partner);
            
            // Получаем статистику по партнеру
            Object[] statistics = partnerDao.getPartnerStatistics(partner.getPartnerId());
            dto.setBookingsCount((int) statistics[0]);
            dto.setTotalRevenue((double) statistics[1]);
            
            partnerDTOs.add(dto);
        }
        
        return partnerDTOs;
    }
    
    /**
     * Получает партнера по ID
     */
    public Optional<PartnerDTO> getPartnerById(int partnerId) {
        Optional<Partner> partnerOpt = partnerDao.findById(partnerId);
        
        if (partnerOpt.isPresent()) {
            Partner partner = partnerOpt.get();
            PartnerDTO dto = convertToDTO(partner);
            
            // Получаем статистику по партнеру
            Object[] statistics = partnerDao.getPartnerStatistics(partner.getPartnerId());
            dto.setBookingsCount((int) statistics[0]);
            dto.setTotalRevenue((double) statistics[1]);
            
            return Optional.of(dto);
        }
        
        return Optional.empty();
    }
    
    /**
     * Создает нового партнера
     */
    public PartnerDTO createPartner(String name, String contactInfo) {
        Partner partner = new Partner();
        partner.setName(name);
        partner.setContactInfo(contactInfo);
        
        Partner createdPartner = partnerDao.create(partner);
        if (createdPartner != null) {
            return convertToDTO(createdPartner);
        }
        
        return null;
    }
    
    /**
     * Обновляет данные партнера
     */
    public boolean updatePartner(PartnerDTO partnerDTO) {
        Partner partner = new Partner();
        partner.setPartnerId(partnerDTO.getPartnerId());
        partner.setName(partnerDTO.getName());
        partner.setContactInfo(partnerDTO.getContactInfo());
        
        return partnerDao.update(partner);
    }
    
    /**
     * Удаляет партнера
     */
    public boolean deletePartner(int partnerId) {
        return partnerDao.delete(partnerId);
    }
    
    /**
     * Конвертирует модель в DTO
     */
    private PartnerDTO convertToDTO(Partner partner) {
        return new PartnerDTO(
            partner.getPartnerId(),
            partner.getName(),
            partner.getContactInfo()
        );
    }
} 