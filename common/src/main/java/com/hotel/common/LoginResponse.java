package com.hotel.common;

import java.io.Serializable;

/**

 Ответ сервера на авторизацию.

 Включает userId, роль и сообщение.
 */
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private int userId;
    private String roleName;
    private String message;

    public LoginResponse() {}

    public LoginResponse(boolean success, int userId, String roleName, String message) {
        this.success = success;
        this.userId = userId;
        this.roleName = roleName;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
