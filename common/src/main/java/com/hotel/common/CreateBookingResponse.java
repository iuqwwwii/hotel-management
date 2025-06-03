// File: common/src/main/java/com/hotel/common/CreateBookingResponse.java
package com.hotel.common;

import java.io.Serializable;

public class CreateBookingResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;

    public CreateBookingResponse() {}

    public CreateBookingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
