package com.hotel.client.controller;

import com.hotel.common.CreateBookingRequest;
import com.hotel.common.CreateBookingResponse;
import com.hotel.common.RoomDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class GuestBookingController {
    
    // Поля формы для информации о комнате
    @FXML private Label lblRoomNumber;
    @FXML private Label lblRoomType;
    @FXML private Label lblRoomPrice;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label lblTotalCost;
    @FXML private TextArea notesField;
    @FXML private Label errorLabel;
    
    // Новые поля для формы оплаты
    @FXML private Tab paymentTab;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label paymentAmountLabel;
    @FXML private VBox cardDetailsSection;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardExpiryField;
    @FXML private PasswordField cardCvvField;
    @FXML private TextField cardHolderField;
    @FXML private CheckBox savePaymentMethodCheckbox;
    
    private RoomDTO room;
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    private double totalCost = 0.0;
    
    @FXML
    public void initialize() {
        // Устанавливаем текущую дату как минимальную
        LocalDate today = LocalDate.now();
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });
        
        // Дата выезда должна быть после даты заезда
        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate start = startDatePicker.getValue();
                setDisable(empty || date.isBefore(today) || 
                        (start != null && date.isBefore(start)));
            }
        });
        
        // Обновляем доступные даты выезда при изменении даты заезда
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Если дата заезда позже даты выезда или дата выезда не выбрана
                LocalDate endDate = endDatePicker.getValue();
                if (endDate == null || endDate.isBefore(newVal)) {
                    endDatePicker.setValue(newVal.plusDays(1));
                }
                updateTotalCost();
            }
        });
        
        // Обновляем стоимость при изменении даты выезда
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalCost();
        });
        
        // Показываем/скрываем детали карты в зависимости от выбранного способа оплаты
        paymentMethodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCardPayment = "Банковская карта".equals(newVal);
            cardDetailsSection.setVisible(isCardPayment);
            cardDetailsSection.setManaged(isCardPayment);
        });
        
        // Устанавливаем начальные значения
        startDatePicker.setValue(today);
        endDatePicker.setValue(today.plusDays(1));
        paymentMethodCombo.getSelectionModel().select("Наличными при заселении");
    }
    
    public void setRoom(RoomDTO room, int userId) {
        this.room = room;
        this.currentUserId = userId;
        
        lblRoomNumber.setText(room.getNumber());
        lblRoomType.setText(room.getTypeName());
        lblRoomPrice.setText(String.format("%.2f BYN", room.getBasePrice()));
        
        updateTotalCost();
    }
    
    // Перегруженный метод для совместимости с существующим кодом
    public void setRoom(RoomDTO room) {
        // Используем текущий ID пользователя (1)
        setRoom(room, this.currentUserId);
    }
    
    private void updateTotalCost() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        if (start != null && end != null && room != null) {
            long nights = ChronoUnit.DAYS.between(start, end);
            if (nights > 0) {
                totalCost = nights * room.getBasePrice();
                String formattedCost = String.format("%.2f BYN (%d %s)", 
                        totalCost, nights, getDaysSuffix(nights));
                
                lblTotalCost.setText(formattedCost);
                paymentAmountLabel.setText(formattedCost);
            }
        }
    }
    
    private String getDaysSuffix(long days) {
        if (days % 10 == 1 && days % 100 != 11) {
            return "ночь";
        } else if ((days % 10 == 2 || days % 10 == 3 || days % 10 == 4) && 
                  (days % 100 < 10 || days % 100 > 20)) {
            return "ночи";
        } else {
            return "ночей";
        }
    }
    
    @FXML
    private void onCancel() {
        ((Stage) lblRoomNumber.getScene().getWindow()).close();
    }
    
    @FXML
    private void onBook() {
        errorLabel.setText("");
        
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        if (start == null || end == null) {
            errorLabel.setText("Выберите даты заезда и выезда");
            return;
        }
        
        if (start.isEqual(end) || start.isAfter(end)) {
            errorLabel.setText("Дата выезда должна быть после даты заезда");
            return;
        }
        
        // Проверяем способ оплаты
        String paymentMethod = paymentMethodCombo.getValue();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            errorLabel.setText("Выберите способ оплаты");
            paymentTab.getTabPane().getSelectionModel().select(paymentTab);
            return;
        }
        
        // Если выбрана оплата картой, проверяем заполнение данных карты
        if ("Банковская карта".equals(paymentMethod)) {
            if (!validateCardDetails()) {
                paymentTab.getTabPane().getSelectionModel().select(paymentTab);
                return;
            }
        }
        
        Socket sock = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        
        try {
            sock = new Socket("localhost", 5555);
            
            // Создаем выходной поток сначала
            out = new ObjectOutputStream(sock.getOutputStream());
            out.flush(); // Важно вызвать flush после создания ObjectOutputStream
            
            // Затем создаем входной поток
            in = new ObjectInputStream(sock.getInputStream());
            
            // Используем ID текущего пользователя вместо захардкоженного значения
            CreateBookingRequest request = new CreateBookingRequest(
                    currentUserId, // используем ID текущего пользователя
                    room.getRoomId(),
                    start,
                    end,
                    totalCost,
                    "NEW" // начальный статус
            );
            
            // Добавляем информацию о способе оплаты
            request.setPaymentMethod(convertPaymentMethodToCode(paymentMethod));
            
            // Добавляем примечания, если есть
            if (notesField.getText() != null && !notesField.getText().isEmpty()) {
                request.setNotes(notesField.getText());
            }
            
            // Добавляем данные карты, если нужно сохранить
            if (savePaymentMethodCheckbox.isSelected() && "Банковская карта".equals(paymentMethod)) {
                request.setCardNumber(cardNumberField.getText());
                request.setCardExpiry(cardExpiryField.getText());
                request.setCardCvv(cardCvvField.getText());
                request.setCardHolder(cardHolderField.getText());
            }
            
            // Отправляем запрос
            out.writeObject(request);
            out.flush();
            
            // Получаем ответ
            Object responseObj = in.readObject();
            
            if (responseObj instanceof CreateBookingResponse) {
                CreateBookingResponse response = (CreateBookingResponse) responseObj;
                
                if (response.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, 
                            "Успех", 
                            "Бронирование создано", 
                            "Ваше бронирование номера " + room.getNumber() + 
                            " успешно создано.\nСтатус бронирования: Новое");
                    ((Stage) lblRoomNumber.getScene().getWindow()).close();
                } else {
                    errorLabel.setText(response.getMessage());
                }
            } else {
                errorLabel.setText("Получен неверный тип ответа от сервера");
            }
        } catch (java.io.EOFException e) {
            errorLabel.setText("Ошибка при получении ответа от сервера");
            e.printStackTrace();
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Закрываем ресурсы в правильном порядке
            try { if (in != null) in.close(); } catch (Exception e) { /* игнорируем */ }
            try { if (out != null) out.close(); } catch (Exception e) { /* игнорируем */ }
            try { if (sock != null) sock.close(); } catch (Exception e) { /* игнорируем */ }
        }
    }
    
    private boolean validateCardDetails() {
        // Проверка номера карты
        String cardNumber = cardNumberField.getText();
        if (cardNumber == null || cardNumber.isEmpty() || !cardNumber.matches("\\d{16}")) {
            errorLabel.setText("Введите корректный номер карты (16 цифр)");
            return false;
        }
        
        // Проверка срока действия
        String expiry = cardExpiryField.getText();
        if (expiry == null || expiry.isEmpty() || !expiry.matches("\\d{2}/\\d{2}")) {
            errorLabel.setText("Введите срок действия в формате ММ/ГГ");
            return false;
        }
        
        // Проверка CVV
        String cvv = cardCvvField.getText();
        if (cvv == null || cvv.isEmpty() || !cvv.matches("\\d{3}")) {
            errorLabel.setText("Введите CVV-код (3 цифры)");
            return false;
        }
        
        // Проверка имени держателя
        String holder = cardHolderField.getText();
        if (holder == null || holder.isEmpty()) {
            errorLabel.setText("Введите имя держателя карты");
            return false;
        }
        
        return true;
    }
    
    private String convertPaymentMethodToCode(String displayMethod) {
        switch (displayMethod) {
            case "Наличными при заселении":
                return "CASH";
            case "Банковская карта":
                return "CARD";
            default:
                return "OTHER";
        }
    }
    
    private void showAlert(Alert.AlertType type, 
                          String title, 
                          String header, 
                          String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 