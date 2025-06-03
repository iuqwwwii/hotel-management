package com.hotel.client.controller;

import com.hotel.common.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminEditRoomController {
    @FXML private TextField numberField;
    @FXML private ComboBox<String> floorComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private FlowPane imagesContainer;
    @FXML private Button addImageButton;
    @FXML private Button deleteImageButton;
    
    private RoomDTO roomToEdit;
    private boolean isNewRoom = true;
    private List<ImageDTO> roomImages = new ArrayList<>();
    private ImageView selectedImageView;
    
    @FXML
    public void initialize() {
        // Инициализация комбобоксов
        floorComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9");
        floorComboBox.setValue("1");
        
        typeComboBox.getItems().addAll("Одноместный", "Двухместный", "Полулюкс", "Люкс");
        statusComboBox.getItems().addAll("AVAILABLE", "BOOKED", "MAINTENANCE");
        
        // Автоматическое определение этажа по введенному номеру
        numberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                try {
                    int floor = Character.getNumericValue(newValue.charAt(0));
                    if (floor >= 1 && floor <= 9) {
                        floorComboBox.setValue(String.valueOf(floor));
                    }
                } catch (Exception ignored) {}
            }
        });
        
        // Валидация для цены (только числа)
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });
        
        // Обработчик выбора изображения
        imagesContainer.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof ImageView) {
                selectImage((ImageView) event.getTarget());
            } else {
                deselectAllImages();
            }
        });
    }
    
    public void setRoom(RoomDTO room) {
        this.roomToEdit = room;
        this.isNewRoom = false;
        
        numberField.setText(room.getNumber());
        
        // Попробуем определить этаж из номера
        if (room.getNumber() != null && room.getNumber().length() > 0) {
            try {
                int floor = Character.getNumericValue(room.getNumber().charAt(0));
                if (floor >= 1 && floor <= 9) {
                    floorComboBox.setValue(String.valueOf(floor));
                }
            } catch (Exception ignored) {}
        }
        
        statusComboBox.setValue(room.getStatus());
        typeComboBox.setValue(room.getTypeName());
        priceField.setText(String.valueOf(room.getBasePrice()));
        if (room.getDescription() != null) {
            descriptionArea.setText(room.getDescription());
        }
        
        // Загружаем изображения комнаты
        if (!isNewRoom) {
            loadRoomImages();
        }
    }
    
    private void loadRoomImages() {
        // Запускаем загрузку изображений в отдельном потоке
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                FetchRoomImagesRequest request = new FetchRoomImagesRequest(roomToEdit.getRoomId());
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                if (response instanceof FetchRoomImagesResponse) {
                    FetchRoomImagesResponse imagesResponse = (FetchRoomImagesResponse) response;
                    if (imagesResponse.isSuccess() && imagesResponse.getImages() != null) {
                        roomImages = imagesResponse.getImages();
                        // Обновляем UI в потоке JavaFX
                        Platform.runLater(() -> displayImages());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Ошибка при загрузке изображений комнаты: " + e.getMessage());
            }
        }).start();
    }
    
    private void displayImages() {
        imagesContainer.getChildren().clear();
        
        for (ImageDTO image : roomImages) {
            if (image.getImageData() != null) {
                try {
                    Image img = new Image(new ByteArrayInputStream(image.getImageData()));
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    imageView.setUserData(image); // Сохраняем ссылку на ImageDTO
                    
                    // Создаем контейнер для изображения с рамкой
                    StackPane container = new StackPane(imageView);
                    container.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 2;");
                    
                    imagesContainer.getChildren().add(container);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @FXML
    private void onAddImage() {
        if (isNewRoom) {
            showError("Сначала сохраните комнату, затем добавьте изображения");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(addImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                String description = selectedFile.getName();
                
                // Отправляем запрос на добавление изображения
                new Thread(() -> {
                    try (Socket sock = new Socket("localhost", 5555);
                         ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                        
                        AddRoomImageRequest request = new AddRoomImageRequest(
                            roomToEdit.getRoomId(),
                            description,
                            imageData,
                            selectedFile.getName()
                        );
                        
                        out.writeObject(request);
                        out.flush();
                        
                        Object response = in.readObject();
                        if (response instanceof AddRoomImageResponse) {
                            AddRoomImageResponse imageResponse = (AddRoomImageResponse) response;
                            if (imageResponse.isSuccess() && imageResponse.getImage() != null) {
                                roomImages.add(imageResponse.getImage());
                                // Обновляем UI в потоке JavaFX
                                Platform.runLater(() -> displayImages());
                            } else {
                                Platform.runLater(() -> showError("Ошибка при добавлении изображения: " + 
                                                                 imageResponse.getMessage()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showError("Ошибка при отправке изображения: " + e.getMessage()));
                    }
                }).start();
                
            } catch (IOException e) {
                showError("Ошибка при чтении файла: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onDeleteImage() {
        if (selectedImageView == null) {
            return;
        }
        
        ImageDTO selectedImage = (ImageDTO) selectedImageView.getUserData();
        if (selectedImage == null || selectedImage.getImageId() == null) {
            return;
        }
        
        // Подтверждение удаления
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Подтверждение");
        confirmDialog.setHeaderText("Удаление изображения");
        confirmDialog.setContentText("Вы уверены, что хотите удалить это изображение?");
        
        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Отправляем запрос на удаление изображения
                new Thread(() -> {
                    try (Socket sock = new Socket("localhost", 5555);
                         ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                        
                        DeleteRoomImageRequest request = new DeleteRoomImageRequest(selectedImage.getImageId());
                        out.writeObject(request);
                        out.flush();
                        
                        Object responseObj = in.readObject();
                        if (responseObj instanceof DeleteRoomImageResponse) {
                            DeleteRoomImageResponse deleteResponse = (DeleteRoomImageResponse) responseObj;
                            if (deleteResponse.isSuccess()) {
                                // Удаляем изображение из списка и обновляем UI
                                Platform.runLater(() -> {
                                    roomImages.remove(selectedImage);
                                    displayImages();
                                    deselectAllImages();
                                });
                            } else {
                                Platform.runLater(() -> showError("Ошибка при удалении изображения: " + 
                                                                deleteResponse.getMessage()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showError("Ошибка при отправке запроса: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }
    
    private void selectImage(ImageView imageView) {
        deselectAllImages();
        
        // Находим StackPane-контейнер выбранного изображения
        StackPane container = (StackPane) imageView.getParent();
        container.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-padding: 1;");
        
        selectedImageView = imageView;
        deleteImageButton.setDisable(false);
    }
    
    private void deselectAllImages() {
        for (int i = 0; i < imagesContainer.getChildren().size(); i++) {
            if (imagesContainer.getChildren().get(i) instanceof StackPane) {
                StackPane container = (StackPane) imagesContainer.getChildren().get(i);
                container.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 2;");
            }
        }
        
        selectedImageView = null;
        deleteImageButton.setDisable(true);
    }
    
    @FXML
    private void onCancel() {
        closeDialog();
    }
    
    @FXML
    private void onSave() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Создаем запрос
            int roomId = isNewRoom ? 0 : roomToEdit.getRoomId();
            
            // Формируем номер комнаты, включая этаж, если номер не начинается с цифры этажа
            String roomNumber = numberField.getText().trim();
            int selectedFloor = Integer.parseInt(floorComboBox.getValue());
            
            // Если первая цифра номера не соответствует выбранному этажу, добавим этаж в номер
            if (roomNumber.length() == 0 || Character.getNumericValue(roomNumber.charAt(0)) != selectedFloor) {
                roomNumber = selectedFloor + roomNumber;
            }
            
            UpdateRoomRequest request = new UpdateRoomRequest(
                roomId,
                roomNumber,
                statusComboBox.getValue(),
                typeComboBox.getValue(),
                Double.parseDouble(priceField.getText().trim()),
                descriptionArea.getText().trim()
            );
            
            // Отправляем запрос на сервер
            try (Socket socket = new Socket("localhost", 5555);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                if (response instanceof UpdateRoomResponse) {
                    UpdateRoomResponse updateResponse = (UpdateRoomResponse) response;
                    if (updateResponse.isSuccess()) {
                        // Если это была новая комната, обновляем roomToEdit для возможности загрузки изображений
                        if (isNewRoom && updateResponse.getUpdatedRoom() != null) {
                            roomToEdit = updateResponse.getUpdatedRoom();
                            isNewRoom = false;
                        }
                        closeDialog();
                    } else {
                        showError("Ошибка: " + updateResponse.getErrorMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка подключения к серверу: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        String number = numberField.getText().trim();
        String type = typeComboBox.getValue();
        String status = statusComboBox.getValue();
        String priceText = priceField.getText().trim();
        
        if (number.isEmpty()) {
            showError("Номер комнаты не может быть пустым");
            return false;
        }
        
        if (floorComboBox.getValue() == null) {
            showError("Выберите этаж");
            return false;
        }
        
        if (type == null) {
            showError("Выберите тип номера");
            return false;
        }
        
        if (status == null) {
            showError("Выберите статус номера");
            return false;
        }
        
        if (priceText.isEmpty()) {
            showError("Укажите цену");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Цена должна быть больше нуля");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректный формат цены");
            return false;
        }
        
        hideError();
        return true;
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
