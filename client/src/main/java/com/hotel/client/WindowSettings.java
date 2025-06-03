package com.hotel.client;

import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.prefs.Preferences;

/**
 * Класс для сохранения и восстановления настроек окна (размер, позиция, состояние развернутости)
 */
public class WindowSettings {
    private static final String KEY_X = "window_x";
    private static final String KEY_Y = "window_y";
    private static final String KEY_WIDTH = "window_width";
    private static final String KEY_HEIGHT = "window_height";
    private static final String KEY_MAXIMIZED = "window_maximized";
    
    private static final Preferences prefs = Preferences.userNodeForPackage(WindowSettings.class);
    
    /**
     * Сохраняет текущие настройки окна
     * @param stage Окно, настройки которого нужно сохранить
     */
    public static void saveSettings(Stage stage) {
        if (stage.isMaximized()) {
            prefs.putBoolean(KEY_MAXIMIZED, true);
        } else {
            prefs.putBoolean(KEY_MAXIMIZED, false);
            prefs.putDouble(KEY_X, stage.getX());
            prefs.putDouble(KEY_Y, stage.getY());
            prefs.putDouble(KEY_WIDTH, stage.getWidth());
            prefs.putDouble(KEY_HEIGHT, stage.getHeight());
        }
    }
    
    /**
     * Применяет сохраненные настройки к окну
     * @param stage Окно, к которому нужно применить настройки
     */
    public static void applySettings(Stage stage) {
        boolean maximized = prefs.getBoolean(KEY_MAXIMIZED, false);
        
        if (maximized) {
            stage.setMaximized(true);
        } else {
            // Получаем сохраненные размеры и позицию
            double x = prefs.getDouble(KEY_X, -1);
            double y = prefs.getDouble(KEY_Y, -1);
            double width = prefs.getDouble(KEY_WIDTH, 800);
            double height = prefs.getDouble(KEY_HEIGHT, 600);
            
            // Проверяем, находится ли окно в пределах экрана
            boolean isValidPosition = isPositionValid(x, y, width, height);
            
            if (isValidPosition) {
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(width);
                stage.setHeight(height);
            } else {
                // Если позиция некорректная, центрируем окно
                centerStage(stage, width, height);
            }
        }
    }
    
    /**
     * Проверяет, находится ли окно в пределах экрана
     */
    private static boolean isPositionValid(double x, double y, double width, double height) {
        if (x < 0 || y < 0) {
            return false;
        }
        
        // Получаем размеры основного экрана
        Screen primaryScreen = Screen.getPrimary();
        double screenWidth = primaryScreen.getBounds().getWidth();
        double screenHeight = primaryScreen.getBounds().getHeight();
        
        // Проверяем, что окно хотя бы частично видимо на экране
        return x < screenWidth && y < screenHeight && 
               x + width / 3 < screenWidth && y + height / 3 < screenHeight;
    }
    
    /**
     * Центрирует окно на экране
     */
    private static void centerStage(Stage stage, double width, double height) {
        Screen primaryScreen = Screen.getPrimary();
        double screenWidth = primaryScreen.getBounds().getWidth();
        double screenHeight = primaryScreen.getBounds().getHeight();
        
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX((screenWidth - width) / 2);
        stage.setY((screenHeight - height) / 2);
    }
} 