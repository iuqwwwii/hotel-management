package com.hotel.server.model;

/**
 * Модель для партнеров
 */
public class Partner {
    private Integer partnerId;
    private String name;
    private String contactInfo;
    
    public Partner() {
    }
    
    public Partner(Integer partnerId, String name, String contactInfo) {
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