package com.hotel.common;

import java.io.Serializable;

public class UpdateRoomResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final boolean success;
    private final String errorMessage;
    private final RoomDTO updatedRoom;
    
    public UpdateRoomResponse(boolean success, RoomDTO updatedRoom) {
        this.success = success;
        this.errorMessage = null;
        this.updatedRoom = updatedRoom;
    }
    
    public UpdateRoomResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.updatedRoom = null;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public RoomDTO getUpdatedRoom() {
        return updatedRoom;
    }
} 