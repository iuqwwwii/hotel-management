package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на создание нового партнера
 */
public class CreatePartnerRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String contactInfo;
    
    public CreatePartnerRequest() {
    }
    
    public CreatePartnerRequest(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
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