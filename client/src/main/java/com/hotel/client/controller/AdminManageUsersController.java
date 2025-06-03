package com.hotel.client.controller;

import com.hotel.common.UserDTO;
import com.hotel.common.FetchUsersRequest;
import com.hotel.common.DeleteUserRequest;
import com.hotel.common.DeleteUserResponse;
import com.hotel.common.FetchUsersResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminManageUsersController {
    
    @FXML private ComboBox<String> roleFilterComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Integer> colId;
    @FXML private TableColumn<UserDTO, String> colUsername;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colPhone;
    @FXML private TableColumn<UserDTO, String> colRole;
    @FXML private TableColumn<UserDTO, Void> colActions;
    @FXML private Label statusLabel;
    
    private List<UserDTO> allUsers = new ArrayList<>();
    private List<String> allRoles = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Настройка колонок таблицы
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        
        // Добавление кнопок действий
        addActionButtons();
        
        // Настройка поискового поля
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal != null && !oldVal.equals(newVal)) {
                applyFilters();
            }
        });
        
        // Инициализация фильтра ролей
        initRoleFilter();
        
        // Загрузка пользователей
        loadUsers();
    }
    
    private void initRoleFilter() {
        allRoles.add("ADMIN");
        allRoles.add("EMPLOYEE");
        allRoles.add("GUEST");
        
        // Создаем список для фильтра с опцией "Все роли"
        List<String> filterItems = new ArrayList<>();
        filterItems.add("Все роли");
        filterItems.addAll(allRoles);
        
        roleFilterComboBox.setItems(FXCollections.observableArrayList(filterItems));
        roleFilterComboBox.getSelectionModel().selectFirst();
    }
    
    private void addActionButtons() {
        Callback<TableColumn<UserDTO, Void>, TableCell<UserDTO, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<UserDTO, Void> call(final TableColumn<UserDTO, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Изменить");
                    private final Button deleteBtn = new Button("Удалить");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        editBtn.getStyleClass().add("btn-primary");
                        editBtn.setOnAction(event -> {
                            UserDTO user = getTableView().getItems().get(getIndex());
                            onEditUser(user);
                        });
                        
                        deleteBtn.getStyleClass().add("btn-danger");
                        deleteBtn.setOnAction(event -> {
                            UserDTO user = getTableView().getItems().get(getIndex());
                            onDeleteUser(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        
        colActions.setCellFactory(cellFactory);
    }
    
    private void loadUsers() {
        showStatus("Загрузка пользователей...");
        
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                System.out.println("Sending FetchUsersRequest to server...");
                
                // Создаем запрос на получение всех пользователей
                FetchUsersRequest request = new FetchUsersRequest();
                
                out.writeObject(request);
                out.flush();
                
                try {
                    Object response = in.readObject();
                    System.out.println("Received response: " + (response != null ? response.getClass().getSimpleName() : "null"));
                    
                    if (response instanceof FetchUsersResponse) {
                        FetchUsersResponse usersResponse = (FetchUsersResponse) response;
                        
                        if (usersResponse.isSuccess()) {
                            List<UserDTO> users = usersResponse.getUsers();
                            System.out.println("Received " + (users != null ? users.size() : 0) + " users");
                            
                            Platform.runLater(() -> {
                                if (users != null && !users.isEmpty()) {
                                    allUsers = users;
                                    applyFilters();
                                    hideStatus();
                                } else {
                                    showStatus("Пользователи не найдены");
                                }
                            });
                        } else {
                            Platform.runLater(() -> 
                                showError("Ошибка: " + usersResponse.getMessage())
                            );
                        }
                    } else {
                        Platform.runLater(() -> 
                            showError("Ошибка: неожиданный ответ от сервера")
                        );
                    }
                } catch (java.io.EOFException eof) {
                    System.out.println("EOFException: сервер закрыл соединение преждевременно");
                    Platform.runLater(() -> 
                        showError("Ошибка связи с сервером: сервер закрыл соединение")
                    );
                    eof.printStackTrace();
                }
                
            } catch (Exception e) {
                System.out.println("Exception loading users: " + e.getMessage());
                Platform.runLater(() -> 
                    showError("Ошибка при загрузке пользователей: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    public void onApplyFilter() {
        applyFilters();
    }
    
    @FXML
    public void onResetFilter() {
        searchField.clear();
        roleFilterComboBox.getSelectionModel().selectFirst();
        applyFilters();
    }
    
    private void applyFilters() {
        // Получаем значения фильтров
        String searchText = searchField.getText().toLowerCase();
        String selectedRole = roleFilterComboBox.getValue();
        
        // Создаем отфильтрованный список
        List<UserDTO> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // Фильтр по поисковому тексту
                    boolean matchesSearch = true;
                    if (searchText != null && !searchText.isEmpty()) {
                        matchesSearch = (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText)) ||
                                       (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText)) ||
                                       (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));
                    }
                    
                    // Фильтр по роли
                    boolean matchesRole = true;
                    if (selectedRole != null && !"Все роли".equals(selectedRole)) {
                        matchesRole = selectedRole.equals(user.getRoleName());
                    }
                    
                    return matchesSearch && matchesRole;
                })
                .collect(Collectors.toList());
        
        // Обновляем таблицу
        usersTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }
    
    @FXML
    public void onAddUser() {
        showUserDialog(null);
    }
    
    private void onEditUser(UserDTO user) {
        showUserDialog(user);
    }
    
    private void onDeleteUser(UserDTO user) {
        // Показываем диалог подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление пользователя");
        alert.setContentText("Вы уверены, что хотите удалить пользователя " + user.getUsername() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Подтверждено - удаляем пользователя
            deleteUser(user);
        }
    }
    
    private void deleteUser(UserDTO user) {
        showStatus("Удаление пользователя...");
        
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                // Создаем запрос на удаление пользователя
                DeleteUserRequest request = new DeleteUserRequest(user.getUserId());
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                
                if (response instanceof DeleteUserResponse) {
                    DeleteUserResponse deleteResponse = (DeleteUserResponse) response;
                    
                    Platform.runLater(() -> {
                        if (deleteResponse.isSuccess()) {
                            showSuccess("Пользователь успешно удален");
                            loadUsers(); // Перезагружаем список
                        } else {
                            showError("Ошибка: " + deleteResponse.getMessage());
                        }
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showError("Ошибка при удалении пользователя: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showUserDialog(UserDTO user) {
        try {
            // Загружаем форму редактирования/добавления пользователя
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/UserEditForm.fxml"));
            Parent root = loader.load();
            
            // Настраиваем контроллер
            UserEditFormController controller = loader.getController();
            controller.setRoles(allRoles);
            
            if (user != null) {
                controller.setUser(user); // Режим редактирования
            }
            
            // Создаем новый Stage для формы
            Stage userStage = new Stage();
            userStage.initModality(Modality.APPLICATION_MODAL);
            userStage.initOwner(usersTable.getScene().getWindow());
            userStage.setTitle(user == null ? "Добавление пользователя" : "Редактирование пользователя");
            
            Scene scene = new Scene(root);
            userStage.setScene(scene);
            
            // Обновляем список пользователей после закрытия формы
            userStage.setOnHidden(event -> loadUsers());
            
            userStage.showAndWait();
            
        } catch (Exception e) {
            showError("Ошибка при открытии формы: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
        statusLabel.setVisible(true);
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
    
    private void hideStatus() {
        statusLabel.setVisible(false);
    }
} 