package com.hotel.client.controller;

import com.hotel.common.UserDTO;
import com.hotel.common.CreateUserRequest;
import com.hotel.common.CreateUserResponse;
import com.hotel.common.UpdateUserRequest;
import com.hotel.common.UpdateUserResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserEditFormController {
    
    @FXML private Label formTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordLabel;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    
    private UserDTO user;
    private List<String> roles = new ArrayList<>();
    private boolean isEditMode = false;
    
    @FXML
    public void initialize() {
        // Настройка отображения ошибок
        errorLabel.setVisible(false);
    }
    
    /**
     * Устанавливает список доступных ролей
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
        roleComboBox.setItems(FXCollections.observableArrayList(roles));
    }
    
    /**
     * Устанавливает пользователя для редактирования
     */
    public void setUser(UserDTO user) {
        this.user = user;
        this.isEditMode = true;
        
        // Обновляем заголовок формы
        formTitleLabel.setText("Редактирование пользователя");
        
        // Скрываем поле пароля при редактировании
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        passwordLabel.setVisible(false);
        passwordLabel.setManaged(false);
        
        // Заполняем поля данными пользователя
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        fullNameField.setText(user.getFullName());
        
        // Выбираем роль пользователя
        if (user.getRoleName() != null) {
            roleComboBox.setValue(user.getRoleName());
        }
    }
    
    @FXML
    public void onSave() {
        // Валидация полей
        if (!validateFields()) {
            return;
        }
        
        // Создаем или обновляем объект пользователя
        if (user == null) {
            user = new UserDTO();
        }
        
        user.setUsername(usernameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setFullName(fullNameField.getText().trim());
        user.setRoleName(roleComboBox.getValue());
        
        // Отправляем запрос на сервер
        if (isEditMode) {
            updateUser();
        } else {
            createUser();
        }
    }
    
    @FXML
    public void onCancel() {
        closeForm();
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        // Проверка обязательных полей
        if (usernameField.getText().trim().isEmpty()) {
            errors.append("Логин обязателен для заполнения\n");
        }
        
        if (!isEditMode && passwordField.getText().trim().isEmpty()) {
            errors.append("Пароль обязателен для заполнения\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email обязателен для заполнения\n");
        }
        
        if (fullNameField.getText().trim().isEmpty()) {
            errors.append("ФИО обязательно для заполнения\n");
        }
        
        if (roleComboBox.getValue() == null) {
            errors.append("Выберите роль пользователя\n");
        }
        
        // Если есть ошибки, отображаем их
        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void createUser() {
        // Создаем запрос на создание пользователя
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(usernameField.getText().trim());
        request.setPassword(passwordField.getText().trim());
        request.setEmail(emailField.getText().trim());
        request.setPhone(phoneField.getText().trim());
        request.setFullName(fullNameField.getText().trim());
        request.setRoleName(roleComboBox.getValue());
        
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                
                if (response instanceof CreateUserResponse) {
                    CreateUserResponse createResponse = (CreateUserResponse) response;
                    
                    Platform.runLater(() -> {
                        if (createResponse.isSuccess()) {
                            // Закрываем форму после успешного создания
                            closeForm();
                        } else {
                            showError(createResponse.getMessage());
                        }
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showError("Ошибка при создании пользователя: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }
    
    private void updateUser() {
        // Создаем запрос на обновление пользователя
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserId(user.getUserId());
        request.setUsername(usernameField.getText().trim());
        request.setEmail(emailField.getText().trim());
        request.setPhone(phoneField.getText().trim());
        request.setFullName(fullNameField.getText().trim());
        request.setRoleName(roleComboBox.getValue());
        
        new Thread(() -> {
            try (Socket sock = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                
                if (response instanceof UpdateUserResponse) {
                    UpdateUserResponse updateResponse = (UpdateUserResponse) response;
                    
                    Platform.runLater(() -> {
                        if (updateResponse.isSuccess()) {
                            // Закрываем форму после успешного обновления
                            closeForm();
                        } else {
                            showError(updateResponse.getMessage());
                        }
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showError("Ошибка при обновлении пользователя: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void closeForm() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }
} 