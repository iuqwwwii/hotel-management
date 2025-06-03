package com.hotel.client.util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Менеджер уведомлений - реализует паттерн Singleton
 * Предоставляет единую точку доступа для отображения уведомлений в приложении
 */
public class NotificationManager {
    // Экземпляр синглтона
    private static NotificationManager instance;
    
    // Время отображения уведомления по умолчанию (в секундах)
    private static final double DEFAULT_NOTIFICATION_DURATION = 4.0;
    
    // Иконки для разных типов уведомлений в формате SVG
    private static final String SUCCESS_ICON = "M9,20.42L2.79,14.21L5.62,11.38L9,14.77L18.88,4.88L21.71,7.71L9,20.42Z";
    private static final String ERROR_ICON = "M13,13H11V7H13M13,17H11V15H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z";
    private static final String INFO_ICON = "M13,9H11V7H13M13,17H11V11H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z";
    private static final String WARNING_ICON = "M13,13H11V7H13M13,17H11V15H13M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z";
    
    // Приватный конструктор (часть паттерна Singleton)
    private NotificationManager() {
        // Запрещаем создание экземпляра извне
    }
    
    /**
     * Возвращает экземпляр NotificationManager (реализация паттерна Singleton)
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    /**
     * Показывает уведомление об успехе
     * @param title Заголовок уведомления
     * @param message Сообщение уведомления
     */
    public void showSuccess(String title, String message) {
        showNotification(title, message, SUCCESS_ICON, "#388e3c", DEFAULT_NOTIFICATION_DURATION);
    }
    
    /**
     * Показывает уведомление об ошибке
     * @param title Заголовок уведомления
     * @param message Сообщение уведомления
     */
    public void showError(String title, String message) {
        showNotification(title, message, ERROR_ICON, "#d32f2f", DEFAULT_NOTIFICATION_DURATION);
    }
    
    /**
     * Показывает информационное уведомление
     * @param title Заголовок уведомления
     * @param message Сообщение уведомления
     */
    public void showInfo(String title, String message) {
        showNotification(title, message, INFO_ICON, "#1976d2", DEFAULT_NOTIFICATION_DURATION);
    }
    
    /**
     * Показывает предупреждающее уведомление
     * @param title Заголовок уведомления
     * @param message Сообщение уведомления
     */
    public void showWarning(String title, String message) {
        showNotification(title, message, WARNING_ICON, "#ff9800", DEFAULT_NOTIFICATION_DURATION);
    }
    
    /**
     * Показывает уведомление указанного типа
     * @param title Заголовок уведомления
     * @param message Сообщение уведомления
     * @param iconPath SVG-путь для иконки
     * @param color Цвет иконки и полосы
     * @param durationInSeconds Длительность отображения в секундах
     */
    private void showNotification(String title, String message, String iconPath, String color, double durationInSeconds) {
        Platform.runLater(() -> {
            try {
                // Находим активное окно приложения
                Window activeWindow = findActiveWindow();
                
                if (activeWindow == null) {
                    return; // Если активное окно не найдено, ничего не делаем
                }
                
                // Создаем иконку
                SVGPath icon = new SVGPath();
                icon.setContent(iconPath);
                icon.setFill(Color.web(color));
                icon.setScaleX(1.5);
                icon.setScaleY(1.5);
                
                // Создаем заголовок
                Label titleLabel = new Label(title);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                // Создаем сообщение
                Label messageLabel = new Label(message);
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(300);
                
                // Создаем блок текста
                VBox textBlock = new VBox(5, titleLabel, messageLabel);
                
                // Создаем контейнер с иконкой и текстом
                HBox content = new HBox(15, icon, textBlock);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setPadding(new Insets(15));
                
                // Создаем цветную полосу слева
                StackPane colorBar = new StackPane();
                colorBar.setStyle("-fx-background-color: " + color + ";");
                colorBar.setMinWidth(5);
                colorBar.setPrefWidth(5);
                
                // Объединяем полосу и контент
                HBox root = new HBox(colorBar, content);
                root.setStyle(
                    "-fx-background-color: #2a4270; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0.5, 0, 3);"
                );
                
                // Устанавливаем стили текста
                titleLabel.setStyle(titleLabel.getStyle() + "-fx-text-fill: #e3f2fd;");
                messageLabel.setStyle(messageLabel.getStyle() + "-fx-text-fill: #bbdefb;");
                
                // Создаем и настраиваем всплывающее окно
                Popup popup = new Popup();
                popup.getContent().add(root);
                popup.setAutoHide(true);
                
                // Позиционируем в правом верхнем углу с небольшим отступом
                double offsetX = 20;
                double offsetY = 50;
                
                popup.show(activeWindow, 
                        activeWindow.getX() + activeWindow.getWidth() - root.getPrefWidth() - offsetX,
                        activeWindow.getY() + offsetY);
                
                // Применяем анимацию появления
                root.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                
                // Устанавливаем таймер для скрытия уведомления
                new Thread(() -> {
                    try {
                        Thread.sleep((long) (durationInSeconds * 1000));
                        
                        Platform.runLater(() -> {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
                            fadeOut.setFromValue(1);
                            fadeOut.setToValue(0);
                            fadeOut.setOnFinished(e -> popup.hide());
                            fadeOut.play();
                        });
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Ищет активное окно приложения
     */
    private Window findActiveWindow() {
        for (Window window : Stage.getWindows()) {
            if (window instanceof Stage && window.isShowing()) {
                return window;
            }
        }
        return null;
    }
} 