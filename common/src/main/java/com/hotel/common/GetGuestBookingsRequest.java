package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDate;

public class GetGuestBookingsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private String statusFilter;
    private LocalDate startDateFilter;
    
    public GetGuestBookingsRequest(int userId) {
        this.userId = userId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getStatusFilter() {
        return statusFilter;
    }
    
    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }
    
    public LocalDate getStartDateFilter() {
        return startDateFilter;
    }
    
    public void setStartDateFilter(LocalDate startDateFilter) {
        this.startDateFilter = startDateFilter;
    }
} 