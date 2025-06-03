// common/src/main/java/com/hotel/common/RoomDTO.java
package com.hotel.common;

import java.io.Serializable;

public class RoomDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String imagePath;

    private int roomId;
    private String number;
    private String status;
    private String typeName;
    private double basePrice;
    private String description;

    public RoomDTO() {}

    public RoomDTO(int roomId, String number, String status, String typeName, double basePrice) {
        this.roomId = roomId;
        this.number = number;
        this.status = status;
        this.typeName = typeName;
        this.basePrice = basePrice;
    }
    
    public RoomDTO(int roomId, String number, String status, String typeName, double basePrice, String description) {
        this.roomId = roomId;
        this.number = number;
        this.status = status;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.description = description;
    }

    // геттеры/сеттеры
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

