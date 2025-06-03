package com.hotel.client.controller;

import com.hotel.client.util.ImageHandler;
import com.hotel.common.FetchRoomImagesRequest;
import com.hotel.common.FetchRoomImagesResponse;
import com.hotel.common.ImageDTO;
import com.hotel.common.RoomDTO;
import javafx.application.Platform;
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
import com.hotel.client.util.ImageHandler;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ResourceBundle;

public class RoomCardEnhancedController {
    @FXML private ImageView imgView;
    @FXML private Label lblRoomNumber;
    @FXML private Label lblStatus;
    @FXML private Label lblType;
    @FXML private Label lblCapacity;
    @FXML private Label lblFloor;
    @FXML private Label lblPrice;
    @FXML private Button bookButton;

    private RoomDTO room;
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    private List<ImageDTO> roomImages;
    private int currentImageIndex = 0;

    public void bind(RoomDTO room) {
        this.room = room;
        
        lblRoomNumber.setText("№ " + room.getNumber());
        lblType.setText(room.getTypeName());
        lblPrice.setText(String.format("%.2f BYN / ночь", room.getBasePrice()));
        
        // Устанавливаем статус комнаты с цветовым кодированием
        String status = room.getStatus();
        lblStatus.setText(status);
        
        if ("AVAILABLE".equals(status)) {
            lblStatus.setText("Доступен");
            lblStatus.setStyle("-fx-text-fill: #009688;");
        } else if ("MAINTENANCE".equals(status)) {
            lblStatus.setText("На обслуживании");
            lblStatus.setStyle("-fx-text-fill: #FF9800;");
        } else if ("OCCUPIED".equals(status)) {
            lblStatus.setText("Занят");
            lblStatus.setStyle("-fx-text-fill: #F44336;");
        }
        
        // Устанавливаем вместимость и этаж
        // Эти данные могут отсутствовать в текущей реализации RoomDTO
        // Поэтому используем временные данные
        int capacity = 2; // Заглушка
        int floor = Integer.parseInt(room.getNumber().substring(0, 1)); // Примерная логика
        
        lblCapacity.setText(capacity + getCapacitySuffix(capacity));
        lblFloor.setText(String.valueOf(floor));
        
        // Загружаем изображения комнаты
        loadRoomImages();
        
        // Отключаем кнопку бронирования, если комната недоступна
        bookButton.setDisable(!"AVAILABLE".equals(status));
        if (!"AVAILABLE".equals(status)) {
            bookButton.setStyle("-fx-opacity: 0.5;");
        }
    }
    
    private void loadRoomImages() {
        // Сначала устанавливаем стандартное изображение
        setDefaultImage();
        
        // Запускаем загрузку изображений в отдельном потоке
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                FetchRoomImagesRequest request = new FetchRoomImagesRequest(room.getRoomId());
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                if (response instanceof FetchRoomImagesResponse) {
                    FetchRoomImagesResponse imagesResponse = (FetchRoomImagesResponse) response;
                    if (imagesResponse.isSuccess() && imagesResponse.getImages() != null && !imagesResponse.getImages().isEmpty()) {
                        roomImages = imagesResponse.getImages();
                        // Обновляем UI в потоке JavaFX
                        Platform.runLater(() -> displayImage(0));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Ошибка при загрузке изображений комнаты: " + e.getMessage());
            }
        }).start();
    }
    
    private void displayImage(int index) {
        if (roomImages == null || roomImages.isEmpty()) {
            return;
        }
        
        currentImageIndex = index;
        if (currentImageIndex < 0) {
            currentImageIndex = roomImages.size() - 1;
        } else if (currentImageIndex >= roomImages.size()) {
            currentImageIndex = 0;
        }
        
        ImageDTO image = roomImages.get(currentImageIndex);
        if (image.getImageData() != null) {
            try {
                Image img = new Image(new ByteArrayInputStream(image.getImageData()));
                imgView.setImage(img);
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultImage();
            }
        }
    }
    
    @FXML
    private void nextImage() {
        if (roomImages != null && !roomImages.isEmpty()) {
            displayImage(currentImageIndex + 1);
        }
    }
    
    @FXML
    private void previousImage() {
        if (roomImages != null && !roomImages.isEmpty()) {
            displayImage(currentImageIndex - 1);
        }
    }
    
    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
    
    private String getCapacitySuffix(int capacity) {
        if (capacity == 1) {
            return " человек";
        } else if (capacity > 1 && capacity < 5) {
            return " человека";
        } else {
            return " человек";
        }
    }
    
    private void setDefaultImage() {
        // Установка стандартного изображения с использованием ImageHandler
        try {
            // Импортируем наш новый класс для работы с изображениям
            
            // Пробуем загрузить стандартное изображение из ресурсов
            Image defaultImg = ImageHandler.loadImageFromResources("/images/logo.png", 320, 200);
            
            if (defaultImg != null) {
                imgView.setImage(defaultImg);
            } else {
                // Если стандартное изображение недоступно, создаем программно
                String roomNumber = (room != null && room.getNumber() != null) ? room.getNumber() : "";
                Image generatedImage = ImageHandler.createRoomImage(roomNumber, 320, 200);
                imgView.setImage(generatedImage);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при установке изображения по умолчанию: " + e.getMessage());
            e.printStackTrace();
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
            // Используем текущий ID пользователя вместо захардкоженного значения
            controller.setRoom(room, currentUserId);
            
            // Создаем новый Stage для формы бронирования
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL); // Блокирует взаимодействие с другими окнами
            bookingStage.initOwner(bookButton.getScene().getWindow());
            bookingStage.setTitle("Бронирование номера " + room.getNumber());
            
            Scene scene = new Scene(root);
            bookingStage.setScene(scene);
            bookingStage.showAndWait();
            
            // После закрытия окна бронирования обновляем родительский контейнер
            // Эта логика должна быть реализована в родительском контроллере
            
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