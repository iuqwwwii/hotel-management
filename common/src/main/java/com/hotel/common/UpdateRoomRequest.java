package com.hotel.common;

import java.io.Serializable;

public class UpdateRoomRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int roomId; // 0 для нового номера
    private final String number;
    private final String status;
    private final String typeName;
    private final double basePrice;
    private final String description;
    
    public UpdateRoomRequest(int roomId, String number, String status, String typeName, double basePrice, String description) {
        this.roomId = roomId;
        this.number = number;
        this.status = status;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.description = description;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getNumber() {
        return number;
    }

    public String getStatus() {
        return status;
    }

    public String getTypeName() {
        return typeName;
    }

    public double getBasePrice() {
        return basePrice;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isNewRoom() {
        return roomId == 0;
    }
} 