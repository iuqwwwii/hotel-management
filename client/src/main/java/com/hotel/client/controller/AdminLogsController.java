package com.hotel.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdminLogsController {
    
    @FXML private ComboBox<String> logLevelComboBox;
    @FXML private ComboBox<String> logCategoryComboBox;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<LogEntry> logsTable;
    @FXML private TableColumn<LogEntry, String> colTimestamp;
    @FXML private TableColumn<LogEntry, String> colLevel;
    @FXML private TableColumn<LogEntry, String> colCategory;
    @FXML private TableColumn<LogEntry, String> colMessage;
    @FXML private Label statusLabel;
    
    private ObservableList<LogEntry> allLogs = FXCollections.observableArrayList();
    private ObservableList<LogEntry> filteredLogs = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Настройка колонок таблицы
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        
        // Инициализация фильтров
        logLevelComboBox.setItems(FXCollections.observableArrayList(
            "Все уровни", "INFO", "WARNING", "ERROR", "DEBUG"
        ));
        logLevelComboBox.getSelectionModel().selectFirst();
        
        logCategoryComboBox.setItems(FXCollections.observableArrayList(
            "Все категории", "Авторизация", "Бронирование", "Номера", "Отзывы", "Система"
        ));
        logCategoryComboBox.getSelectionModel().selectFirst();
        
        periodComboBox.setItems(FXCollections.observableArrayList(
            "Все время", "Сегодня", "Вчера", "Последние 7 дней", "Последние 30 дней"
        ));
        periodComboBox.getSelectionModel().selectFirst();
        
        // Загрузка демонстрационных логов
        loadDemoLogs();
        
        // Применение фильтров
        applyFilters();
    }
    
    @FXML
    public void onRefresh() {
        loadDemoLogs(); // В реальной системе здесь будет запрос к серверу
        applyFilters();
    }
    
    @FXML
    public void onClear() {
        // Показываем диалог подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистка журнала");
        alert.setContentText("Вы уверены, что хотите очистить журнал событий?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            allLogs.clear();
            filteredLogs.clear();
            logsTable.setItems(filteredLogs);
            updateStatusLabel();
        }
    }
    
    @FXML
    public void onSearch() {
        applyFilters();
    }
    
    @FXML
    public void onExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт журнала");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );
        fileChooser.setInitialFileName("hotel_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        File file = fileChooser.showSaveDialog(logsTable.getScene().getWindow());
        if (file != null) {
            exportLogsToCSV(file);
        }
    }
    
    private void loadDemoLogs() {
        // Демонстрационные данные для примера
        allLogs.clear();
        
        // Генерация случайных логов
        Random random = new Random();
        String[] levels = {"INFO", "WARNING", "ERROR", "DEBUG"};
        String[] categories = {"Авторизация", "Бронирование", "Номера", "Отзывы", "Система"};
        
        List<String> messages = new ArrayList<>();
        messages.add("Пользователь user123 вошел в систему");
        messages.add("Ошибка при обработке бронирования #12345");
        messages.add("Номер 101 забронирован на период 15.06.2023 - 20.06.2023");
        messages.add("Создан новый отзыв для номера 205");
        messages.add("Система запущена");
        messages.add("Резервное копирование базы данных выполнено");
        messages.add("Попытка несанкционированного доступа с IP 192.168.1.100");
        messages.add("Обновлена информация о номере 302");
        messages.add("Отменено бронирование #54321");
        messages.add("Пользователь admin выполнил экспорт отчета");
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 100; i++) {
            LocalDateTime timestamp = now.minusHours(random.nextInt(240)); // За последние 10 дней
            String level = levels[random.nextInt(levels.length)];
            String category = categories[random.nextInt(categories.length)];
            String message = messages.get(random.nextInt(messages.size()));
            
            allLogs.add(new LogEntry(
                timestamp.format(formatter),
                level,
                category,
                message + " (ID: " + (1000 + i) + ")"
            ));
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedLevel = logLevelComboBox.getValue();
        String selectedCategory = logCategoryComboBox.getValue();
        
        filteredLogs.clear();
        
        for (LogEntry log : allLogs) {
            // Применение фильтра по уровню
            if (!"Все уровни".equals(selectedLevel) && !log.getLevel().equals(selectedLevel)) {
                continue;
            }
            
            // Применение фильтра по категории
            if (!"Все категории".equals(selectedCategory) && !log.getCategory().equals(selectedCategory)) {
                continue;
            }
            
            // Применение фильтра по поисковому тексту
            if (!searchText.isEmpty() && 
                !log.getMessage().toLowerCase().contains(searchText) &&
                !log.getCategory().toLowerCase().contains(searchText) &&
                !log.getLevel().toLowerCase().contains(searchText)) {
                continue;
            }
            
            // Если прошли все фильтры, добавляем в отфильтрованный список
            filteredLogs.add(log);
        }
        
        logsTable.setItems(filteredLogs);
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        statusLabel.setText("Загружено записей: " + filteredLogs.size());
    }
    
    private void exportLogsToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Заголовок CSV
            writer.println("Время,Уровень,Категория,Сообщение");
            
            // Данные
            for (LogEntry log : filteredLogs) {
                writer.println(
                    escapeCSV(log.getTimestamp()) + "," +
                    escapeCSV(log.getLevel()) + "," +
                    escapeCSV(log.getCategory()) + "," +
                    escapeCSV(log.getMessage())
                );
            }
            
            // Показываем сообщение об успехе
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Экспорт завершен");
                alert.setHeaderText(null);
                alert.setContentText("Журнал успешно экспортирован в файл:\n" + file.getAbsolutePath());
                alert.showAndWait();
            });
            
        } catch (FileNotFoundException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не удалось экспортировать журнал");
                alert.setContentText("Ошибка: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Класс для представления записи журнала в таблице
     */
    public static class LogEntry {
        private final String timestamp;
        private final String level;
        private final String category;
        private final String message;
        
        public LogEntry(String timestamp, String level, String category, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.category = category;
            this.message = message;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 