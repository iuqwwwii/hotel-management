package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDate;

public class BookingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int bookingId;
    private int userId;
    private int roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    private String status;

    public BookingDTO() {}

    public BookingDTO(int bookingId, int userId, int roomId,
                      LocalDate startDate, LocalDate endDate,
                      double totalCost, String status) {
        this.bookingId = bookingId;
        this.userId    = userId;
        this.roomId    = roomId;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.totalCost = totalCost;
        this.status    = status;
    }

    public BookingDTO(int bookingId, int userId, int roomId, String roomNumber, String roomType,
                      LocalDate startDate, LocalDate endDate,
                      double totalCost, String status) {
        this.bookingId = bookingId;
        this.userId    = userId;
        this.roomId    = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.totalCost = totalCost;
        this.status    = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
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
    
    public String getCheckInDate() {
        return startDate != null ? startDate.toString() : "";
    }
    
    public String getCheckOutDate() {
        return endDate != null ? endDate.toString() : "";
    }
}

