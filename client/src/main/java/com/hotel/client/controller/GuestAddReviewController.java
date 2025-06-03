package com.hotel.client.controller;

import com.hotel.common.GetGuestBookingsRequest;
import com.hotel.common.GetGuestBookingsResponse;
import com.hotel.common.BookingDTO;
import com.hotel.common.CreateReviewRequest;
import com.hotel.common.CreateReviewResponse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GuestAddReviewController {
    
    @FXML private ComboBox<BookingDisplayItem> bookingIdComboBox;
    @FXML private RadioButton rating1;
    @FXML private RadioButton rating2;
    @FXML private RadioButton rating3;
    @FXML private RadioButton rating4;
    @FXML private RadioButton rating5;
    @FXML private ToggleGroup ratingGroup;
    @FXML private TextField titleField;
    @FXML private TextArea reviewTextArea;
    @FXML private Label statusLabel;
    
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    private List<BookingDTO> userBookings = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Проверяем, что ToggleGroup инициализирован из FXML
        if (ratingGroup == null) {
            System.out.println("Создаем новую группу переключателей, так как она не была загружена из FXML");
            ratingGroup = new ToggleGroup();
            rating1.setToggleGroup(ratingGroup);
            rating2.setToggleGroup(ratingGroup);
            rating3.setToggleGroup(ratingGroup);
            rating4.setToggleGroup(ratingGroup);
            rating5.setToggleGroup(ratingGroup);
        }
        
        // Устанавливаем рейтинг по умолчанию
        rating5.setSelected(true);
        
        // Загрузка бронирований пользователя
        loadUserBookings();
        
        // Настройка отображения статуса
        statusLabel.setVisible(false);
    }
    
    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        System.out.println("GuestAddReviewController: Setting user ID to " + userId + " (previous: " + this.currentUserId + ")");
        this.currentUserId = userId;
        // Перезагружаем бронирования при изменении ID пользователя
        loadUserBookings();
    }
    
    private void loadUserBookings() {
        System.out.println("GuestAddReviewController: Loading bookings for user ID: " + currentUserId);
        
        if (currentUserId <= 0) {
            showError("Не выполнен вход в систему. Пожалуйста, войдите в систему для добавления отзыва.");
            return;
        }
        
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            // Запрос всех бронирований пользователя без фильтра статуса
            GetGuestBookingsRequest request = new GetGuestBookingsRequest(currentUserId);
            System.out.println("GuestAddReviewController: Отправляем запрос GetGuestBookingsRequest для пользователя ID=" + currentUserId);
            // Устанавливаем фильтр на статус CHECKED_OUT - только завершенные бронирования
            request.setStatusFilter("CHECKED_OUT");
            
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            System.out.println("GuestAddReviewController: Получен ответ: " + 
                             (response != null ? response.getClass().getSimpleName() : "null"));
            
            if (response instanceof GetGuestBookingsResponse) {
                GetGuestBookingsResponse bookingsResponse = (GetGuestBookingsResponse) response;
                List<BookingDTO> allBookings = bookingsResponse.getBookings();
                
                System.out.println("GuestAddReviewController: Получено " + allBookings.size() + 
                                 " бронирований со статусом CHECKED_OUT для пользователя ID=" + currentUserId);
                
                // Проверка на пустой список бронирований
                if (allBookings.isEmpty()) {
                    System.out.println("GuestAddReviewController: У пользователя ID=" + currentUserId + 
                                     " нет завершенных бронирований.");
                    
                    // Очищаем выпадающий список
                    bookingIdComboBox.setItems(FXCollections.observableArrayList());
                    
                    // Показываем сообщение, если нет завершенных бронирований
                    showError("У вас нет завершенных бронирований. Отзыв можно оставить только после проживания в отеле.");
                    return;
                }
                
                // Фильтрация бронирований (двойная проверка для надежности)
                userBookings = new ArrayList<>();
                for (BookingDTO booking : allBookings) {
                    System.out.println("GuestAddReviewController: Проверка бронирования - " +
                                     "ID=" + booking.getBookingId() + 
                                     ", UserID=" + booking.getUserId() + 
                                     ", RoomID=" + booking.getRoomId() +
                                     ", Room=" + booking.getRoomNumber() + 
                                     ", Status=" + booking.getStatus());
                    
                    // Проверяем и userId и статус
                    if (booking.getUserId() == currentUserId && "CHECKED_OUT".equals(booking.getStatus())) {
                        userBookings.add(booking);
                    } else {
                        System.out.println("GuestAddReviewController: Бронирование пропущено - не соответствует критериям");
                    }
                }
                
                System.out.println("GuestAddReviewController: После фильтрации осталось " + 
                                 userBookings.size() + " бронирований");
                
                // Заполняем выпадающий список
                List<BookingDisplayItem> displayItems = new ArrayList<>();
                for (BookingDTO booking : userBookings) {
                    displayItems.add(new BookingDisplayItem(booking));
                }
                
                bookingIdComboBox.setItems(FXCollections.observableArrayList(displayItems));
                
                if (!displayItems.isEmpty()) {
                    bookingIdComboBox.getSelectionModel().selectFirst();
                } else {
                    // Показываем сообщение, если нет завершенных бронирований
                    showError("У вас нет завершенных бронирований. Отзыв можно оставить только после проживания в отеле.");
                }
            } else {
                System.out.println("GuestAddReviewController: Получен неожиданный ответ: " + 
                                 (response != null ? response.getClass().getSimpleName() : "null"));
                showError("Ошибка: получен неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            System.err.println("GuestAddReviewController: Ошибка при загрузке бронирований: " + e.getMessage());
            e.printStackTrace();
            showError("Ошибка при загрузке бронирований: " + e.getMessage());
        }
    }
    
    @FXML
    private void onSubmitReview() {
        // Проверка ID пользователя
        if (currentUserId <= 0) {
            showError("Ошибка: не удалось определить ID пользователя. Пожалуйста, повторно войдите в систему.");
            System.err.println("GuestAddReviewController: Ошибка - неверный ID пользователя: " + currentUserId);
            return;
        }
        
        // Проверка заполнения полей
        if (bookingIdComboBox.getValue() == null) {
            showError("Выберите номер бронирования");
            return;
        }
        
        if (reviewTextArea.getText().trim().isEmpty()) {
            showError("Введите текст отзыва");
            return;
        }
        
        // Получение выбранного рейтинга
        Toggle selectedToggle = ratingGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showError("Выберите оценку от 1 до 5");
            return;
        }
        
        RadioButton selectedRating = (RadioButton) selectedToggle;
        System.out.println("GuestAddReviewController: Выбранный рейтинг: " + selectedRating.getText());
        int rating = Integer.parseInt(selectedRating.getText());
        
        // Получение выбранного бронирования
        BookingDisplayItem selectedBookingItem = bookingIdComboBox.getValue();
        BookingDTO selectedBooking = selectedBookingItem.getBooking();
        
        // Проверка, принадлежит ли бронирование текущему пользователю
        if (selectedBooking.getUserId() != currentUserId) {
            String errorMsg = "Ошибка: выбранное бронирование не принадлежит текущему пользователю. " +
                           "BookingUserId=" + selectedBooking.getUserId() + ", CurrentUserId=" + currentUserId;
            System.err.println("GuestAddReviewController: " + errorMsg);
            showError("Ошибка: выбрано бронирование другого пользователя. Обратитесь к администратору.");
            return;
        }
        
        // Формирование текста отзыва (с заголовком, если он указан)
        String comment = reviewTextArea.getText().trim();
        if (!titleField.getText().trim().isEmpty()) {
            comment = titleField.getText().trim() + "\n\n" + comment;
        }
        
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            // Создание запроса на добавление отзыва
            CreateReviewRequest request = new CreateReviewRequest(
                    currentUserId,
                    selectedBooking.getRoomId(),
                    selectedBooking.getBookingId(),
                    rating,
                    comment
            );
            
            System.out.println("GuestAddReviewController: Отправка запроса на создание отзыва: " + 
                             "UserID=" + currentUserId + 
                             ", RoomID=" + selectedBooking.getRoomId() + 
                             ", BookingID=" + selectedBooking.getBookingId() + 
                             ", Rating=" + rating);
            
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            System.out.println("GuestAddReviewController: Получен ответ типа: " + 
                             (response != null ? response.getClass().getSimpleName() : "null"));
            
            if (response instanceof CreateReviewResponse) {
                CreateReviewResponse reviewResponse = (CreateReviewResponse) response;
                
                if (reviewResponse.isSuccess()) {
                    showSuccess("Отзыв успешно добавлен");
                    System.out.println("GuestAddReviewController: Отзыв успешно добавлен");
                    
                    // Закрываем окно через 2 секунды
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            javafx.application.Platform.runLater(() -> {
                                ((Stage) statusLabel.getScene().getWindow()).close();
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    String errorMsg = "Ошибка: " + reviewResponse.getMessage();
                    System.err.println("GuestAddReviewController: " + errorMsg);
                    showError(errorMsg);
                }
            } else {
                String errorMsg = "Неожиданный ответ от сервера: " + 
                               (response != null ? response.getClass().getSimpleName() : "null");
                System.err.println("GuestAddReviewController: " + errorMsg);
                showError(errorMsg);
            }
            
        } catch (Exception e) {
            String errorMsg = "Ошибка при отправке отзыва: " + e.getMessage();
            System.err.println("GuestAddReviewController: " + errorMsg);
            e.printStackTrace();
            showError(errorMsg);
        }
    }
    
    @FXML
    private void onCancel() {
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        statusLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2ecc71;");
        statusLabel.setVisible(true);
    }
    
    // Вспомогательный класс для отображения бронирований в ComboBox
    public static class BookingDisplayItem {
        private final BookingDTO booking;
        
        public BookingDisplayItem(BookingDTO booking) {
            this.booking = booking;
        }
        
        public BookingDTO getBooking() {
            return booking;
        }
        
        @Override
        public String toString() {
            return String.format("№%d: %s (комната %s)", 
                    booking.getBookingId(), 
                    booking.getStartDate() + " - " + booking.getEndDate(),
                    booking.getRoomNumber());
        }
    }
} 