package com.hotel.common;

import java.io.Serializable;

/**
 * DTO для передачи данных о партнерах между клиентом и сервером
 */
public class PartnerDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer partnerId;
    private String name;
    private String contactInfo;
    private int bookingsCount; // Количество бронирований через этого партнера
    private double totalRevenue; // Общая сумма дохода от этого партнера
    
    // Конструкторы
    public PartnerDTO() {
    }
    
    public PartnerDTO(Integer partnerId, String name, String contactInfo) {
        this.partnerId = partnerId;
        this.name = name;
        this.contactInfo = contactInfo;
    }
    
    public PartnerDTO(Integer partnerId, String name, String contactInfo, int bookingsCount, double totalRevenue) {
        this.partnerId = partnerId;
        this.name = name;
        this.contactInfo = contactInfo;
        this.bookingsCount = bookingsCount;
        this.totalRevenue = totalRevenue;
    }
    
    // Геттеры и сеттеры
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
    
    public int getBookingsCount() {
        return bookingsCount;
    }
    
    public void setBookingsCount(int bookingsCount) {
        this.bookingsCount = bookingsCount;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
} 