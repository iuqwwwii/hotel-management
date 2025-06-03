package com.hotel.client.strategy;

import javafx.application.Platform;

import java.io.File;

/**
 * Абстрактный базовый класс для всех стратегий экспорта
 * Предоставляет общую функциональность для конкретных стратегий
 */
public abstract class AbstractExportStrategy implements ExportStrategy {
    
    protected final String format;
    protected final boolean includeHeaders;
    
    /**
     * Конструктор базового класса
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public AbstractExportStrategy(String format, boolean includeHeaders) {
        this.format = format.toLowerCase();
        this.includeHeaders = includeHeaders;
        
        // Проверка формата
        if (!this.format.equals("csv") && !this.format.equals("xlsx")) {
            throw new IllegalArgumentException("Неподдерживаемый формат экспорта: " + format);
        }
    }
    
    @Override
    public String getFormat() {
        return format;
    }
    
    /**
     * Обновляет статус экспорта в UI-потоке
     * 
     * @param message Сообщение статуса
     */
    protected void updateStatus(String message) {
        Platform.runLater(() -> {
            System.out.println("Экспорт: " + message);
            // Здесь можно добавить обновление статуса в UI, если необходимо
        });
    }
    
    /**
     * Обрабатывает ошибку экспорта
     * 
     * @param message Сообщение об ошибке
     * @param e Исключение
     */
    protected void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Проверяет, должны ли данные быть экспортированы в формате CSV
     * 
     * @return true если формат CSV, false в противном случае
     */
    protected boolean isCsvFormat() {
        return "csv".equals(format);
    }
    
    /**
     * Проверяет, должны ли данные быть экспортированы в формате Excel
     * 
     * @return true если формат Excel, false в противном случае
     */
    protected boolean isExcelFormat() {
        return "xlsx".equals(format);
    }
} 