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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminManageRoomsController {

    @FXML private TableView<RoomDTO> roomsTable;
    @FXML private TableColumn<RoomDTO, Integer> idColumn;
    @FXML private TableColumn<RoomDTO, String> numberColumn;
    @FXML private TableColumn<RoomDTO, String> typeColumn;
    @FXML private TableColumn<RoomDTO, String> statusColumn;
    @FXML private TableColumn<RoomDTO, Double> priceColumn;
    @FXML private TableColumn<RoomDTO, String> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    
    private final ObservableList<RoomDTO> roomsList = FXCollections.observableArrayList();
    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
    
    @FXML
    public void initialize() {
        // Инициализация таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        
        // Настройка колонки с кнопками действий
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Изменить");
            private final Button deleteButton = new Button("Удалить");
            private final HBox hbox = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    RoomDTO room = getTableView().getItems().get(getIndex());
                    openEditRoomDialog(room);
                });

                deleteButton.setOnAction(event -> {
                    RoomDTO room = getTableView().getItems().get(getIndex());
                    confirmAndDeleteRoom(room);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

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
        
        // Привязка таблицы к данным
        roomsTable.setItems(roomsList);
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
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить список номеров");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка подключения", "Не удалось подключиться к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusValue = statusFilter.getValue();
        String typeValue = typeFilter.getValue();
        
        ObservableList<RoomDTO> filteredList = FXCollections.observableArrayList();
        
        for (RoomDTO room : roomsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                                   room.getNumber().toLowerCase().contains(searchText);
            
            boolean matchesStatus = statusValue.equals("Все статусы") || 
                                   room.getStatus().equals(statusValue);
            
            boolean matchesType = typeValue.equals("Все типы") || 
                                 room.getTypeName().equals(typeValue);
            
            if (matchesSearch && matchesStatus && matchesType) {
                filteredList.add(room);
            }
        }
        
        roomsTable.setItems(filteredList);
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
            dialogStage.initOwner(roomsTable.getScene().getWindow());
            
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
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteRoom(room);
        }
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
    private void switchToCardView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminViewRoomsCards.fxml"), bundle);
            Node cardView = loader.load();
            
            // Получаем borderPane - корневой элемент административного интерфейса
            BorderPane borderPane = (BorderPane) roomsTable.getScene().getWindow().getScene().getRoot();
            
            // Заменяем текущий центральный элемент на карточный вид
            borderPane.setCenter(cardView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось переключиться на карточный вид");
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