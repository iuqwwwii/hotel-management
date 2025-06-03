package com.hotel.client.controller;

import com.hotel.client.ClientApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class DirectorController {
    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPlaceholder;

    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML
    public void initialize() {
        welcomeLabel.setText("Добро пожаловать!");
        
        // По умолчанию загружаем панель обзора
        onDashboard();
    }

    private void loadCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxmlPath),
                    bundle
            );
            Node view = loader.load();
            contentPlaceholder.getChildren().setAll(view);
            
            // Если загружаем представление отзывов, настраиваем его как для администратора
            if (fxmlPath.equals("ReviewsView.fxml")) {
                ReviewsViewController controller = loader.getController();
                controller.setAdmin(true); // Устанавливаем права администратора для директора
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentPlaceholder.getChildren().setAll(new Label("Ошибка загрузки " + fxmlPath));
        }
    }

    @FXML
    public void onDashboard() {
        loadCenter("DirectorDashboardHome.fxml");
    }

    @FXML
    public void onViewBookings() {
        loadCenter("DirectorBookings.fxml");
    }

    @FXML
    public void onViewRooms() {
        loadCenter("DirectorRooms.fxml");
    }

    @FXML
    public void onManagePartners() {
        loadCenter("PartnerList.fxml");
    }

    @FXML
    public void onViewReviews() {
        loadCenter("ReviewsView.fxml");
    }

    @FXML
    public void onLogout() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            ClientApplication.showAuthView(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 