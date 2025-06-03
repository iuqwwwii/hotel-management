package com.hotel.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.hotel.client.ClientApplication;

import java.util.ResourceBundle;

public class EmployeeController {
    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPlaceholder;

    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML public void initialize() {
        welcomeLabel.setText(bundle.getString("employee.welcome"));
    }

    private void loadCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxml),
                    bundle
            );
            Node view = loader.load();
            rootPane.setCenter(view);
        } catch (Exception e) {
            contentPlaceholder.getChildren().setAll(new Label("Ошибка загрузки " + fxml));
            e.printStackTrace();
        }
    }

    // TODO: Требуется создать FXML-файл для этой функциональности
    @FXML public void onProcessBookings() { loadCenter("EmployeeProcessBookings.fxml"); }
    
    // TODO: Требуется создать FXML-файл для этой функциональности
    @FXML public void onManageServices()  { loadCenter("EmployeeManageServices.fxml"); }
    
    // TODO: Требуется создать FXML-файл для этой функциональности
    @FXML public void onProfile()  { loadCenter("EmployeeProfile.fxml"); }
    
    @FXML
    public void onLogout() {
        try {
            Stage st = (Stage) rootPane.getScene().getWindow();
            ClientApplication.showAuthView(st);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
