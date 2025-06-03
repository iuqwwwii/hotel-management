package com.hotel.client.controller;

import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuestRoomsCardsController {
    
    @FXML private TilePane roomsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roomTypeFilter;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private CheckBox availableOnlyCheck;
    @FXML private Label resultsCountLabel;
    
    private List<RoomDTO> allRooms = new ArrayList<>();
    private List<String> roomTypes = new ArrayList<>();
    
    // ID текущего пользователя
    private int currentUserId = 1;
    
    @FXML
    public void initialize() {
        // Настраиваем поле поиска для поиска при вводе
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal != null && !oldVal.equals(newVal)) {
                applyFilters();
            }
        });
        
        // Первичная загрузка
        Platform.runLater(this::loadRooms);
    }
    
    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
    
    private void loadRooms() {
        // Очищаем контейнер перед загрузкой
        roomsContainer.getChildren().clear();
        
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in  = new ObjectInputStream(sock.getInputStream())) {

            out.writeObject(new FetchRoomsRequest());
            out.flush();
            FetchRoomsResponse resp = (FetchRoomsResponse) in.readObject();

            if (resp.isSuccess()) {
                // Сохраняем все номера
                allRooms = resp.getRooms();
                
                // Собираем уникальные типы номеров для фильтра
                roomTypes = allRooms.stream()
                    .map(RoomDTO::getTypeName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
                
                // Заполняем выпадающий список типов
                roomTypeFilter.setItems(FXCollections.observableArrayList(roomTypes));
                
                // Применяем фильтры и отображаем номера
                applyFilters();
            } else {
                showAlert(Alert.AlertType.WARNING, "Внимание",
                        "Не удалось загрузить список номеров.", resp.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Не удалось соединиться с сервером.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onRefresh() {
        loadRooms();
    }
    
    @FXML
    public void onResetFilters() {
        // Сбрасываем все фильтры
        searchField.clear();
        roomTypeFilter.getSelectionModel().clearSelection();
        minPriceField.clear();
        maxPriceField.clear();
        availableOnlyCheck.setSelected(true);
        
        // Применяем фильтры (сброшенные)
        applyFilters();
    }
    
    @FXML
    public void onApplyFilters() {
        applyFilters();
    }
    
    private void applyFilters() {
        // Если нет данных - ничего не делаем
        if (allRooms == null || allRooms.isEmpty()) {
            return;
        }
        
        // Отфильтрованные номера
        List<RoomDTO> filteredRooms = new ArrayList<>(allRooms);
        
        // Применяем фильтр поиска
        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            String search = searchText.toLowerCase();
            filteredRooms = filteredRooms.stream()
                .filter(r -> r.getNumber().toLowerCase().contains(search) || 
                         r.getTypeName().toLowerCase().contains(search))
                .collect(Collectors.toList());
        }
        
        // Применяем фильтр по типу комнаты
        String selectedType = roomTypeFilter.getValue();
        if (selectedType != null && !selectedType.isEmpty()) {
            filteredRooms = filteredRooms.stream()
                .filter(r -> r.getTypeName().equals(selectedType))
                .collect(Collectors.toList());
        }
        
        // Применяем фильтр по минимальной цене
        String minPrice = minPriceField.getText();
        if (minPrice != null && !minPrice.isEmpty()) {
            try {
                double min = Double.parseDouble(minPrice);
                filteredRooms = filteredRooms.stream()
                    .filter(r -> r.getBasePrice() >= min)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                // Просто игнорируем некорректное значение
            }
        }
        
        // Применяем фильтр по максимальной цене
        String maxPrice = maxPriceField.getText();
        if (maxPrice != null && !maxPrice.isEmpty()) {
            try {
                double max = Double.parseDouble(maxPrice);
                filteredRooms = filteredRooms.stream()
                    .filter(r -> r.getBasePrice() <= max)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                // Просто игнорируем некорректное значение
            }
        }
        
        // Применяем фильтр "только доступные"
        if (availableOnlyCheck.isSelected()) {
            filteredRooms = filteredRooms.stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .collect(Collectors.toList());
        }
        
        // Обновляем интерфейс
        loadRoomCards(filteredRooms);
        resultsCountLabel.setText("Найдено номеров: " + filteredRooms.size());
    }
    
    private void loadRoomCards(List<RoomDTO> rooms) {
        // Очищаем текущие карточки
        roomsContainer.getChildren().clear();
        
        for (RoomDTO room : rooms) {
            try {
                // Загружаем шаблон карточки
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RoomCardEnhanced.fxml"),
                    ResourceBundle.getBundle("i18n.Bundle_ru")
                );
                Parent roomCard = loader.load();
                
                // Настраиваем контроллер карточки
                RoomCardEnhancedController cardController = loader.getController();
                cardController.bind(room);
                // Передаем ID текущего пользователя
                cardController.setCurrentUserId(currentUserId);
                
                // Добавляем карточку в контейнер
                roomsContainer.getChildren().add(roomCard);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showAlert(Alert.AlertType type,
                          String title,
                          String header,
                          String content) {
        Alert alert = new Alert(type);
        if (roomsContainer.getScene() != null) {
            alert.initOwner(roomsContainer.getScene().getWindow());
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) alert.setContentText(content);
        alert.showAndWait();
    }
} 