package com.hotel.client.controller;

import com.hotel.common.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import com.hotel.client.ClientApplication;
import com.hotel.client.controller.ReviewsViewController;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class AdminController {
    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPlaceholder;

    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML
    public void initialize() {
        welcomeLabel.setText(bundle.getString("admin.welcome"));
    }

    public void loadCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxml),
                    ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent view = loader.load();
            rootPane.setCenter(view);
            
            // Если загружаем представление отзывов, настраиваем его как для администратора
            if (fxml.equals("ReviewsView.fxml")) {
                ReviewsViewController controller = loader.getController();
                controller.setAdmin(true); // Устанавливаем права администратора
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentPlaceholder.getChildren().setAll(new Label("Ошибка загрузки: " + e.getMessage()));
        }
    }

    @FXML public void onManageUsers()     { loadCenter("AdminManageUsers.fxml"); }
    @FXML public void onManageRooms()     { loadCenter("AdminManageRooms.fxml"); }
    @FXML public void onProcessBookings() { loadCenter("AdminProcessBookings.fxml"); }
    @FXML public void onViewReviews()     { loadCenter("ReviewsView.fxml"); }
    @FXML public void onViewReports()     { loadCenter("AdminReports.fxml"); }
    @FXML public void onViewLogs()        { loadCenter("AdminLogs.fxml"); }
    
    @FXML 
    public void onManagePartners() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PartnerList.fxml"),
                    ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent view = loader.load();
            rootPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось загрузить форму управления партнерами: " + e.getMessage());
        }
    }
    
    @FXML
    public void onExportData() {
        try {
            // Загружаем форму для экспорта данных
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/DataExport.fxml"),
                    ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent view = loader.load();
            rootPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось загрузить форму экспорта данных: " + e.getMessage());
        }
    }
    
    @FXML
    public void onCreateTestReviews() {
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Создаем 15 тестовых отзывов
            CreateTestReviewsRequest request = new CreateTestReviewsRequest(15);
            
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof CreateTestReviewsResponse) {
                CreateTestReviewsResponse resp = (CreateTestReviewsResponse) response;
                
                if (resp.isSuccess()) {
                    showInfo("Тестовые отзывы созданы: " + resp.getCount());
                    // Если открыт экран отзывов, обновляем его
                    if (rootPane.getCenter() instanceof Parent) {
                        Parent center = (Parent) rootPane.getCenter();
                        Node node = center.lookup("#reviewsContainer");
                        if (node != null && node.getParent() != null) {
                            // Находим контроллер
                            Scene scene = node.getScene();
                            if (scene != null) {
                                // Перезагружаем отзывы
                                loadCenter("ReviewsView.fxml");
                            }
                        }
                    }
                } else {
                    showError("Не удалось создать тестовые отзывы: " + resp.getMessage());
                }
            } else {
                showError("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при создании тестовых отзывов: " + e.getMessage());
        }
    }

    @FXML
    public void onLogout() {
        // просто возвращаемся на форму логина
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            ClientApplication.showAuthView(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}


