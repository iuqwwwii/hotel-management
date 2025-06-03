package com.hotel.client.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;

/**
 * Класс для хранения и восстановления состояния окна
 */
public class WindowState {
    private static double width;
    private static double height;
    private static double x;
    private static double y;
    private static boolean maximized;
    
    private static final String CONFIG_FILE = "window_state.properties";

    /**
     * Сохраняет текущее состояние окна
     * @param stage Окно, состояние которого нужно сохранить
     */
    public static void saveState(Stage stage) {
        // Сохраняем состояние максимизации
        maximized = stage.isMaximized();
        
        // Если окно максимизировано, сначала восстанавливаем его до нормального размера
        // чтобы получить корректные значения размеров и позиции
        if (maximized) {
            stage.setMaximized(false);
        }
        
        // Сохраняем размеры и позицию
        width = stage.getWidth();
        height = stage.getHeight();
        x = stage.getX();
        y = stage.getY();
        
        // Возвращаем максимизацию, если она была
        if (maximized) {
            stage.setMaximized(true);
        }
        
        // Сохраняем в файл
        saveToFile();
    }

    /**
     * Восстанавливает сохраненное состояние окна
     * @param stage Окно, для которого нужно восстановить состояние
     */
    public static void restoreState(Stage stage) {
        // Загружаем из файла
        loadFromFile();
        
        // Устанавливаем размеры и позицию
        if (width > 0 && height > 0) {
            stage.setWidth(width);
            stage.setHeight(height);
        }
        
        // Проверяем, находится ли окно в пределах видимых экранов
        if (x >= 0 && y >= 0) {
            // Получаем список всех экранов
            boolean isVisible = false;
            for (Screen screen : Screen.getScreens()) {
                Rectangle2D bounds = screen.getVisualBounds();
                // Проверяем, что хотя бы часть окна видна на экране
                if (x < bounds.getMaxX() && x + width > bounds.getMinX() &&
                    y < bounds.getMaxY() && y + height > bounds.getMinY()) {
                    isVisible = true;
                    break;
                }
            }
            
            // Если окно не видно ни на одном экране, центрируем его
            if (isVisible) {
                stage.setX(x);
                stage.setY(y);
            } else {
                stage.centerOnScreen();
            }
        } else {
            // Если координаты не были сохранены, центрируем окно
            stage.centerOnScreen();
        }
        
        // Восстанавливаем максимизацию
        stage.setMaximized(maximized);
    }
    
    /**
     * Сохраняет состояние окна в файл настроек
     */
    private static void saveToFile() {
        Properties props = new Properties();
        props.setProperty("width", String.valueOf(width));
        props.setProperty("height", String.valueOf(height));
        props.setProperty("x", String.valueOf(x));
        props.setProperty("y", String.valueOf(y));
        props.setProperty("maximized", String.valueOf(maximized));
        
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Window State Configuration");
        } catch (IOException e) {
            System.err.println("Не удалось сохранить состояние окна: " + e.getMessage());
        }
    }
    
    /**
     * Загружает состояние окна из файла настроек
     */
    private static void loadFromFile() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            // Если файл не существует, используем значения по умолчанию
            width = 800;
            height = 600;
            x = -1;
            y = -1;
            maximized = false;
            return;
        }
        
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);
            
            width = Double.parseDouble(props.getProperty("width", "800"));
            height = Double.parseDouble(props.getProperty("height", "600"));
            x = Double.parseDouble(props.getProperty("x", "-1"));
            y = Double.parseDouble(props.getProperty("y", "-1"));
            maximized = Boolean.parseBoolean(props.getProperty("maximized", "false"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Не удалось загрузить состояние окна: " + e.getMessage());
            // При ошибке используем значения по умолчанию
            width = 800;
            height = 600;
            x = -1;
            y = -1;
            maximized = false;
        }
    }
} 