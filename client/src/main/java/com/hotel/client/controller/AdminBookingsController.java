package com.hotel.client.controller;

import com.hotel.common.BookingDTO;
import com.hotel.common.GetAllBookingsRequest;
import com.hotel.common.GetAllBookingsResponse;
import com.hotel.common.UpdateBookingStatusRequest;
import com.hotel.common.UpdateBookingStatusResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminBookingsController {

    @FXML private TableView<BookingDTO> bookingsTable;
    @FXML private TableColumn<BookingDTO, Integer> idColumn;
    @FXML private TableColumn<BookingDTO, Integer> userColumn;
    @FXML private TableColumn<BookingDTO, Integer> roomColumn;
    @FXML private TableColumn<BookingDTO, String> datesColumn;
    @FXML private TableColumn<BookingDTO, Double> costColumn;
    @FXML private TableColumn<BookingDTO, String> statusColumn;
    @FXML private TableColumn<BookingDTO, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label totalBookingsLabel;
    
    private final ObservableList<BookingDTO> bookingsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    // Возможные статусы бронирования
    private final List<String> STATUSES = Arrays.asList(
        "NEW", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"
    );
    
    // Отображаемые названия статусов
    private final List<String> STATUS_DISPLAY = Arrays.asList(
        "Новое", "Подтверждено", "Заселен", "Выселен", "Отменено"
    );

    @FXML
    public void initialize() {
        setupTable();
        setupStatusFilter();
        loadBookings();
    }
    
    private void setupTable() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
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
        
        bookingsTable.setItems(bookingsList);
    }
    
    private void setupStatusFilter() {
        // Добавляем "Все" и статусы
        ObservableList<String> filterItems = FXCollections.observableArrayList();
        filterItems.add("Все");
        filterItems.addAll(STATUS_DISPLAY);
        statusFilterCombo.setItems(filterItems);
        statusFilterCombo.getSelectionModel().selectFirst();
        
        // Обработчик изменения фильтра
        statusFilterCombo.getSelectionModel().selectedItemProperty().addListener(
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
                
                // Отправляем запрос на получение всех бронирований
                GetAllBookingsRequest request = new GetAllBookingsRequest();
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                if (response instanceof GetAllBookingsResponse) {
                    GetAllBookingsResponse bookingsResponse = (GetAllBookingsResponse) response;
                    final List<BookingDTO> allBookings = bookingsResponse.getBookings();
                    
                    // Фильтруем бронирования по статусу, если выбран конкретный статус
                    final String selectedFilter = statusFilterCombo.getValue();
                    final List<BookingDTO> filteredBookings;
                    
                    if (selectedFilter != null && !"Все".equals(selectedFilter)) {
                        int statusIndex = STATUS_DISPLAY.indexOf(selectedFilter);
                        if (statusIndex >= 0) {
                            String statusCode = STATUSES.get(statusIndex);
                            filteredBookings = allBookings.stream()
                                .filter(b -> statusCode.equals(b.getStatus()))
                                .collect(Collectors.toList());
                        } else {
                            filteredBookings = allBookings;
                        }
                    } else {
                        filteredBookings = allBookings;
                    }
                    
                    // Обновляем UI в потоке JavaFX
                    Platform.runLater(() -> {
                        bookingsList.clear();
                        bookingsList.addAll(filteredBookings);
                        totalBookingsLabel.setText("Всего бронирований: " + filteredBookings.size());
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
                    private final Button confirmButton = new Button("Подтвердить");
                    private final Button cancelButton = new Button("Отменить");
                    private final Button checkInButton = new Button("Заселить");
                    private final Button checkOutButton = new Button("Выселить");
                    private final HBox buttonsBox = new HBox(5, confirmButton, cancelButton, checkInButton, checkOutButton);

                    {
                        // Стилизуем кнопки
                        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
                        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        checkInButton.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white;");
                        checkOutButton.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white;");
                        
                        // Настраиваем обработчики кнопок
                        confirmButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                updateBookingStatus(booking, "CONFIRMED");
                            }
                        });
                        
                        cancelButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                updateBookingStatus(booking, "CANCELLED");
                            }
                        });
                        
                        checkInButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                updateBookingStatus(booking, "CHECKED_IN");
                            }
                        });
                        
                        checkOutButton.setOnAction(event -> {
                            BookingDTO booking = getTableRow().getItem();
                            if (booking != null) {
                                updateBookingStatus(booking, "CHECKED_OUT");
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
                                
                                // Показываем только релевантные кнопки в зависимости от статуса
                                confirmButton.setVisible("NEW".equals(status));
                                confirmButton.setManaged("NEW".equals(status));
                                
                                cancelButton.setVisible(!"CANCELLED".equals(status) && !"CHECKED_OUT".equals(status));
                                cancelButton.setManaged(!"CANCELLED".equals(status) && !"CHECKED_OUT".equals(status));
                                
                                checkInButton.setVisible("CONFIRMED".equals(status));
                                checkInButton.setManaged("CONFIRMED".equals(status));
                                
                                checkOutButton.setVisible("CHECKED_IN".equals(status));
                                checkOutButton.setManaged("CHECKED_IN".equals(status));
                                
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
    
    private void updateBookingStatus(BookingDTO booking, String newStatus) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на обновление статуса бронирования
                UpdateBookingStatusRequest request = new UpdateBookingStatusRequest(
                        booking.getBookingId(), newStatus);
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                if (response instanceof UpdateBookingStatusResponse) {
                    UpdateBookingStatusResponse statusResponse = (UpdateBookingStatusResponse) response;
                    
                    Platform.runLater(() -> {
                        if (statusResponse.isSuccess()) {
                            // Обновляем статус в списке и перезагружаем данные
                            loadBookings();
                        } else {
                            showAlert(Alert.AlertType.ERROR, 
                                    "Ошибка", 
                                    "Не удалось обновить статус", 
                                    statusResponse.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, 
                            "Ошибка", 
                            "Не удалось обновить статус бронирования", 
                            e.getMessage());
                });
            }
        }).start();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 