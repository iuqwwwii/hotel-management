package com.hotel.client.controller;

import com.hotel.common.RoomDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class RoomCardController {
    @FXML private ImageView imgView;
    @FXML private Label lblType;
    @FXML private Label lblPrice;
    @FXML private Button bookButton;

    private RoomDTO room;

    public void bind(RoomDTO room) {
        this.room = room;
        lblType.setText(room.getTypeName());
        lblPrice.setText(String.format("%.0f BYN/night", room.getBasePrice()));
        
        // Используем стандартное изображение для всех комнат
        setDefaultImage();
    }
    
    private void setDefaultImage() {
        // Установка стандартного изображения
        try {
            // Используем существующее изображение из ресурсов
            Image defaultImg = new Image(getClass().getResourceAsStream("/images/logo.png"), 
                                         300, 200, true, true);
            imgView.setImage(defaultImg);
        } catch (Exception e) {
            // Если стандартное изображение недоступно
            System.err.println("Не удалось загрузить стандартное изображение: " + e.getMessage());
        }
    }

    @FXML
    private void onBook() {
        try {
            // Загружаем форму бронирования
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/GuestBookingForm.fxml"),
                ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent root = loader.load();
            
            // Настраиваем контроллер с выбранной комнатой
            GuestBookingController controller = loader.getController();
            // Предполагается, что пользователь с ID=3 - это "guest1" из примера
            controller.setRoom(room, 3); 
            
            // Создаем новый Stage для формы бронирования
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL); // Блокирует взаимодействие с другими окнами
            bookingStage.initOwner(bookButton.getScene().getWindow());
            bookingStage.setTitle("Бронирование номера " + room.getNumber());
            
            Scene scene = new Scene(root);
            bookingStage.setScene(scene);
            bookingStage.showAndWait();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть форму бронирования");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
