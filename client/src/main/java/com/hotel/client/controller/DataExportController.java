package com.hotel.client.controller;

import com.hotel.client.strategy.ExportStrategy;
import com.hotel.client.strategy.ExportStrategyFactory;
import com.hotel.client.util.NotificationManager;
import com.hotel.common.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;

/**
 * Контроллер для экспорта данных в CSV и Excel с использованием паттерна Стратегия
 */
public class DataExportController {
    @FXML private RadioButton partnersRadio;
    @FXML private RadioButton bookingsRadio;
    @FXML private RadioButton roomsRadio;
    @FXML private RadioButton reviewsRadio;
    @FXML private RadioButton statisticsRadio;
    
    @FXML private RadioButton csvRadio;
    @FXML private RadioButton excelRadio;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField filterField;
    @FXML private CheckBox includeHeadersCheckbox;
    
    @FXML private Button exportButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    
    private Timeline statusTimeline;
    
    @FXML
    public void initialize() {
        // Установка текущей даты и периода последних 30 дней
        LocalDate today = LocalDate.now();
        endDatePicker.setValue(today);
        startDatePicker.setValue(today.minusDays(30));
        
        // Инициализация анимации для статусной метки
        setupStatusAnimation();
        
        // Применение дополнительных стилей к элементам
        applyStyleEffects();
    }
    
    /**
     * Настраивает анимацию для статусной метки
     */
    private void setupStatusAnimation() {
        // Создаем анимацию затухания для статусной метки
        statusTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> hideStatus())
        );
        statusTimeline.setCycleCount(1);
    }
    
    /**
     * Применяет дополнительные эффекты стилей к элементам интерфейса
     */
    private void applyStyleEffects() {
        // Применяем эффекты наведения для кнопок
        exportButton.setOnMouseEntered(e -> exportButton.setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        exportButton.setOnMouseExited(e -> exportButton.setStyle("-fx-scale-x: 1; -fx-scale-y: 1;"));
        
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-scale-x: 1; -fx-scale-y: 1;"));
    }
    
    @FXML
    private void onExport() {
        setStatus("Подготовка данных для экспорта...", false);
        
        // Определяем тип экспортируемых данных
        final String dataType;
        if (partnersRadio.isSelected()) dataType = "partners";
        else if (bookingsRadio.isSelected()) dataType = "bookings";
        else if (roomsRadio.isSelected()) dataType = "rooms";
        else if (reviewsRadio.isSelected()) dataType = "reviews";
        else if (statisticsRadio.isSelected()) dataType = "statistics";
        else dataType = "unknown";
        
        // Определяем формат экспорта
        String format = csvRadio.isSelected() ? "csv" : "xlsx";
        
        // Фильтр и диапазон дат
        String filter = filterField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        // Проверка дат
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showError("Дата начала не может быть позже даты окончания");
            return;
        }
        
        // Диалог выбора файла
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить экспортированные данные");
        
        // Настройка расширения и имени файла по умолчанию
        FileChooser.ExtensionFilter extFilter;
        if ("csv".equals(format)) {
            extFilter = new FileChooser.ExtensionFilter("CSV файлы (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(dataType + "_export.csv");
        } else {
            extFilter = new FileChooser.ExtensionFilter("Excel файлы (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(dataType + "_export.xlsx");
        }
        
        // Показываем диалог выбора файла
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file == null) {
            // Пользователь отменил операцию
            setStatus("Экспорт отменен", false);
            return;
        }
        
        // Блокируем кнопку экспорта
        exportButton.setDisable(true);
        
        // Выполняем экспорт в отдельном потоке
        new Thread(() -> {
            boolean success = false;
            
            try {
                // Загружаем данные с сервера
                Platform.runLater(() -> setStatus("Загрузка данных с сервера...", false));
                
                // Создаем и используем соответствующую стратегию экспорта
                ExportStrategy strategy = ExportStrategyFactory.createStrategy(
                    dataType, format, startDate, endDate, filter, includeHeadersCheckbox.isSelected()
                );
                
                // Выполняем экспорт
                success = strategy.export(file);
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка при экспорте: " + e.getMessage()));
            } finally {
                // Разблокируем кнопку экспорта
                final boolean finalSuccess = success;
                Platform.runLater(() -> {
                    exportButton.setDisable(false);
                    if (finalSuccess) {
                        setStatus("Экспорт успешно завершен", true);
                        // Показываем уведомление через общий менеджер уведомлений
                        NotificationManager.getInstance().showSuccess(
                            "Экспорт данных", 
                            "Данные успешно экспортированы в выбранный файл"
                        );
                    }
                });
            }
        }).start();
    }
    
    @FXML
    private void onCancel() {
        // Закрываем окно с анимацией затухания
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), 
                                                   cancelButton.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
        fadeOut.play();
    }

    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String message) {
        setStatus(message, true);
    }
    
    /**
     * Устанавливает текст и стиль статусной метки
     */
    private void setStatus(String message, boolean isError) {
        if (statusTimeline != null) {
            statusTimeline.stop();
        }
        
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        
        // Показываем метку с анимацией
        if (!statusLabel.isVisible()) {
            statusLabel.setVisible(true);
            statusLabel.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), statusLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
        
        // Запускаем таймер для скрытия метки
        statusTimeline.playFromStart();
    }
    
    /**
     * Скрывает статусную метку с анимацией
     */
    private void hideStatus() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), statusLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> statusLabel.setVisible(false));
        fadeOut.play();
    }
} 