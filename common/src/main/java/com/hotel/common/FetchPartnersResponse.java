package com.hotel.common;

import java.io.Serializable;
import java.util.List;

/**
 * Ответ на запрос получения списка партнеров
 */
public class FetchPartnersResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private List<PartnerDTO> partners;
    
    public FetchPartnersResponse() {
    }
    
    public FetchPartnersResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public FetchPartnersResponse(boolean success, String message, List<PartnerDTO> partners) {
        this.success = success;
        this.message = message;
        this.partners = partners;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<PartnerDTO> getPartners() {
        return partners;
    }
    
    public void setPartners(List<PartnerDTO> partners) {
        this.partners = partners;
    }
} 