package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на получение отзывов
 */
public class FetchReviewsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer userId;
    private Integer roomId;
    private Boolean fetchAll;
    private Integer minRating;
    private Integer maxRating;
    
    public FetchReviewsRequest() {
    }

    /**
     * Конструктор с параметром fetchAll
     * @param fetchAll флаг для получения всех отзывов
     */
    public FetchReviewsRequest(boolean fetchAll) {
        this.fetchAll = fetchAll;
    }

    public static FetchReviewsRequest fetchAllReviews() {
        FetchReviewsRequest request = new FetchReviewsRequest();
        request.setFetchAll(true);
        return request;
    }
    // Геттеры и сеттеры
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
    
    public Boolean getFetchAll() {
        return fetchAll;
    }
    
    public void setFetchAll(Boolean fetchAll) {
        this.fetchAll = fetchAll;
    }
    
    public Integer getMinRating() {
        return minRating;
    }
    
    public void setMinRating(Integer minRating) {
        this.minRating = minRating;
    }
    
    public Integer getMaxRating() {
        return maxRating;
    }
    
    public void setMaxRating(Integer maxRating) {
        this.maxRating = maxRating;
    }
} 