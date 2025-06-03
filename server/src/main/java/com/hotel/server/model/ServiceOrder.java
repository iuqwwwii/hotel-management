package com.hotel.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceOrder {
    LocalDateTime executionDate;
    BigDecimal price;
    LocalDateTime deliveryDate;
    User user;
    String orderId;
    String orderStatus;
    int quantity;
    String notes;
    public LocalDateTime getExecutionDate() {return executionDate;}
    public void setExecutionDate(LocalDateTime executionDate) {this.executionDate = executionDate;}
    public BigDecimal getPrice() {return price;}
    public void setPrice(BigDecimal price) {this.price = price;}
    public LocalDateTime getDeliveryDate() {return deliveryDate;}
    public void setDeliveryDate(LocalDateTime deliveryDate) {this.deliveryDate = deliveryDate;}
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    public String getOrderId() {return orderId;}
    public void setOrderId(String orderId) {this.orderId = orderId;}
    public String getOrderStatus() {return orderStatus;}
    public void setOrderStatus(String orderStatus) {this.orderStatus = orderStatus;}
    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}
}
