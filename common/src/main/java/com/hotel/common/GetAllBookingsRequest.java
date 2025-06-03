package com.hotel.common;

import java.io.Serializable;

/**
 * Запрос на получение всех бронирований
 */
public class GetAllBookingsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Можно добавить параметры фильтрации в будущем
    
    public GetAllBookingsRequest() {
        // Пустой конструктор
    }
} 