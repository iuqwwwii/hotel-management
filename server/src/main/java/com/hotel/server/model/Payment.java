package com.hotel.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    int id;
    BigDecimal amount;
    Long bookingId;
    Long customerId;
    LocalDateTime paymentDate;
    String paymentType;
    String paymentStatus;
    String paymentReference;
    String paymentDescription;

    public Payment() {
    }

    public void setId(int id) {this.id=id;}
    public void setAmount(BigDecimal amount) {this.amount=amount;}
    public void setBookingId(Long bookingId) {this.bookingId=bookingId;}
    public void setCustomerId(Long customerId) {this.customerId=customerId;}
    public void setPaymentDate(LocalDateTime paymentDate) {this.paymentDate=paymentDate;}
    public void setPaymentType(String paymentType) {this.paymentType=paymentType;}
    public void setPaymentStatus(String paymentStatus) {this.paymentStatus=paymentStatus;}
    public void setPaymentReference(String paymentReference) {this.paymentReference=paymentReference;}
    public void setPaymentDescription(String paymentDescription) {this.paymentDescription=paymentDescription;}
    public int getId() {
        return id;
    }
    public BigDecimal getAmount() {return amount;}
    public Long getBookingId() {return bookingId;}
    public Long getCustomerId() {return customerId;}
    public LocalDateTime getPaymentDate() {return paymentDate;}
    public String getPaymentType() {return paymentType;}
    public String getPaymentStatus() {return paymentStatus;}
    public String getPaymentReference() {return paymentReference;}
    public String getPaymentDescription() {return paymentDescription;}
}
