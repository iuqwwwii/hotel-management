// client/src/main/java/com/hotel/client/controller/GuestRoomsController.java
package com.hotel.client.controller;

import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ResourceBundle;

public class GuestRoomsController {
    @FXML private TableView<RoomDTO> roomsTable;
    @FXML private TableColumn<RoomDTO, Integer> colNumber;
    @FXML private TableColumn<RoomDTO, String> colType;
    @FXML private TableColumn<RoomDTO, Double> colPrice;
    @FXML private TableColumn<RoomDTO, String> colStatus;
    @FXML private TableColumn<RoomDTO, Void> colAction;
    
    // ID текущего пользователя
    private int currentUserId = 1;

    @FXML
    public void initialize() {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        addActionButtons();

        // Отложенный первичный вызов: после того как 'roomsTable' будет на сцене
        Platform.runLater(this::onRefresh);
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button bookBtn = new Button("Забронировать");
            {
                bookBtn.setOnAction(event -> {
                    RoomDTO dto = getTableView().getItems().get(getIndex());
                    onBookRoom(dto);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(bookBtn));
            }
        });
    }

    @FXML
    public void onRefresh() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in  = new ObjectInputStream(sock.getInputStream())) {

            out.writeObject(new FetchRoomsRequest());
            out.flush();
            FetchRoomsResponse resp = (FetchRoomsResponse) in.readObject();

            if (resp.isSuccess()) {
                List<RoomDTO> rooms = resp.getRooms();
                roomsTable.getItems().setAll(rooms);
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

    private void onBookRoom(RoomDTO dto) {
        try {
            // Загружаем форму бронирования
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/GuestBookingForm.fxml"),
                ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent root = loader.load();
            
            // Настраиваем контроллер с выбранной комнатой
            GuestBookingController controller = loader.getController();
            // Используем текущий ID пользователя вместо захардкоженного значения
            controller.setRoom(dto, currentUserId);
            
            // Создаем новый Stage для формы бронирования
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL); // Блокирует взаимодействие с другими окнами
            bookingStage.initOwner(roomsTable.getScene().getWindow());
            bookingStage.setTitle("Бронирование номера " + dto.getNumber());
            
            Scene scene = new Scene(root);
            bookingStage.setScene(scene);
            bookingStage.showAndWait();
            
            // После закрытия окна бронирования обновляем список комнат
            onRefresh();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    "Не удалось открыть форму бронирования",
                    e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String header,
                           String content) {
        Alert alert = new Alert(type);
        alert.initOwner(roomsTable.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) alert.setContentText(content);
        alert.showAndWait();
    }
}
