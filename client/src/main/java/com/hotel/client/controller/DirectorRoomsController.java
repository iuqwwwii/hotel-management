package com.hotel.client.controller;

import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class DirectorRoomsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roomTypeFilter;
    @FXML private ComboBox<String> statusFilter;
    
    @FXML private TableView<RoomDTO> roomsTable;
    @FXML private TableColumn<RoomDTO, Integer> idColumn;
    @FXML private TableColumn<RoomDTO, String> numberColumn;
    @FXML private TableColumn<RoomDTO, String> typeColumn;
    @FXML private TableColumn<RoomDTO, Integer> capacityColumn;
    @FXML private TableColumn<RoomDTO, Double> priceColumn;
    @FXML private TableColumn<RoomDTO, String> statusColumn;
    @FXML private TableColumn<RoomDTO, Double> occupancyColumn;
    
    @FXML private PieChart roomTypeChart;
    @FXML private LineChart<String, Number> occupancyChart;
    
    private List<RoomDTO> allRooms = new ArrayList<>();
    private ObservableList<RoomDTO> filteredRooms = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Настраиваем таблицу
        setupTable();
        
        // Загружаем фильтры
        setupFilters();
        
        // Загружаем данные
        loadData();
        
        // Настраиваем поиск
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRooms();
        });
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        
        // В RoomDTO нет поля capacity, поэтому временно используем заглушку
        capacityColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RoomDTO, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<RoomDTO, Integer> param) {
                // Возвращаем 2 или 4 в зависимости от типа номера
                String type = param.getValue().getTypeName();
                int capacity = type.contains("FAMILY") ? 4 : 2;
                return new SimpleIntegerProperty(capacity).asObject();
            }
        });
        
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Для occupancyColumn нужно вычислять значение
        occupancyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RoomDTO, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<RoomDTO, Double> param) {
                // Здесь можно было бы вычислять загруженность на основе бронирований
                // Но для примера просто возвращаем случайное значение от 0 до 100%
                double occupancy = Math.random() * 100;
                // Возвращаем ObservableValue<Double>
                return new SimpleDoubleProperty(occupancy).asObject();
            }
        });
        
        // Форматирование ячеек
        priceColumn.setCellFactory(column -> new TableCell<RoomDTO, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f BYN", price));
                }
            }
        });
        
        occupancyColumn.setCellFactory(column -> new TableCell<RoomDTO, Double>() {
            @Override
            protected void updateItem(Double occupancy, boolean empty) {
                super.updateItem(occupancy, empty);
                if (empty || occupancy == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", occupancy));
                }
            }
        });
        
        // Стилизация статусов
        statusColumn.setCellFactory(column -> new TableCell<RoomDTO, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "AVAILABLE":
                            setStyle("-fx-text-fill: #4caf50;"); // Зеленый
                            break;
                        case "OCCUPIED":
                            setStyle("-fx-text-fill: #f44336;"); // Красный
                            break;
                        case "MAINTENANCE":
                            setStyle("-fx-text-fill: #ff9800;"); // Оранжевый
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
    }
    
    private void setupFilters() {
        // Типы номеров
        roomTypeFilter.getItems().addAll("Все", "Стандарт", "Люкс", "Полулюкс", "Семейный", "Бизнес");
        roomTypeFilter.setValue("Все");
        
        // Статусы
        statusFilter.getItems().addAll("Все", "Доступен", "Занят", "На обслуживании");
        statusFilter.setValue("Все");
    }
    
    private void loadData() {
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
                    filteredRooms.setAll(allRooms);
                    roomsTable.setItems(filteredRooms);
                    
                    // Обновляем графики
                    updateCharts();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить данные о номерах: " + e.getMessage());
        }
    }
    
    private void filterRooms() {
        String searchText = searchField.getText().toLowerCase();
        String roomType = roomTypeFilter.getValue();
        String status = statusFilter.getValue();
        
        List<RoomDTO> filtered = allRooms.stream()
                .filter(room -> {
                    // Поиск по тексту
                    boolean matchesSearch = searchText.isEmpty() || 
                            room.getNumber().toLowerCase().contains(searchText) ||
                            room.getTypeName().toLowerCase().contains(searchText);
                    
                    // Фильтр по типу
                    boolean matchesType = "Все".equals(roomType) || 
                            mapRoomType(room.getTypeName()).equals(roomType);
                    
                    // Фильтр по статусу
                    boolean matchesStatus = "Все".equals(status) || 
                            mapStatus(room.getStatus()).equals(status);
                    
                    return matchesSearch && matchesType && matchesStatus;
                })
                .collect(Collectors.toList());
        
        filteredRooms.setAll(filtered);
    }
    
    private String mapRoomType(String type) {
        // Преобразование технических названий типов в понятные для пользователя
        switch (type.toUpperCase()) {
            case "STANDARD": return "Стандарт";
            case "DELUXE": return "Люкс";
            case "SEMI_DELUXE": return "Полулюкс";
            case "FAMILY": return "Семейный";
            case "BUSINESS": return "Бизнес";
            default: return type;
        }
    }
    
    private String mapStatus(String status) {
        // Преобразование технических названий статусов в понятные для пользователя
        switch (status.toUpperCase()) {
            case "AVAILABLE": return "Доступен";
            case "OCCUPIED": return "Занят";
            case "MAINTENANCE": return "На обслуживании";
            default: return status;
        }
    }
    
    private void updateCharts() {
        updateRoomTypeChart();
        updateOccupancyChart();
    }
    
    private void updateRoomTypeChart() {
        // Группировка номеров по типам
        Map<String, Long> roomsByType = allRooms.stream()
                .collect(Collectors.groupingBy(
                        room -> mapRoomType(room.getTypeName()),
                        Collectors.counting()
                ));
        
        // Создаем данные для диаграммы
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        roomsByType.forEach((type, count) -> {
            pieChartData.add(new PieChart.Data(type + " (" + count + ")", count));
        });
        
        roomTypeChart.setData(pieChartData);
    }
    
    private void updateOccupancyChart() {
        // Очищаем предыдущие данные
        occupancyChart.getData().clear();
        
        // В реальном приложении здесь нужно было бы получать данные о загрузке по месяцам
        // Для примера генерируем случайные данные
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Загрузка номеров");
        
        // Добавляем данные для каждого месяца
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, new Locale("ru"));
            double occupancy = 40 + Math.random() * 50; // Случайное значение от 40% до 90%
            series.getData().add(new XYChart.Data<>(monthName, occupancy));
        }
        
        occupancyChart.getData().add(series);
    }
    
    @FXML
    public void onApplyFilters() {
        filterRooms();
    }
    
    @FXML
    public void onResetFilters() {
        searchField.clear();
        roomTypeFilter.setValue("Все");
        statusFilter.setValue("Все");
        filterRooms();
    }
    
    @FXML
    public void onRefresh() {
        loadData();
    }
    
    @FXML
    public void onExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт данных");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("rooms_export.csv");
        
        File file = fileChooser.showSaveDialog(roomsTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Заголовок CSV
                writer.write("ID,Номер,Тип,Цена за ночь,Статус\n");
                
                // Данные
                for (RoomDTO room : filteredRooms) {
                    writer.write(String.format("%d,%s,%s,%.2f,%s\n",
                            room.getRoomId(),
                            room.getNumber(),
                            room.getTypeName(),
                            room.getBasePrice(),
                            room.getStatus()));
                }
                
                showAlert("Экспорт успешен", "Данные успешно экспортированы в " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Ошибка", "Не удалось экспортировать данные: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 