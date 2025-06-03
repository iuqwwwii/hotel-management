package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на получение списка пользователей
 */
public class FetchUsersRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String roleFilter;
    
    public FetchUsersRequest() {
    }
    
    public FetchUsersRequest(String roleFilter) {
        this.roleFilter = roleFilter;
    }
    
    public String getRoleFilter() {
        return roleFilter;
    }
    
    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }
}
