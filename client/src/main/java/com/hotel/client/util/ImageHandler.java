package com.hotel.client.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Утилитарный класс для работы с изображениями
 */
public class ImageHandler {

    /**
     * Загружает изображение из файла
     * @param file Файл изображения
     * @param width Требуемая ширина
     * @param height Требуемая высота
     * @return Изображение или null в случае ошибки
     */
    public static Image loadImage(File file, int width, int height) {
        if (file == null || !file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return new Image(fis, width, height, true, true);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке изображения: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Загружает изображение из ресурсов
     * @param resourcePath Путь к изображению в ресурсах
     * @param width Требуемая ширина
     * @param height Требуемая высота
     * @return Изображение или null в случае ошибки
     */
    public static Image loadImageFromResources(String resourcePath, int width, int height) {
        try {
            return new Image(ImageHandler.class.getResourceAsStream(resourcePath), 
                           width, height, true, true);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке изображения из ресурсов: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Создает изображение с градиентом для номера комнаты
     * @param roomNumber Номер комнаты
     * @param width Ширина изображения
     * @param height Высота изображения
     * @return Созданное изображение
     */
    public static Image createRoomImage(String roomNumber, int width, int height) {
        try {
            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Генерируем случайный цвет для градиента
            Random random = new Random(roomNumber != null ? roomNumber.hashCode() : 0);
            Color color1 = Color.rgb(
                    100 + random.nextInt(100), 
                    100 + random.nextInt(100), 
                    180 + random.nextInt(75));
            Color color2 = color1.darker();
            
            // Создаем градиентный фон
            LinearGradient gradient = new LinearGradient(
                    0, 0, 1, 1, true, 
                    CycleMethod.NO_CYCLE,
                    new Stop(0, color1),
                    new Stop(1, color2));
            
            gc.setFill(gradient);
            gc.fillRect(0, 0, width, height);
            
            // Добавляем текст
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 24));
            
            String text = "Номер " + (roomNumber != null ? roomNumber : "");
            double textWidth = gc.getFont().getSize() * text.length() * 0.6;
            double x = (width - textWidth) / 2;
            
            gc.fillText(text, x, height / 2);
            
            // Создаем изображение из Canvas
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            return canvas.snapshot(params, null);
            
        } catch (Exception e) {
            System.err.println("Ошибка при создании изображения: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Создает изображение для профиля пользователя с инициалами
     * @param userName Имя пользователя
     * @param width Ширина изображения
     * @param height Высота изображения
     * @return Созданное изображение
     */
    public static Image createUserProfileImage(String userName, int width, int height) {
        try {
            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Генерируем цвет на основе имени пользователя
            Random random = new Random(userName != null ? userName.hashCode() : 0);
            Color bgColor = Color.rgb(
                    100 + random.nextInt(100), 
                    100 + random.nextInt(100), 
                    180 + random.nextInt(75));
            
            // Рисуем круг
            gc.setFill(bgColor);
            gc.fillOval(0, 0, width, height);
            
            // Добавляем инициалы
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Bold", width * 0.4));
            
            String initials = "";
            if (userName != null && !userName.isEmpty()) {
                String[] nameParts = userName.split(" ");
                if (nameParts.length > 0 && nameParts[0].length() > 0) {
                    initials += nameParts[0].charAt(0);
                }
                if (nameParts.length > 1 && nameParts[1].length() > 0) {
                    initials += nameParts[1].charAt(0);
                }
            }
            
            if (initials.isEmpty()) {
                initials = "?";
            }
            
            // Центрируем текст
            double textWidth = gc.getFont().getSize() * initials.length() * 0.6;
            double x = (width - textWidth) / 2;
            double y = (height / 2) + (gc.getFont().getSize() / 3);
            
            gc.fillText(initials.toUpperCase(), x, y);
            
            // Создаем изображение
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            return canvas.snapshot(params, null);
            
        } catch (Exception e) {
            System.err.println("Ошибка при создании изображения пользователя: " + e.getMessage());
            return null;
        }
    }
} 