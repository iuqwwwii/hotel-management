package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на удаление партнера
 */
public class DeletePartnerRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer partnerId;
    
    public DeletePartnerRequest() {
    }
    
    public DeletePartnerRequest(Integer partnerId) {
        this.partnerId = partnerId;
    }
    
    public Integer getPartnerId() {
        return partnerId;
    }
    
    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }
} 