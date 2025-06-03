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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class DirectorBookingsController {

    @FXML private TableView<BookingDTO> bookingsTable;
    @FXML private TableColumn<BookingDTO, Integer> idColumn;
    @FXML private TableColumn<BookingDTO, Integer> userColumn;
    @FXML private TableColumn<BookingDTO, Integer> roomColumn;
    @FXML private TableColumn<BookingDTO, String> datesColumn;
    @FXML private TableColumn<BookingDTO, Double> costColumn;
    @FXML private TableColumn<BookingDTO, String> statusColumn;
    @FXML private TableColumn<BookingDTO, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> periodFilterCombo;
    
    @FXML private Label totalBookingsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label checkedInLabel;
    @FXML private Label cancelledLabel;
    
    @FXML private BarChart<String, Number> monthlyChart;
    
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
    
    // Периоды для фильтрации
    private final Map<String, Integer> PERIODS = Map.of(
        "За все время", 0,
        "За последний месяц", 1,
        "За последние 3 месяца", 3,
        "За последние 6 месяцев", 6,
        "За последний год", 12
    );

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
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
    
    private void setupFilters() {
        // Добавляем "Все" и статусы
        ObservableList<String> statusItems = FXCollections.observableArrayList();
        statusItems.add("Все");
        statusItems.addAll(STATUS_DISPLAY);
        statusFilterCombo.setItems(statusItems);
        statusFilterCombo.getSelectionModel().selectFirst();
        
        // Периоды
        ObservableList<String> periodItems = FXCollections.observableArrayList(PERIODS.keySet());
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
    
    @FXML
    public void onExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("bookings_report.csv");
        
        File file = fileChooser.showSaveDialog(bookingsTable.getScene().getWindow());
        if (file != null) {
            exportBookingsToCsv(file);
        }
    }
    
    private void exportBookingsToCsv(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Заголовок CSV
            writer.println("ID,Пользователь,Номер,Дата заезда,Дата выезда,Стоимость,Статус");
            
            // Данные
            for (BookingDTO booking : bookingsList) {
                String status = STATUSES.indexOf(booking.getStatus()) >= 0 ? 
                        STATUS_DISPLAY.get(STATUSES.indexOf(booking.getStatus())) : 
                        booking.getStatus();
                        
                writer.println(String.format("%d,%d,%d,%s,%s,%.2f,%s",
                    booking.getBookingId(),
                    booking.getUserId(),
                    booking.getRoomId(),
                    booking.getStartDate().format(dateFormatter),
                    booking.getEndDate().format(dateFormatter),
                    booking.getTotalCost(),
                    status
                ));
            }
            
            showAlert(Alert.AlertType.INFORMATION, 
                "Экспорт", 
                "Экспорт успешно завершен", 
                "Данные сохранены в файл: " + file.getAbsolutePath());
                
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                "Ошибка", 
                "Ошибка при экспорте", 
                e.getMessage());
        }
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
                    
                    // Применяем фильтры
                    final List<BookingDTO> filteredBookings = applyFilters(allBookings);
                    
                    // Обновляем UI в потоке JavaFX
                    Platform.runLater(() -> {
                        updateBookingsList(filteredBookings);
                        updateStatistics(filteredBookings);
                        updateChart(filteredBookings);
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
    
    private List<BookingDTO> applyFilters(List<BookingDTO> bookings) {
        List<BookingDTO> result = new ArrayList<>(bookings);
        
        // Фильтр по статусу
        String selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus != null && !"Все".equals(selectedStatus)) {
            int statusIndex = STATUS_DISPLAY.indexOf(selectedStatus);
            if (statusIndex >= 0) {
                String statusCode = STATUSES.get(statusIndex);
                result = result.stream()
                    .filter(b -> statusCode.equals(b.getStatus()))
                    .collect(Collectors.toList());
            }
        }
        
        // Фильтр по периоду
        String selectedPeriod = periodFilterCombo.getValue();
        if (selectedPeriod != null && PERIODS.containsKey(selectedPeriod)) {
            int months = PERIODS.get(selectedPeriod);
            if (months > 0) {
                LocalDate cutoffDate = LocalDate.now().minusMonths(months);
                result = result.stream()
                    .filter(b -> b.getStartDate().isAfter(cutoffDate) || b.getStartDate().isEqual(cutoffDate))
                    .collect(Collectors.toList());
            }
        }
        
        return result;
    }
    
    private void updateBookingsList(List<BookingDTO> bookings) {
        bookingsList.clear();
        bookingsList.addAll(bookings);
        totalBookingsLabel.setText(String.valueOf(bookings.size()));
    }
    
    private void updateStatistics(List<BookingDTO> bookings) {
        // Активные бронирования (NEW + CONFIRMED)
        long activeCount = bookings.stream()
                .filter(b -> "NEW".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus()))
                .count();
        activeBookingsLabel.setText(String.valueOf(activeCount));
        
        // Заселенные
        long checkedInCount = bookings.stream()
                .filter(b -> "CHECKED_IN".equals(b.getStatus()))
                .count();
        checkedInLabel.setText(String.valueOf(checkedInCount));
        
        // Отмененные
        long cancelledCount = bookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .count();
        cancelledLabel.setText(String.valueOf(cancelledCount));
    }
    
    private void updateChart(List<BookingDTO> bookings) {
        // Очищаем предыдущие данные
        monthlyChart.getData().clear();
        
        // Группируем бронирования по месяцам
        Map<Month, Integer> bookingsByMonth = new HashMap<>();
        
        // Инициализируем все месяцы
        for (Month month : Month.values()) {
            bookingsByMonth.put(month, 0);
        }
        
        // Считаем бронирования по месяцам
        for (BookingDTO booking : bookings) {
            Month month = booking.getStartDate().getMonth();
            bookingsByMonth.put(month, bookingsByMonth.get(month) + 1);
        }
        
        // Создаем серию данных для графика
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Добавляем данные в серию (сортируем месяцы по порядку)
        Arrays.stream(Month.values())
              .forEach(month -> {
                  String monthName = month.getDisplayName(TextStyle.SHORT, new Locale("ru"));
                  series.getData().add(new XYChart.Data<>(monthName, bookingsByMonth.get(month)));
              });
        
        // Добавляем серию на график
        monthlyChart.getData().add(series);
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
                    private final Button viewButton = new Button("Детали");
                    private final HBox buttonsBox = new HBox(5, confirmButton, cancelButton, checkInButton, checkOutButton, viewButton);

                    {
                        // Стилизуем кнопки
                        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 10px;");
                        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                        checkInButton.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-size: 10px;");
                        checkOutButton.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-font-size: 10px;");
                        viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 10px;");
                        
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
                                // Отображаем только подходящие кнопки в зависимости от статуса
                                switch (booking.getStatus()) {
                                    case "NEW":
                                        // Для новых бронирований: подтвердить или отменить
                                        confirmButton.setVisible(true);
                                        cancelButton.setVisible(true);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(false);
                                        viewButton.setVisible(true);
                                        break;
                                    case "CONFIRMED":
                                        // Для подтвержденных: заселить или отменить
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(true);
                                        checkInButton.setVisible(true);
                                        checkOutButton.setVisible(false);
                                        viewButton.setVisible(true);
                                        break;
                                    case "CHECKED_IN":
                                        // Для заселенных: только выселить
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(false);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(true);
                                        viewButton.setVisible(true);
                                        break;
                                    case "CHECKED_OUT":
                                    case "CANCELLED":
                                        // Для выселенных и отмененных: только просмотр
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(false);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(false);
                                        viewButton.setVisible(true);
                                        break;
                                    default:
                                        // Для остальных статусов только просмотр
                                        confirmButton.setVisible(false);
                                        cancelButton.setVisible(false);
                                        checkInButton.setVisible(false);
                                        checkOutButton.setVisible(false);
                                        viewButton.setVisible(true);
                                        break;
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
    
    private void showBookingDetails(BookingDTO booking) {
        // Здесь можно реализовать отображение подробной информации о бронировании
        // Например, открыть новое окно с деталями
        showAlert(Alert.AlertType.INFORMATION, 
                "Детали бронирования", 
                "Бронирование #" + booking.getBookingId(), 
                "Пользователь: " + booking.getUserId() + "\n" +
                "Номер: " + booking.getRoomId() + "\n" +
                "Даты: " + booking.getStartDate().format(dateFormatter) + " - " + 
                         booking.getEndDate().format(dateFormatter) + "\n" +
                "Стоимость: " + String.format("%.2f BYN", booking.getTotalCost()) + "\n" +
                "Статус: " + STATUS_DISPLAY.get(STATUSES.indexOf(booking.getStatus())));
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 