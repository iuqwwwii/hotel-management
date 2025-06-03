package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на обновление информации о партнере
 */
public class UpdatePartnerRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer partnerId;
    private String name;
    private String contactInfo;
    
    public UpdatePartnerRequest() {
    }
    
    public UpdatePartnerRequest(Integer partnerId, String name, String contactInfo) {
        this.partnerId = partnerId;
        this.name = name;
        this.contactInfo = contactInfo;
    }
    
    public Integer getPartnerId() {
        return partnerId;
    }
    
    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
} 