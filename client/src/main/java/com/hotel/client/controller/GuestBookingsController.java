package com.hotel.client.controller;

import com.hotel.common.BookingDTO;
import com.hotel.common.GetGuestBookingsRequest;
import com.hotel.common.GetGuestBookingsResponse;
import com.hotel.common.CancelBookingRequest;
import com.hotel.common.CancelBookingResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class GuestBookingsController {

    @FXML private TableView<BookingDTO> bookingsTable;
    @FXML private TableColumn<BookingDTO, Integer> idColumn;
    @FXML private TableColumn<BookingDTO, Integer> roomColumn;
    @FXML private TableColumn<BookingDTO, String> datesColumn;
    @FXML private TableColumn<BookingDTO, Double> costColumn;
    @FXML private TableColumn<BookingDTO, String> statusColumn;
    @FXML private TableColumn<BookingDTO, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> periodFilterCombo;
    @FXML private Label totalBookingsLabel;
    
    private final ObservableList<BookingDTO> bookingsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    // Пользователь, для которого загружаются бронирования (должен быть установлен извне)
    private int currentUserId;
    
    // Возможные статусы бронирования
    private final List<String> STATUSES = Arrays.asList(
        "NEW", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"
    );
    
    // Отображаемые названия статусов
    private final List<String> STATUS_DISPLAY = Arrays.asList(
        "Новое", "Подтверждено", "Заселен", "Выселен", "Отменено"
    );
    
    // Периоды для фильтрации
    private final List<String> PERIODS = Arrays.asList(
        "За все время", 
        "За последний месяц",
        "За последние 3 месяца",
        "За последние 6 месяцев",
        "За последний год"
    );
    
    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        
        // Временно установим ID пользователя для тестирования
        // В реальной системе это должно быть получено через AuthService или подобный механизм
        this.currentUserId = 1;
        
        loadBookings();
    }
    
    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadBookings();
    }
    
    private void setupTable() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        
        // Форматируем даты
        datesColumn.setCellValueFactory(cellData -> {
            BookingDTO booking = cellData.getValue();
            String formattedDates = booking.getStartDate().format(dateFormatter) + 
                                    " - " + 
                                    booking.getEndDate().format(dateFormatter);
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedDates);
        });
        
        // Форматируем стоимость
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        costColumn.setCellFactory(column -> new TableCell<BookingDTO, Double>() {
            @Override
            protected void updateItem(Double cost, boolean empty) {
                super.updateItem(cost, empty);
                if (empty || cost == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f BYN", cost));
                }
            }
        });
        
        // Отображаем статус
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<BookingDTO, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int index = STATUSES.indexOf(status);
                    setText(index >= 0 ? STATUS_DISPLAY.get(index) : status);
                    
                    // Добавляем цветовое оформление для статусов
                    switch (status) {
                        case "NEW":
                            setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold;");
                            break;
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
                            break;
                        case "CHECKED_IN":
                            setStyle("-fx-text-fill: #7b1fa2; -fx-font-weight: bold;");
                            break;
                        case "CHECKED_OUT":
                            setStyle("-fx-text-fill: #0097a7; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Создаем кнопки действий
        actionsColumn.setCellFactory(createActionsColumnCallback());
        
        // Улучшаем читаемость таблицы
        bookingsTable.setRowFactory(tv -> {
            TableRow<BookingDTO> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent;");
            return row;
        });
        
        bookingsTable.setStyle("-fx-background-color: white; -fx-control-inner-background: white;");
        
        bookingsTable.setItems(bookingsList);
    }
    
    private void setupFilters() {
        // Добавляем "Все" и статусы для фильтра статусов
        ObservableList<String> statusItems = FXCollections.observableArrayList();
        statusItems.add("Все");
        statusItems.addAll(STATUS_DISPLAY);
        statusFilterCombo.setItems(statusItems);
        statusFilterCombo.getSelectionModel().selectFirst();
        
        // Добавляем периоды
        ObservableList<String> periodItems = FXCollections.observableArrayList(PERIODS);
        periodFilterCombo.setItems(periodItems);
        periodFilterCombo.getSelectionModel().selectFirst();
        
        // Обработчики изменения фильтров
        statusFilterCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadBookings()
        );
        
        periodFilterCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadBookings()
        );
    }
    
    @FXML
    public void onRefresh() {
        loadBookings();
    }
    
    private void loadBookings() {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на получение бронирований текущего пользователя
                GetGuestBookingsRequest request = new GetGuestBookingsRequest(currentUserId);
                
                // Добавляем фильтры в запрос
                String selectedStatus = statusFilterCombo.getValue();
                if (selectedStatus != null && !"Все".equals(selectedStatus)) {
                    int statusIndex = STATUS_DISPLAY.indexOf(selectedStatus);
                    if (statusIndex >= 0) {
                        request.setStatusFilter(STATUSES.get(statusIndex));
                    }
                }
                
                String selectedPeriod = periodFilterCombo.getValue();
                if (selectedPeriod != null && !PERIODS.get(0).equals(selectedPeriod)) {
                    int months = 0;
                    if (PERIODS.get(1).equals(selectedPeriod)) months = 1;
                    else if (PERIODS.get(2).equals(selectedPeriod)) months = 3;
                    else if (PERIODS.get(3).equals(selectedPeriod)) months = 6;
                    else if (PERIODS.get(4).equals(selectedPeriod)) months = 12;
                    
                    if (months > 0) {
                        request.setStartDateFilter(LocalDate.now().minusMonths(months));
                    }
                }
                
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                if (response instanceof GetGuestBookingsResponse) {
                    GetGuestBookingsResponse bookingsResponse = (GetGuestBookingsResponse) response;
                    final List<BookingDTO> filteredBookings = bookingsResponse.getBookings();
                    
                    // Обновляем UI в потоке JavaFX
                    Platform.runLater(() -> {
                        bookingsList.clear();
                        bookingsList.addAll(filteredBookings);
                        totalBookingsLabel.setText(String.valueOf(filteredBookings.size()));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, 
                            "Ошибка", 
                            "Не удалось загрузить бронирования", 
                            e.getMessage());
                });
            }
        }).start();
    }
    
    private Callback<TableColumn<BookingDTO, Void>, TableCell<BookingDTO, Void>> createActionsColumnCallback() {
        return new Callback<>() {
            @Override
            public TableCell<BookingDTO, Void> call(final TableColumn<BookingDTO, Void> param) {
                return new TableCell<>() {
                    private final Button cancelButton = new Button("Отменить");
                    private final Button viewButton = new Button("Детали");
                    private final HBox buttonsBox = new HBox(5, cancelButton, viewButton);

                    {
                        // Стилизуем кнопки
                        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
                        
                        // Настраиваем обработчики кнопок
                        cancelButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                Alert confirmation = new Alert(
                                    Alert.AlertType.CONFIRMATION,
                                    "Вы уверены, что хотите отменить бронирование?",
                                    ButtonType.YES, ButtonType.NO
                                );
                                confirmation.setTitle("Подтверждение");
                                confirmation.setHeaderText("Отмена бронирования #" + booking.getBookingId());
                                
                                confirmation.showAndWait().ifPresent(result -> {
                                    if (result == ButtonType.YES) {
                                        cancelBooking(booking);
                                    }
                                });
                            }
                        });
                        
                        viewButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                showBookingDetails(booking);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                String status = booking.getStatus();
                                
                                // Показываем кнопку отмены только для новых и подтвержденных бронирований
                                boolean canCancel = "NEW".equals(status) || "CONFIRMED".equals(status);
                                cancelButton.setVisible(canCancel);
                                cancelButton.setManaged(canCancel);
                                
                                setGraphic(buttonsBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }
    
    private void cancelBooking(BookingDTO booking) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на отмену бронирования
                CancelBookingRequest request = new CancelBookingRequest(
                    booking.getBookingId(), 
                    currentUserId
                );
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                if (response instanceof CancelBookingResponse) {
                    CancelBookingResponse cancelResponse = (CancelBookingResponse) response;
                    
                    Platform.runLater(() -> {
                        if (cancelResponse.isSuccess()) {
                            // Обновляем список и показываем уведомление
                            loadBookings();
                            showAlert(Alert.AlertType.INFORMATION, 
                                "Успех", 
                                "Бронирование отменено", 
                                "Бронирование номера #" + booking.getRoomId() + " было успешно отменено.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, 
                                "Ошибка", 
                                "Не удалось отменить бронирование", 
                                cancelResponse.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, 
                        "Ошибка", 
                        "Не удалось отменить бронирование", 
                        e.getMessage());
                });
            }
        }).start();
    }
    
    private void showBookingDetails(BookingDTO booking) {
        // Отображаем детальную информацию о бронировании
        int statusIndex = STATUSES.indexOf(booking.getStatus());
        String statusDisplay = statusIndex >= 0 ? STATUS_DISPLAY.get(statusIndex) : booking.getStatus();
        
        showAlert(Alert.AlertType.INFORMATION, 
            "Детали бронирования", 
            "Бронирование #" + booking.getBookingId(), 
            "Номер: " + booking.getRoomId() + "\n" +
            "Даты: " + booking.getStartDate().format(dateFormatter) + " - " + 
                      booking.getEndDate().format(dateFormatter) + "\n" +
            "Стоимость: " + String.format("%.2f BYN", booking.getTotalCost()) + "\n" +
            "Статус: " + statusDisplay);
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 