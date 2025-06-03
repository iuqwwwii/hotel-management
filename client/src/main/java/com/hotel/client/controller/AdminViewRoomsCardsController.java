package com.hotel.client.controller;

import com.hotel.common.DeleteRoomRequest;
import com.hotel.common.DeleteRoomResponse;
import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminViewRoomsCardsController {
    @FXML private TilePane roomsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    
    private final ObservableList<RoomDTO> roomsList = FXCollections.observableArrayList();
    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
    
    @FXML
    public void initialize() {
        // Настройка фильтров
        statusFilter.getItems().addAll("Все статусы", "AVAILABLE", "BOOKED", "MAINTENANCE");
        statusFilter.setValue("Все статусы");
        statusFilter.setOnAction(e -> applyFilters());

        typeFilter.getItems().addAll("Все типы", "Одноместный", "Двухместный", "Полулюкс", "Люкс");
        typeFilter.setValue("Все типы");
        typeFilter.setOnAction(e -> applyFilters());
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        
        // Загрузка данных
        loadRooms();
    }
    
    private void loadRooms() {
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject(new FetchRoomsRequest());
            out.flush();
            
            Object response = in.readObject();
            if (response instanceof FetchRoomsResponse) {
                FetchRoomsResponse roomsResponse = (FetchRoomsResponse) response;
                if (roomsResponse.isSuccess()) {
                    roomsList.clear();
                    roomsList.addAll(roomsResponse.getRooms());
                    displayRooms(roomsList);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить список номеров");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка подключения", "Не удалось подключиться к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayRooms(List<RoomDTO> rooms) {
        roomsContainer.getChildren().clear();
        
        for (RoomDTO room : rooms) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminRoomCard.fxml"), bundle);
                VBox roomCard = loader.load();
                
                // Установка данных карточки
                Label roomNumber = (Label) roomCard.lookup("#roomNumber");
                Label roomType = (Label) roomCard.lookup("#roomType");
                Label roomPrice = (Label) roomCard.lookup("#roomPrice");
                Label roomStatus = (Label) roomCard.lookup("#roomStatus");
                Button editButton = (Button) roomCard.lookup("#editButton");
                Button deleteButton = (Button) roomCard.lookup("#deleteButton");
                
                roomNumber.setText("Номер №" + room.getNumber());
                roomType.setText(room.getTypeName());
                roomPrice.setText(String.format("%.2f₽ / ночь", room.getBasePrice()));
                roomStatus.setText(getStatusText(room.getStatus()));
                
                // Установка стиля в зависимости от статуса
                switch (room.getStatus()) {
                    case "AVAILABLE":
                        roomStatus.setStyle("-fx-text-fill: green;");
                        break;
                    case "BOOKED":
                        roomStatus.setStyle("-fx-text-fill: red;");
                        break;
                    case "MAINTENANCE":
                        roomStatus.setStyle("-fx-text-fill: orange;");
                        break;
                }
                
                // Установка обработчиков событий кнопок
                editButton.setOnAction(event -> openEditRoomDialog(room));
                deleteButton.setOnAction(event -> confirmAndDeleteRoom(room));
                
                roomsContainer.getChildren().add(roomCard);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getStatusText(String status) {
        switch (status) {
            case "AVAILABLE": return "Доступен";
            case "BOOKED": return "Забронирован";
            case "MAINTENANCE": return "На обслуживании";
            default: return status;
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusValue = statusFilter.getValue();
        String typeValue = typeFilter.getValue();
        
        List<RoomDTO> filteredRooms = roomsList.stream()
            .filter(room -> searchText.isEmpty() || 
                   room.getNumber().toLowerCase().contains(searchText))
            .filter(room -> statusValue.equals("Все статусы") || 
                   room.getStatus().equals(statusValue))
            .filter(room -> typeValue.equals("Все типы") || 
                   room.getTypeName().equals(typeValue))
            .collect(Collectors.toList());
        
        displayRooms(filteredRooms);
    }
    
    @FXML
    private void onAddRoom() {
        openEditRoomDialog(null); // null означает создание нового номера
    }
    
    private void openEditRoomDialog(RoomDTO room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminEditRoom.fxml"), bundle);
            VBox dialogPane = loader.load();
            
            AdminEditRoomController controller = loader.getController();
            if (room != null) {
                controller.setRoom(room); // редактирование существующего
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null ? "Добавление номера" : "Редактирование номера");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(roomsContainer.getScene().getWindow());
            
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);
            
            // При закрытии диалога обновляем список номеров
            dialogStage.showAndWait();
            loadRooms();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно редактирования номера");
            e.printStackTrace();
        }
    }
    
    private void confirmAndDeleteRoom(RoomDTO room) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление номера " + room.getNumber());
        alert.setContentText("Вы действительно хотите удалить этот номер?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                deleteRoom(room);
            }
        });
    }
    
    private void deleteRoom(RoomDTO room) {
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
             
            // Создаем запрос на удаление номера
            DeleteRoomRequest request = new DeleteRoomRequest(room.getRoomId());
            out.writeObject(request);
            out.flush();
            
            // Обрабатываем ответ
            Object response = in.readObject();
            if (response instanceof DeleteRoomResponse) {
                DeleteRoomResponse deleteResponse = (DeleteRoomResponse) response;
                if (deleteResponse.isSuccess()) {
                    loadRooms(); // Перезагружаем список номеров
                    showAlert(Alert.AlertType.INFORMATION, "Успешно", "Номер успешно удален");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", 
                              "Не удалось удалить номер: " + deleteResponse.getErrorMessage());
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка подключения", 
                      "Не удалось подключиться к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onRefresh() {
        loadRooms();
    }

    @FXML
    private void switchToTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminManageRooms.fxml"), bundle);
            Node tableView = loader.load();
            
            // Получаем borderPane - корневой элемент административного интерфейса
            BorderPane borderPane = (BorderPane) roomsContainer.getScene().getWindow().getScene().getRoot();
            
            // Заменяем текущий центральный элемент на табличный вид
            borderPane.setCenter(tableView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось переключиться на табличный вид");
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 