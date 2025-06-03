package com.hotel.client.ui;

import com.hotel.client.controller.AdminEditRoomController;
import com.hotel.common.RoomDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Класс для создания и настройки меню админ-панели
 */
public class AdminMenu {
    
    /**
     * Создает и настраивает меню для админ-панели
     */
    public static MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        
        // Меню "Управление"
        Menu managementMenu = new Menu("Управление");
        
        MenuItem roomsMenuItem = new MenuItem("Номерной фонд");
        roomsMenuItem.setOnAction(e -> openRoomsManagement(primaryStage));
        
        MenuItem bookingsMenuItem = new MenuItem("Бронирования");
        bookingsMenuItem.setOnAction(e -> openBookingsManagement(primaryStage));
        
        MenuItem usersMenuItem = new MenuItem("Пользователи");
        usersMenuItem.setOnAction(e -> openUsersManagement(primaryStage));
        
        MenuItem partnersMenuItem = new MenuItem("Партнеры");
        partnersMenuItem.setOnAction(e -> openPartnersManagement(primaryStage));
        
        managementMenu.getItems().addAll(roomsMenuItem, bookingsMenuItem, usersMenuItem, partnersMenuItem);
        
        // Меню "Отчеты"
        Menu reportsMenu = new Menu("Отчеты");
        
        MenuItem occupancyMenuItem = new MenuItem("Заполняемость");
        occupancyMenuItem.setOnAction(e -> openOccupancyReport(primaryStage));
        
        MenuItem revenueMenuItem = new MenuItem("Доходы");
        revenueMenuItem.setOnAction(e -> openRevenueReport(primaryStage));
        
        MenuItem reviewsMenuItem = new MenuItem("Отзывы");
        reviewsMenuItem.setOnAction(e -> openReviewsManagement(primaryStage));
        
        reportsMenu.getItems().addAll(occupancyMenuItem, revenueMenuItem, reviewsMenuItem);
        
        // Меню "Настройки"
        Menu settingsMenu = new Menu("Настройки");
        
        MenuItem profileMenuItem = new MenuItem("Профиль");
        profileMenuItem.setOnAction(e -> openProfileSettings(primaryStage));
        
        MenuItem systemMenuItem = new MenuItem("Система");
        systemMenuItem.setOnAction(e -> openSystemSettings(primaryStage));
        
        settingsMenu.getItems().addAll(profileMenuItem, systemMenuItem);
        
        // Добавляем все меню в меню-бар
        menuBar.getMenus().addAll(managementMenu, reportsMenu, settingsMenu);
        
        return menuBar;
    }
    
    // Методы для открытия различных окон управления
    
    private static void openRoomsManagement(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/AdminRoomsList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление номерами");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void openBookingsManagement(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/AdminBookingsList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление бронированиями");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void openUsersManagement(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/AdminUsersList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление пользователями");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void openPartnersManagement(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/PartnerList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление партнерами");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void openOccupancyReport(Stage primaryStage) {
        // TODO: Реализовать открытие отчета по заполняемости
    }
    
    private static void openRevenueReport(Stage primaryStage) {
        // TODO: Реализовать открытие отчета по доходам
    }
    
    private static void openReviewsManagement(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/AdminReviewsList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление отзывами");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void openProfileSettings(Stage primaryStage) {
        // TODO: Реализовать открытие настроек профиля
    }
    
    private static void openSystemSettings(Stage primaryStage) {
        // TODO: Реализовать открытие системных настроек
    }
    
    /**
     * Открывает окно редактирования комнаты
     */
    public static void openRoomEditor(Stage primaryStage, RoomDTO room) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminMenu.class.getResource("/fxml/AdminEditRoom.fxml"));
            Parent root = loader.load();
            
            AdminEditRoomController controller = loader.getController();
            if (room != null) {
                controller.setRoom(room);
            }
            
            Stage stage = new Stage();
            stage.setTitle(room == null ? "Создание номера" : "Редактирование номера");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 