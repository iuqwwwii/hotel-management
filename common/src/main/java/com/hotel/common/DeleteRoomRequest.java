package com.hotel.common;

import java.io.Serializable;

public class DeleteRoomRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int roomId;
    
    public DeleteRoomRequest(int roomId) {
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
} 