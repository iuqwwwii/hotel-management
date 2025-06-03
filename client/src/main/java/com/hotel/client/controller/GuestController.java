package com.hotel.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.hotel.client.ClientApplication;
import com.hotel.client.controller.ReviewsViewController;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class GuestController {
    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPlaceholder;
    @FXML private VBox roomsContainer;

    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    
    /**
     * Устанавливает ID текущего пользователя
     * @param userId ID пользователя
     */
    public void setCurrentUserId(int userId) {
        System.out.println("GuestController: Setting user ID to " + userId);
        this.currentUserId = userId;
    }
    
    /**
     * Возвращает ID текущего пользователя
     * @return ID пользователя
     */
    public int getCurrentUserId() {
        return currentUserId;
    }

    @FXML public void initialize() {
        welcomeLabel.setText(bundle.getString("guest.welcome"));
        loadCenter("GuestViewRoomsCards.fxml");
    }

    public void loadCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxml),
                    ResourceBundle.getBundle("i18n.Bundle_ru")
            );
            Parent view = loader.load();
            
            // Передаем ID пользователя в загруженный контроллер
            Object controller = loader.getController();
            
            // Проверяем разные типы контроллеров, которые могут принимать ID пользователя
            if (controller instanceof ReviewsViewController) {
                ((ReviewsViewController) controller).setCurrentUserId(currentUserId);
                System.out.println("GuestController: Передаем ID=" + currentUserId + " в ReviewsViewController");
            } else if (controller instanceof GuestBookingsController) {
                // Если это контроллер бронирований
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("setCurrentUserId", int.class);
                    method.invoke(controller, currentUserId);
                    System.out.println("GuestController: Передаем ID=" + currentUserId + " в " + controller.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("Не удалось передать ID пользователя в " + controller.getClass().getSimpleName());
                }
            }
            
            // Сохраняем ссылку на этот контроллер в сцене для обратного вызова
            Scene scene = rootPane.getScene();
            if (scene != null) {
                scene.setUserData(this);
            }
            
            rootPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void onViewRooms() { loadCenter("GuestViewRoomsCards.fxml"); }
    @FXML public void onViewBookings() { loadCenter("GuestBookings.fxml"); }
    @FXML public void onAddReview()    { loadCenter("ReviewsView.fxml"); }

    @FXML
    public void onLogout() {
        try {
            Stage st = (Stage) rootPane.getScene().getWindow();
            ClientApplication.showAuthView(st);
        } catch (Exception e) { e.printStackTrace(); }
    }

//    private void loadRooms() {
//        // 1) Запросите у сервера список доступных комнат:
//        // List<RoomDTO> rooms = ServerApi.getAvailableRooms();
//        List<RoomDTO> rooms = List.of(
//                new RoomDTO(101, "Одноместный", BigDecimal.valueOf(50), "/images/room101_1.jpg"),
//                /* ... */
//        );

//        roomsContainer.getChildren().clear();
//        for (RoomDTO dto : rooms) {
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass()
//                        .getResource("/fxml/RoomCard.fxml"), bundle);
//                Node card = loader.load();
//                RoomCardController ctrl = loader.getController();
//                ctrl.setData(dto, this::openBookingForm);
//                roomsContainer.getChildren().add(card);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // Вызывается при клике "Забронировать" в карточке
//    private void openBookingForm(RoomDTO room) {
//        try {
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/fxml/GuestBookingForm.fxml"), bundle);
//            Parent form = loader.load();
//            GuestBookingController ctrl = loader.getController();
//            ctrl.prefillRoom(room);
//            rootPane.setCenter(form);
//        } catch (IOException e) { e.printStackTrace(); }
//    }
}


