package com.hotel.common;

import java.io.Serializable;

public class DeleteRoomResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final boolean success;
    private final String errorMessage;
    
    public DeleteRoomResponse(boolean success) {
        this.success = success;
        this.errorMessage = null;
    }
    
    public DeleteRoomResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
} 