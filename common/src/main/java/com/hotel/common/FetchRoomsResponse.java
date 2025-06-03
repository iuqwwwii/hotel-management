// common/src/main/java/com/hotel/common/FetchRoomsResponse.java
package com.hotel.common;

import java.io.Serializable;
import java.util.List;

public class FetchRoomsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private List<RoomDTO> rooms;
    private String message;

    public FetchRoomsResponse() {}

    public FetchRoomsResponse(List<RoomDTO> rooms, String message, boolean success) {
        this.rooms = rooms;
        this.message = message;
        this.success=success;
    }

    public List<RoomDTO> getRooms() { return rooms; }
    public void setRooms(List<RoomDTO> rooms) { this.rooms = rooms; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean isSuccess() {
        return success;
    }
}
