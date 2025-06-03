package com.hotel.client.controller;

import com.hotel.common.GetAllBookingsRequest;
import com.hotel.common.GetAllBookingsResponse;
import com.hotel.common.BookingDTO;
import com.hotel.common.ReviewDTO;
import com.hotel.common.RoomDTO;
import com.hotel.common.UserDTO;
import com.hotel.common.FetchReviewsRequest;
import com.hotel.common.FetchReviewsResponse;
import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.FetchUsersRequest;
import com.hotel.common.FetchUsersResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminReportsController {
    
    @FXML private ComboBox<String> periodComboBox;
    @FXML private Label totalBookingsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label cancelledBookingsLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private PieChart bookingStatusChart;
    @FXML private BarChart<String, Number> occupancyChart;
    @FXML private BarChart<String, Number> roomRatingChart;
    @FXML private Label statusLabel;
    
    private List<BookingDTO> allBookings;
    private List<RoomDTO> allRooms;
    private List<ReviewDTO> allReviews;
    private List<UserDTO> allUsers;
    
    @FXML
    public void initialize() {
        // Инициализация периодов для фильтра
        periodComboBox.setItems(FXCollections.observableArrayList(
            "Последние 7 дней",
            "Последние 30 дней",
            "Текущий месяц",
            "Текущий год",
            "Все время"
        ));
        periodComboBox.getSelectionModel().selectLast(); // По умолчанию "Все время"
        
        // Загрузка данных
        loadData();
    }
    
    @FXML
    public void onRefresh() {
        loadData();
    }
    
    private void loadData() {
        showStatus("Загрузка данных...");
        
        // Загружаем все необходимые данные параллельно
        Thread bookingsThread = new Thread(this::loadBookings);
        Thread roomsThread = new Thread(this::loadRooms);
        Thread reviewsThread = new Thread(this::loadReviews);
        Thread usersThread = new Thread(this::loadUsers);
        
        bookingsThread.start();
        roomsThread.start();
        reviewsThread.start();
        usersThread.start();
        
        // Ждем завершения всех потоков
        new Thread(() -> {
            try {
                bookingsThread.join();
                roomsThread.join();
                reviewsThread.join();
                usersThread.join();
                
                // Обновляем UI в потоке JavaFX
                Platform.runLater(() -> {
                    updateDashboard();
                    hideStatus();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> 
                    showError("Ошибка при загрузке данных: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void loadBookings() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            out.writeObject(new GetAllBookingsRequest());
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof GetAllBookingsResponse) {
                GetAllBookingsResponse bookingsResponse = (GetAllBookingsResponse) response;
                allBookings = bookingsResponse.getBookings();
                System.out.println("Loaded " + (allBookings != null ? allBookings.size() : 0) + " bookings");
            } else {
                System.out.println("Unexpected response: " + (response != null ? response.getClass().getSimpleName() : "null"));
                allBookings = new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> showError("Ошибка загрузки бронирований: " + e.getMessage()));
            allBookings = new ArrayList<>();
        }
    }
    
    private void loadRooms() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            out.writeObject(new FetchRoomsRequest());
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof FetchRoomsResponse) {
                FetchRoomsResponse roomsResponse = (FetchRoomsResponse) response;
                allRooms = roomsResponse.getRooms();
                System.out.println("Loaded " + (allRooms != null ? allRooms.size() : 0) + " rooms");
            } else {
                System.out.println("Unexpected response: " + (response != null ? response.getClass().getSimpleName() : "null"));
                allRooms = new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading rooms: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> showError("Ошибка загрузки номеров: " + e.getMessage()));
            allRooms = new ArrayList<>();
        }
    }
    
    private void loadReviews() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            out.writeObject(FetchReviewsRequest.fetchAllReviews());
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof FetchReviewsResponse) {
                FetchReviewsResponse reviewsResponse = (FetchReviewsResponse) response;
                allReviews = reviewsResponse.getReviews();
                System.out.println("Loaded " + (allReviews != null ? allReviews.size() : 0) + " reviews");
            } else {
                System.out.println("Unexpected response: " + (response != null ? response.getClass().getSimpleName() : "null"));
                allReviews = new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> showError("Ошибка загрузки отзывов: " + e.getMessage()));
            allReviews = new ArrayList<>();
        }
    }
    
    private void loadUsers() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            out.writeObject(new FetchUsersRequest());
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof FetchUsersResponse) {
                FetchUsersResponse usersResponse = (FetchUsersResponse) response;
                allUsers = usersResponse.getUsers();
                System.out.println("Loaded " + (allUsers != null ? allUsers.size() : 0) + " users");
            } else {
                System.out.println("Unexpected response: " + (response != null ? response.getClass().getSimpleName() : "null"));
                allUsers = new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> showError("Ошибка загрузки пользователей: " + e.getMessage()));
            allUsers = new ArrayList<>();
        }
    }
    
    private void updateDashboard() {
        // Проверяем, что все необходимые списки инициализированы
        if (allBookings == null) allBookings = new ArrayList<>();
        if (allRooms == null) allRooms = new ArrayList<>();
        if (allReviews == null) allReviews = new ArrayList<>();
        if (allUsers == null) allUsers = new ArrayList<>();
        
        // Фильтрация данных по выбранному периоду
        LocalDate startDate = getStartDateForPeriod();
        List<BookingDTO> filteredBookings = filterBookingsByDate(allBookings, startDate);
        
        // Обновление статистики
        updateStatistics(filteredBookings);
        
        // Обновление графиков
        updateBookingStatusChart(filteredBookings);
        updateOccupancyChart(filteredBookings);
        updateRoomRatingChart();
        
        // Скрываем сообщение о статусе загрузки
        hideStatus();
    }
    
    private LocalDate getStartDateForPeriod() {
        String selectedPeriod = periodComboBox.getValue();
        LocalDate now = LocalDate.now();
        
        if (selectedPeriod == null || "Все время".equals(selectedPeriod)) {
            return LocalDate.of(2000, 1, 1); // Достаточно давняя дата
        }
        
        switch (selectedPeriod) {
            case "Последние 7 дней":
                return now.minusDays(7);
            case "Последние 30 дней":
                return now.minusDays(30);
            case "Текущий месяц":
                return now.withDayOfMonth(1);
            case "Текущий год":
                return now.withDayOfYear(1);
            default:
                return LocalDate.of(2000, 1, 1);
        }
    }
    
    private List<BookingDTO> filterBookingsByDate(List<BookingDTO> bookings, LocalDate startDate) {
        return bookings.stream()
                .filter(b -> !b.getStartDate().isBefore(startDate))
                .collect(Collectors.toList());
    }
    
    private void updateStatistics(List<BookingDTO> bookings) {
        // Общее количество бронирований
        totalBookingsLabel.setText(String.valueOf(bookings.size()));
        
        // Активные бронирования (NEW, CONFIRMED, CHECKED_IN)
        long activeCount = bookings.stream()
                .filter(b -> "NEW".equals(b.getStatus()) || 
                           "CONFIRMED".equals(b.getStatus()) ||
                           "CHECKED_IN".equals(b.getStatus()))
                .count();
        activeBookingsLabel.setText(String.valueOf(activeCount));
        
        // Отмененные бронирования
        long cancelledCount = bookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .count();
        cancelledBookingsLabel.setText(String.valueOf(cancelledCount));
        
        // Общее количество номеров
        totalRoomsLabel.setText(String.valueOf(allRooms.size()));
        
        // Занятые номера (имеют активные бронирования на сегодня)
        LocalDate today = LocalDate.now();
        long occupiedCount = allRooms.stream()
                .filter(room -> bookings.stream()
                        .anyMatch(b -> b.getRoomId() == room.getRoomId() && 
                                      !b.getStartDate().isAfter(today) && 
                                      !b.getEndDate().isBefore(today) && 
                                      ("NEW".equals(b.getStatus()) || 
                                       "CONFIRMED".equals(b.getStatus()) ||
                                       "CHECKED_IN".equals(b.getStatus()))))
                .count();
        occupiedRoomsLabel.setText(String.valueOf(occupiedCount));
        
        // Свободные номера
        availableRoomsLabel.setText(String.valueOf(allRooms.size() - occupiedCount));
    }
    
    private void updateBookingStatusChart(List<BookingDTO> bookings) {
        // Очищаем старые данные
        bookingStatusChart.setData(FXCollections.observableArrayList());
        
        // Если список пуст, показываем заглушку
        if (bookings == null || bookings.isEmpty()) {
            bookingStatusChart.setTitle("Нет данных о бронированиях");
            return;
        }
        
        // Подсчет бронирований по статусам
        Map<String, Integer> statusCounts = new HashMap<>();
        
        for (BookingDTO booking : bookings) {
            String status = booking.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        
        // Создание данных для графика
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            String displayName = getStatusDisplayName(entry.getKey());
            pieChartData.add(new PieChart.Data(displayName + " (" + entry.getValue() + ")", entry.getValue()));
        }
        
        // Обновление графика
        bookingStatusChart.setData(pieChartData);
        bookingStatusChart.setTitle("Распределение бронирований по статусам");
    }
    
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "NEW": return "Новые";
            case "CONFIRMED": return "Подтвержденные";
            case "CHECKED_IN": return "Заселенные";
            case "CHECKED_OUT": return "Выселенные";
            case "CANCELLED": return "Отмененные";
            default: return status;
        }
    }
    
    private void updateOccupancyChart(List<BookingDTO> bookings) {
        // Очищаем старые данные
        occupancyChart.getData().clear();
        
        // Если список пуст, показываем заглушку
        if (bookings == null || bookings.isEmpty()) {
            occupancyChart.setTitle("Нет данных о бронированиях");
            return;
        }
        
        // Получаем данные о заселенности по дням
        Map<LocalDate, Integer> occupancyByDate = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(14); // Показываем на 2 недели вперед
        
        // Инициализируем карту для всех дат
        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            occupancyByDate.put(date, 0);
        }
        
        // Подсчитываем количество занятых номеров на каждую дату
        for (BookingDTO booking : bookings) {
            if ("CANCELLED".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
                continue; // Пропускаем отмененные и завершенные бронирования
            }
            
            LocalDate start = booking.getStartDate();
            LocalDate end = booking.getEndDate();
            
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                if (!date.isBefore(today) && !date.isAfter(endDate)) {
                    occupancyByDate.put(date, occupancyByDate.getOrDefault(date, 0) + 1);
                }
            }
        }
        
        // Создаем серию данных для графика
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Занятые номера");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        
        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.format(formatter);
            series.getData().add(new XYChart.Data<>(dateStr, occupancyByDate.get(date)));
        }
        
        // Обновляем график
        occupancyChart.getData().add(series);
        occupancyChart.setTitle("Прогноз загруженности отеля на ближайшие 2 недели");
    }
    
    private void updateRoomRatingChart() {
        // Очищаем старые данные
        roomRatingChart.getData().clear();
        
        // Если список отзывов пуст, показываем заглушку
        if (allReviews == null || allReviews.isEmpty() || allRooms == null || allRooms.isEmpty()) {
            roomRatingChart.setTitle("Нет данных об отзывах");
            return;
        }
        
        // Создаем карту для хранения рейтингов по номерам
        Map<Integer, List<Integer>> ratingsByRoom = new HashMap<>();
        
        // Собираем все рейтинги для каждого номера
        for (ReviewDTO review : allReviews) {
            int roomId = review.getRoomId();
            int rating = review.getRating();
            
            if (!ratingsByRoom.containsKey(roomId)) {
                ratingsByRoom.put(roomId, new ArrayList<>());
            }
            ratingsByRoom.get(roomId).add(rating);
        }
        
        // Если нет отзывов с рейтингами, показываем заглушку
        if (ratingsByRoom.isEmpty()) {
            roomRatingChart.setTitle("Нет отзывов с рейтингами");
            return;
        }
        
        // Вычисляем средний рейтинг для каждого номера
        Map<String, Double> averageRatingByRoom = new HashMap<>();
        
        for (Map.Entry<Integer, List<Integer>> entry : ratingsByRoom.entrySet()) {
            int roomId = entry.getKey();
            List<Integer> ratings = entry.getValue();
            
            // Находим информацию о номере
            String roomNumber = allRooms.stream()
                    .filter(r -> r.getRoomId() == roomId)
                    .map(RoomDTO::getNumber)
                    .findFirst()
                    .orElse("Номер " + roomId);
            
            // Вычисляем средний рейтинг
            double averageRating = ratings.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);
            
            averageRatingByRoom.put(roomNumber, averageRating);
        }
        
        // Создаем серию данных для графика
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Средний рейтинг");
        
        // Сортируем номера по рейтингу (от высшего к низшему)
        List<Map.Entry<String, Double>> sortedEntries = averageRatingByRoom.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());
        
        // Добавляем данные в серию
        for (Map.Entry<String, Double> entry : sortedEntries) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        // Обновляем график
        roomRatingChart.getData().add(series);
        roomRatingChart.setTitle("Рейтинг номеров по отзывам гостей");
    }
    
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2196f3;");
        statusLabel.setVisible(true);
    }
    
    private void showError(String message) {
        statusLabel.setText("Ошибка: " + message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        statusLabel.setVisible(true);
    }
    
    private void hideStatus() {
        statusLabel.setVisible(false);
    }
} 