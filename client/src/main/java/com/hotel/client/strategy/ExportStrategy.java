package com.hotel.client.strategy;

import java.io.File;

/**
 * Интерфейс стратегии экспорта данных (паттерн Стратегия)
 * Определяет контракт для всех конкретных стратегий экспорта
 */
public interface ExportStrategy {
    
    /**
     * Выполняет экспорт данных в указанный файл
     * 
     * @param file Файл, в который будут экспортированы данные
     * @return true если экспорт выполнен успешно, false в противном случае
     * @throws Exception при ошибке экспорта
     */
    boolean export(File file) throws Exception;
    
    /**
     * Возвращает тип данных, экспортируемых этой стратегией
     * 
     * @return Строковое представление типа данных
     */
    String getDataType();
    
    /**
     * Возвращает формат экспорта (csv, xlsx и т.д.)
     * 
     * @return Строковое представление формата экспорта
     */
    String getFormat();
} 