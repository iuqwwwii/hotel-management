package com.hotel.client.strategy;

import java.time.LocalDate;

/**
 * Фабрика для создания стратегий экспорта данных
 * Реализует паттерн "Фабричный метод" (Factory Method)
 */
public class ExportStrategyFactory {
    
    /**
     * Создает и возвращает подходящую стратегию экспорта данных
     * 
     * @param dataType Тип данных для экспорта (partners, bookings, rooms, reviews, statistics)
     * @param format Формат экспорта (csv, xlsx)
     * @param startDate Начальная дата для фильтрации (может быть null)
     * @param endDate Конечная дата для фильтрации (может быть null)
     * @param filter Строка фильтрации (может быть null)
     * @param includeHeaders Включать ли заголовки в экспорт
     * @return Подходящую стратегию экспорта данных
     * @throws IllegalArgumentException если указан неизвестный тип данных или формат
     */
    public static ExportStrategy createStrategy(
            String dataType,
            String format,
            LocalDate startDate,
            LocalDate endDate,
            String filter,
            boolean includeHeaders) {
        
        // Проверяем тип данных
        switch (dataType.toLowerCase()) {
            case "partners":
                return new PartnersExportStrategy(format, filter, includeHeaders);
                
            case "bookings":
                return new BookingsExportStrategy(format, startDate, endDate, filter, includeHeaders);
                
            case "rooms":
                return new RoomsExportStrategy(format, filter, includeHeaders);
                
            case "reviews":
                return new ReviewsExportStrategy(format, startDate, endDate, filter, includeHeaders);
                
            case "statistics":
                return new StatisticsExportStrategy(format, includeHeaders);
                
            default:
                throw new IllegalArgumentException("Неизвестный тип данных: " + dataType);
        }
    }
} 