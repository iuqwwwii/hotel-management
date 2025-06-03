package com.hotel.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.hotel.client.controller.GuestController;
import com.hotel.client.util.WindowState;

import java.util.ResourceBundle;

public class ClientApplication extends Application {
    private static Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        ClientApplication.primaryStage = primaryStage;
        
        // Сначала восстанавливаем состояние окна из файла
        WindowState.restoreState(primaryStage);
        
        // Добавляем обработчик закрытия приложения
        Platform.setImplicitExit(true);
        Platform.runLater(() -> {
            primaryStage.setOnCloseRequest(event -> {
                WindowState.saveState(primaryStage);
            });
        });
        
        // Затем показываем форму авторизации
        showAuthView(primaryStage);
    }
    
    @Override
    public void stop() {
        // Сохраняем состояние окна при завершении приложения
        if (primaryStage != null) {
            WindowState.saveState(primaryStage);
        }
    }

    public static void showAuthView(Stage stage) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/AuthView.fxml"),
                bundle
        );
        Scene scene = new Scene(loader.load(), 1000, 700);
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/registration.css").toExternalForm()
        );
        stage.setTitle(bundle.getString("login.title"));
        stage.setScene(scene);
        
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Если окно уже было открыто ранее, восстанавливаем его состояние
        // иначе устанавливаем начальные размеры и центрируем
        if (stage.getUserData() != null && stage.getUserData().equals("initialized")) {
            WindowState.restoreState(stage);
        } else {
            stage.setUserData("initialized");
        }
        
        stage.show();
    }

    public static void showAdminView(Stage stage) throws Exception {
        // Сохраняем текущее состояние окна
        WindowState.saveState(stage);
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/AdminDashboard.fxml"),
                bundle
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/admin.css").toExternalForm()
        );
        stage.setTitle(bundle.getString("title.admin"));
        stage.setScene(scene);
        
        // Восстанавливаем состояние окна
        WindowState.restoreState(stage);
        
        stage.show();
    }

    public static void showEmployeeView(Stage stage) throws Exception {
        // Сохраняем текущее состояние окна
        WindowState.saveState(stage);
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/DirectorDashboard.fxml"),
                bundle
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/director.css").toExternalForm()
        );
        stage.setTitle(bundle.getString("title.employee"));
        stage.setScene(scene);
        
        // Восстанавливаем состояние окна
        WindowState.restoreState(stage);
        
        stage.show();
    }

    public static void showGuestView(Stage stage) throws Exception {
        // Сохраняем текущее состояние окна
        WindowState.saveState(stage);
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/GuestDashboard.fxml"),
                bundle
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/guest.css").toExternalForm()
        );
        stage.setTitle(bundle.getString("title.guest"));
        stage.setScene(scene);
        
        // Восстанавливаем состояние окна
        WindowState.restoreState(stage);
        
        stage.show();
    }

    public static void showGuestView(Stage stage, int userId) throws Exception {
        // Сохраняем текущее состояние окна
        WindowState.saveState(stage);
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/GuestDashboard.fxml"),
                bundle
        );
        Parent root = loader.load();
        
        // Получаем контроллер и устанавливаем ID пользователя
        GuestController controller = loader.getController();
        controller.setCurrentUserId(userId);
        
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/guest.css").toExternalForm()
        );
        stage.setTitle(bundle.getString("title.guest"));
        stage.setScene(scene);
        
        // Восстанавливаем состояние окна
        WindowState.restoreState(stage);
        
        stage.show();
    }

    public static void showDirectorView(Stage stage) throws Exception {
        // Сохраняем текущее состояние окна
        WindowState.saveState(stage);
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource("/fxml/DirectorDashboard.fxml"),
                bundle
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        
        // Используем специальный CSS для директора
        scene.getStylesheets().add(
                ClientApplication.class.getResource("/css/director.css").toExternalForm()
        );
        
        stage.setTitle("Панель директора");
        stage.setScene(scene);
        
        // Восстанавливаем состояние окна
        WindowState.restoreState(stage);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

