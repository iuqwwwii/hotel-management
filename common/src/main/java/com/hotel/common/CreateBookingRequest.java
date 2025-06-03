// File: common/src/main/java/com/hotel/common/CreateBookingRequest.java
package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDate;

public class CreateBookingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private int roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    private String status;
    private String paymentMethod;
    private String notes;
    
    // Данные для оплаты картой
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardHolder;

    public CreateBookingRequest() {}

    public CreateBookingRequest(int userId, int roomId,
                                LocalDate startDate, LocalDate endDate,
                                double totalCost, String status) {
        this.userId = userId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardExpiry() {
        return cardExpiry;
    }
    
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }
    
    public String getCardCvv() {
        return cardCvv;
    }
    
    public void setCardCvv(String cardCvv) {
        this.cardCvv = cardCvv;
    }
    
    public String getCardHolder() {
        return cardHolder;
    }
    
    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }
}
