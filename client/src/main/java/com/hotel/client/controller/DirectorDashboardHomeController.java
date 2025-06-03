package com.hotel.client.controller;

import com.hotel.common.BookingDTO;
import com.hotel.common.GetAllBookingsRequest;
import com.hotel.common.GetAllBookingsResponse;
import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import com.hotel.common.UpdateBookingStatusRequest;
import com.hotel.common.UpdateBookingStatusResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DirectorDashboardHomeController {

    @FXML private Label availableRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label revenueLabel;
    
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
    
    @FXML private BarChart<String, Number> occupancyChart;
    @FXML private PieChart bookingStatusChart;
    @FXML private ListView<String> recentEventsListView;
    
    private final ObservableList<BookingDTO> bookingsList = FXCollections.observableArrayList();
    private List<RoomDTO> allRooms = new ArrayList<>();
    private List<BookingDTO> allBookings = new ArrayList<>();
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
        // Настраиваем таблицу бронирований
        setupBookingsTable();
        setupStatusFilter();
        
        // Загружаем данные при запуске
        loadData();
    }
    
    @FXML
    public void onRefresh() {
        loadData();
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
            (obs, oldVal, newVal) -> updateBookingsTable()
        );
    }
    
    private void setupBookingsTable() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        
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
                    setText(String.format("%.2f", cost));
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
                        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 10px;");
                        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                        checkInButton.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-size: 10px;");
                        checkOutButton.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-font-size: 10px;");
                        
                        buttonsBox.setStyle("-fx-padding: 5;");
                        
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
                                // Отображаем только подходящие кнопки в зависимости от статуса
                                switch (booking.getStatus()) {
                                    case "NEW":
                                        // Для новых бронирований: подтвердить или отменить
                                        confirmButton.setVisible(true);
                                        cancelButton.setVisible(true);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(false);
                                        break;
                                    case "CONFIRMED":
                                        // Для подтвержденных: заселить или отменить
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(true);
                                        checkInButton.setVisible(true);
                                        checkOutButton.setVisible(false);
                                        break;
                                    case "CHECKED_IN":
                                        // Для заселенных: только выселить
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(false);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(true);
                                        break;
                                    default:
                                        // Для остальных статусов кнопки не нужны
                                        setGraphic(null);
                                        return;
                                }
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
    
    private void loadData() {
        // Загружаем данные о номерах и бронированиях параллельно
        Thread roomsThread = new Thread(this::loadRooms);
        Thread bookingsThread = new Thread(this::loadBookings);
        
        roomsThread.start();
        bookingsThread.start();
        
        try {
            roomsThread.join();
            bookingsThread.join();
            
            // После загрузки всех данных обновляем интерфейс
            updateDashboard();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRooms() {
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject(new FetchRoomsRequest());
            out.flush();
            
            Object response = in.readObject();
            if (response instanceof FetchRoomsResponse) {
                FetchRoomsResponse roomsResponse = (FetchRoomsResponse) response;
                if (roomsResponse.isSuccess()) {
                    allRooms = roomsResponse.getRooms();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить данные о номерах"));
        }
    }
    
    private void loadBookings() {
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.writeObject(new GetAllBookingsRequest());
            out.flush();
            
            Object response = in.readObject();
            if (response instanceof GetAllBookingsResponse) {
                GetAllBookingsResponse bookingsResponse = (GetAllBookingsResponse) response;
                allBookings = bookingsResponse.getBookings();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить данные о бронированиях"));
        }
    }
    
    private void updateBookingsTable() {
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
        
        // Обновляем таблицу
        Platform.runLater(() -> {
            bookingsList.clear();
            bookingsList.addAll(filteredBookings);
            totalBookingsLabel.setText("Всего бронирований: " + filteredBookings.size());
        });
    }
    
    private void updateDashboard() {
        Platform.runLater(() -> {
            updateStatistics();
            updateBookingsTable();
            updateOccupancyChart();
            updateBookingStatusChart();
            updateRecentEvents();
        });
    }
    
    private void updateBookingStatus(BookingDTO booking, String newStatus) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на изменение статуса бронирования
                UpdateBookingStatusRequest request = new UpdateBookingStatusRequest(
                    booking.getBookingId(), newStatus);
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                if (response instanceof UpdateBookingStatusResponse) {
                    UpdateBookingStatusResponse statusResponse = (UpdateBookingStatusResponse) response;
                    
                    if (statusResponse.isSuccess()) {
                        // Обновляем данные после успешного изменения
                        Platform.runLater(() -> {
                            showAlert("Информация", "Статус бронирования успешно изменен на " + 
                                     STATUS_DISPLAY.get(STATUSES.indexOf(newStatus)));
                            loadData(); // Перезагружаем данные
                        });
                    } else {
                        Platform.runLater(() -> 
                            showAlert("Ошибка", "Не удалось изменить статус бронирования: " + 
                                     statusResponse.getMessage())
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> 
                    showAlert("Ошибка", "Ошибка соединения: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void updateStatistics() {
        // Количество доступных номеров
        long availableRooms = allRooms.stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .count();
        availableRoomsLabel.setText(String.valueOf(availableRooms));
        
        // Количество занятых номеров
        long occupiedRooms = allRooms.stream()
                .filter(r -> "OCCUPIED".equals(r.getStatus()))
                .count();
        occupiedRoomsLabel.setText(String.valueOf(occupiedRooms));
        
        // Количество активных бронирований
        long activeBookings = allBookings.stream()
                .filter(b -> "NEW".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus()))
                .count();
        activeBookingsLabel.setText(String.valueOf(activeBookings));
        
        // Выручка за текущий месяц
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        
        double revenue = allBookings.stream()
                .filter(b -> !b.getStatus().equals("CANCELLED"))
                .filter(b -> b.getStartDate().isAfter(startOfMonth) || b.getStartDate().isEqual(startOfMonth))
                .mapToDouble(BookingDTO::getTotalCost)
                .sum();
        
        revenueLabel.setText(String.format("%.2f BYN", revenue));
    }
    
    private void updateOccupancyChart() {
        // Очищаем предыдущие данные
        occupancyChart.getData().clear();
        
        // Группируем бронирования по месяцам
        Map<Month, Integer> bookingsByMonth = new HashMap<>();
        Map<Month, Integer> totalRoomsByMonth = new HashMap<>();
        
        // Инициализируем все месяцы
        for (Month month : Month.values()) {
            bookingsByMonth.put(month, 0);
            // Общее количество номеров * количество дней в месяце
            totalRoomsByMonth.put(month, allRooms.size() * month.length(false));
        }
        
        // Считаем бронирования по месяцам
        for (BookingDTO booking : allBookings) {
            if (!"CANCELLED".equals(booking.getStatus())) {
                Month month = booking.getStartDate().getMonth();
                bookingsByMonth.put(month, bookingsByMonth.get(month) + 1);
            }
        }
        
        // Создаем серию данных для графика
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Загрузка отеля");
        
        // Добавляем данные в серию (сортируем месяцы по порядку)
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, new Locale("ru"));
            
            // Рассчитываем процент загрузки
            double occupancyPercent = 0;
            if (totalRoomsByMonth.get(month) > 0) {
                occupancyPercent = (bookingsByMonth.get(month) * 100.0) / totalRoomsByMonth.get(month);
            }
            
            series.getData().add(new XYChart.Data<>(monthName, occupancyPercent));
        }
        
        // Добавляем серию на график
        occupancyChart.getData().add(series);
    }
    
    private void updateBookingStatusChart() {
        // Очищаем предыдущие данные
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Считаем количество бронирований по статусам
        Map<String, Long> bookingsByStatus = allBookings.stream()
                .collect(Collectors.groupingBy(BookingDTO::getStatus, Collectors.counting()));
        
        // Добавляем данные на диаграмму с понятными названиями
        if (bookingsByStatus.containsKey("NEW")) {
            pieChartData.add(new PieChart.Data("Новые", bookingsByStatus.get("NEW")));
        }
        
        if (bookingsByStatus.containsKey("CONFIRMED")) {
            pieChartData.add(new PieChart.Data("Подтверждённые", bookingsByStatus.get("CONFIRMED")));
        }
        
        if (bookingsByStatus.containsKey("CHECKED_IN")) {
            pieChartData.add(new PieChart.Data("Заселённые", bookingsByStatus.get("CHECKED_IN")));
        }
        
        if (bookingsByStatus.containsKey("CHECKED_OUT")) {
            pieChartData.add(new PieChart.Data("Выселенные", bookingsByStatus.get("CHECKED_OUT")));
        }
        
        if (bookingsByStatus.containsKey("CANCELLED")) {
            pieChartData.add(new PieChart.Data("Отменённые", bookingsByStatus.get("CANCELLED")));
        }
        
        bookingStatusChart.setData(pieChartData);
    }
    
    private void updateRecentEvents() {
        // Очищаем предыдущие данные
        ObservableList<String> events = FXCollections.observableArrayList();
        
        // Сортируем бронирования по дате (сначала новые)
        List<BookingDTO> sortedBookings = allBookings.stream()
                .sorted((b1, b2) -> b2.getStartDate().compareTo(b1.getStartDate()))
                .limit(10)  // Ограничиваем 10 последними событиями
                .collect(Collectors.toList());
        
        // Добавляем события
        for (BookingDTO booking : sortedBookings) {
            String eventText = "";
            String date = booking.getStartDate().format(dateFormatter);
            
            switch (booking.getStatus()) {
                case "NEW":
                    eventText = "Новое бронирование #" + booking.getBookingId() + " на " + date;
                    break;
                case "CONFIRMED":
                    eventText = "Подтверждено бронирование #" + booking.getBookingId() + " на " + date;
                    break;
                case "CHECKED_IN":
                    eventText = "Заселение по брони #" + booking.getBookingId() + " на " + date;
                    break;
                case "CHECKED_OUT":
                    eventText = "Выселение по брони #" + booking.getBookingId() + " на " + date;
                    break;
                case "CANCELLED":
                    eventText = "Отменено бронирование #" + booking.getBookingId() + " на " + date;
                    break;
            }
            
            events.add(eventText);
        }
        
        recentEventsListView.setItems(events);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 