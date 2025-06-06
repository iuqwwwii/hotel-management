// LoginRequest.java
package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на авторизацию: логин + пароль.
 * Serializable — чтобы передавать по ObjectStream.
 */
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    public LoginRequest() { }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

