package com.hotel.common;

import java.io.Serializable;

/**
 * Ответ на запрос обновления информации о партнере
 */
public class UpdatePartnerResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private PartnerDTO partner;
    
    public UpdatePartnerResponse() {
    }
    
    public UpdatePartnerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public UpdatePartnerResponse(boolean success, String message, PartnerDTO partner) {
        this.success = success;
        this.message = message;
        this.partner = partner;
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
    
    public PartnerDTO getPartner() {
        return partner;
    }
    
    public void setPartner(PartnerDTO partner) {
        this.partner = partner;
    }
} 