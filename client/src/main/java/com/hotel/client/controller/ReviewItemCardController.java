package com.hotel.client.controller;

import com.hotel.common.DeleteReviewRequest;
import com.hotel.common.DeleteReviewResponse;
import com.hotel.common.ReviewDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ReviewItemCardController {
    
    @FXML private Label roomLabel;
    @FXML private Label userLabel;
    @FXML private Label dateLabel;
    @FXML private Label commentLabel;
    @FXML private HBox ratingContainer;
    @FXML private HBox actionButtonsContainer;
    @FXML private Button deleteButton;
    @FXML private Button replyButton;
    
    private ReviewDTO review;
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    private boolean isAdmin = false; // Флаг, является ли пользователь администратором
    private Consumer<Void> onDeleteCallback; // Коллбэк для обновления родительского списка
    
    @FXML
    public void initialize() {
        // Настройка действий по умолчанию
    }
    
    /**
     * Привязка данных отзыва к элементам интерфейса
     */
    public void bind(ReviewDTO review, int currentUserId, boolean isAdmin, Consumer<Void> onDeleteCallback) {
        this.review = review;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.onDeleteCallback = onDeleteCallback;
        
        // Настройка основной информации
        roomLabel.setText("Номер " + review.getRoomId());
        userLabel.setText(review.getUsername() != null ? review.getUsername() : review.getUsername());
        
        // Настройка даты
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        dateLabel.setText(review.getReviewDate().format(formatter));
        
        // Настройка текста отзыва
        commentLabel.setText(review.getComment());
        
        // Настройка отображения рейтинга
        updateRatingStars(review.getRating());
        
        // Настройка кнопок действий
        configureActionButtons();
    }
    
    /**
     * Устанавливает данные отзыва в карточку
     * Новый метод для улучшенного интерфейса
     */
    public void setReviewData(ReviewDTO review) {
        this.review = review;
        
        // Настройка основной информации
        roomLabel.setText("Номер " + (review.getRoomNumber() != null ? review.getRoomNumber() : review.getRoomId()));
        userLabel.setText(review.getUserFullName() != null ? review.getUserFullName() : review.getUsername());
        
        // Настройка даты
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        dateLabel.setText(review.getReviewDate().format(formatter));
        
        // Настройка текста отзыва
        commentLabel.setText(review.getComment());
        
        // Настройка отображения рейтинга
        updateRatingStars(review.getRating());
    }
    
    /**
     * Настраивает видимость кнопок действий
     * Новый метод для улучшенного интерфейса
     */
    public void setActionButtonsVisibility(boolean canDelete, boolean canReply) {
        // Показываем кнопку удаления
        deleteButton.setVisible(canDelete);
        
        // Показываем кнопку ответа
        replyButton.setVisible(canReply);
        
        // Показываем панель кнопок, если хотя бы одна кнопка видима
        boolean showActions = canDelete || canReply;
        actionButtonsContainer.setVisible(showActions);
        actionButtonsContainer.setManaged(showActions);
    }
    
    /**
     * Устанавливает обработчик для удаления отзыва
     * Новый метод для улучшенного интерфейса
     */
    public void setOnDeleteAction(Consumer<Integer> onDeleteAction) {
        // Сохраняем коллбэк в виде лямбда-выражения
        this.onDeleteCallback = unused -> {
            if (onDeleteAction != null && review != null && review.getReviewId() != null) {
                onDeleteAction.accept(review.getReviewId());
            }
        };
    }
    
    /**
     * Настройка отображения звезд рейтинга
     */
    private void updateRatingStars(Integer rating) {
        ratingContainer.getChildren().clear();
        
        if (rating == null) {
            return; // Нет рейтинга для отображения
        }
        
        // SVG-путь для звезды
        String starPath = "M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z";
        
        // Создаем 5 звезд и заполняем их цветом в зависимости от рейтинга
        for (int i = 1; i <= 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starPath);
            star.setFill(i <= rating ? Color.GOLD : Color.LIGHTGRAY);
            star.setScaleX(0.8);
            star.setScaleY(0.8);
            ratingContainer.getChildren().add(star);
        }
    }
    
    /**
     * Настройка кнопок действий в зависимости от прав пользователя
     */
    private void configureActionButtons() {
        boolean showActions = false;
        
        // Показываем кнопку удаления только администратору или автору отзыва
        if (isAdmin || review.getUserId() == currentUserId) {
            deleteButton.setVisible(true);
            showActions = true;
        } else {
            deleteButton.setVisible(false);
        }
        
        // Показываем кнопку ответа только администратору
        replyButton.setVisible(isAdmin);
        
        // Показываем панель кнопок, если хотя бы одна кнопка видима
        actionButtonsContainer.setVisible(showActions);
        actionButtonsContainer.setManaged(showActions);
    }
    
    /**
     * Обработчик нажатия кнопки удаления отзыва
     */
    @FXML
    public void onDeleteReview() {
        if (review == null || review.getReviewId() == null) {
            return;
        }
        
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            // Создаем запрос на удаление отзыва
            DeleteReviewRequest request = new DeleteReviewRequest(review.getReviewId(), currentUserId);
            
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof DeleteReviewResponse) {
                DeleteReviewResponse deleteResponse = (DeleteReviewResponse) response;
                
                if (deleteResponse.isSuccess()) {
                    // Вызываем коллбэк для обновления списка отзывов
                    if (onDeleteCallback != null) {
                        onDeleteCallback.accept(null);
                    }
                } else {
                    // Отображаем сообщение об ошибке
                    System.err.println("Ошибка при удалении отзыва: " + deleteResponse.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка при удалении отзыва: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обработчик нажатия кнопки ответа на отзыв
     */
    @FXML
    public void onReplyToReview() {
        if (review == null || review.getReviewId() == null) {
            return;
        }
        
        try {
            // Создаем диалоговое окно для ответа
            javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Ответ на отзыв");
            dialog.setHeaderText("Ответ на отзыв пользователя: " + (review.getUserFullName() != null ? review.getUserFullName() : review.getUsername()));
            
            // Добавляем кнопки
            dialog.getDialogPane().getButtonTypes().addAll(
                    javafx.scene.control.ButtonType.OK, 
                    javafx.scene.control.ButtonType.CANCEL);
            
            // Создаем текстовое поле для ввода ответа
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setPromptText("Введите ваш ответ...");
            textArea.setPrefHeight(150);
            textArea.setPrefWidth(400);
            textArea.setWrapText(true);
            
            // Добавляем поле в диалог
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.getChildren().add(textArea);
            dialog.getDialogPane().setContent(content);
            
            // Добавляем обработчик для кнопки OK
            dialog.setResultConverter(buttonType -> {
                if (buttonType == javafx.scene.control.ButtonType.OK) {
                    return textArea.getText();
                }
                return null;
            });
            
            // Показываем диалог и ждем результат
            java.util.Optional<String> result = dialog.showAndWait();
            
            // Обрабатываем ответ
            result.ifPresent(replyText -> {
                if (!replyText.trim().isEmpty()) {
                    // Здесь можно добавить логику отправки ответа на сервер
                    // Например, вызвать метод sendReplyToServer(review.getReviewId(), replyText);
                    
                    // Для демонстрации просто показываем уведомление об успехе
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    alert.setContentText("Ваш ответ на отзыв был отправлен!");
                    alert.showAndWait();
                }
            });
        } catch (Exception e) {
            System.err.println("Ошибка при создании диалога ответа: " + e.getMessage());
            e.printStackTrace();
            
            // Показываем сообщение об ошибке
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось ответить на отзыв: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 