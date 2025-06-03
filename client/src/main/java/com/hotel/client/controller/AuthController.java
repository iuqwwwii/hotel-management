package com.hotel.client.controller;

import com.hotel.common.LoginRequest;
import com.hotel.common.LoginResponse;
import com.hotel.common.RegisterUserRequest;
import com.hotel.common.RegisterUserResponse;
import com.hotel.common.util.ValidateUtil;
import com.hotel.client.util.WindowState;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import com.hotel.client.ClientApplication;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController {

    @FXML private StackPane root;
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;

    @FXML private TextField loginField;
    @FXML private PasswordField passField;
    @FXML private Label loginError;

    @FXML private TextField usernameField;
    @FXML private TextField emailRegField;
    @FXML private TextField fullNameRegField;
    @FXML private PasswordField passRegField;
    @FXML private PasswordField passRegConfirmField;
    @FXML private Label regError;

    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML
    public void initialize() {
        // Плавное появление
        FadeTransition ft = new FadeTransition(Duration.millis(800), loginPane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        
        // Добавляем слушателей для адаптивности при изменении размера окна
        loginPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Теперь мы можем получить доступ к Stage
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    // Устанавливаем минимальные размеры
                    stage.setMinWidth(500);
                    stage.setMinHeight(600);
                    
                    // Добавляем обработчик закрытия окна для сохранения состояния
                    stage.setOnCloseRequest(event -> {
                        WindowState.saveState(stage);
                    });
                }
            }
        });
    }

    @FXML
    public void showRegister() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);

        registerPane.setOpacity(0);
        registerPane.setVisible(true);
        registerPane.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(800), registerPane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    public void showLogin() {
        registerPane.setVisible(false);
        registerPane.setManaged(false);

        loginPane.setOpacity(0);
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(800), loginPane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }


    @FXML
    public void onLogin() {
        loginError.setText("");
        String user = loginField.getText(), pass = passField.getText();
        if (user.isBlank() || pass.isBlank()) {
            loginError.setText(bundle.getString("login.error.empty"));
            return;
        }
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {

            out.writeObject(new LoginRequest(user, pass));
            out.flush();
            LoginResponse resp = (LoginResponse) in.readObject();
            if (!resp.isSuccess()) {
                loginError.setText(bundle.getString("login.error.invalid"));
                return;
            }

            // Получаем текущий Stage
            Stage stage = (Stage) loginField.getScene().getWindow();
            
            // Сохраняем состояние окна перед переходом
            WindowState.saveState(stage);
            
            // В зависимости от роли вызываем нужный метод
            String role = resp.getRoleName().toUpperCase();
            switch (role) {
                case "ADMIN":
                    ClientApplication.showAdminView(stage);
                    break;
                case "EMPLOYEE":
                    ClientApplication.showEmployeeView(stage);
                    break;
                case "DIRECTOR":
                    ClientApplication.showDirectorView(stage);
                    break;
                default:
                    // Для гостя передаем ID пользователя
                    ClientApplication.showGuestView(stage, resp.getUserId());
                    break;
            }


        } catch (Exception e) {
            loginError.setText(bundle.getString("login.error.connection"));
            e.printStackTrace();
        }
    }

    @FXML
    public void onRegister() {
        // Очищаем прошлые ошибки
        regError.setText("");

        // Считываем и обрезаем пробелы
        String username   = usernameField.getText().trim();
        String email      = emailRegField.getText().trim();
        String fullName   = fullNameRegField.getText().trim();
        String password   = passRegField.getText();
        String confirmPwd = passRegConfirmField.getText();

        // Проверки на заполненность
        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty()
                || password.isEmpty() || confirmPwd.isEmpty()) {
            regError.setText("Заполните все поля");
            return;
        }

        // Локальная валидация
        if (!ValidateUtil.isValidUsername(username)) {
            regError.setText("Логин: 1–20 символов (буквы, цифры, . или _)");
            return;
        }
        if (!ValidateUtil.isValidEmail(email)) {
            regError.setText("Неверный формат e-mail");
            return;
        }
        if (!ValidateUtil.isValidFullName(fullName)) {
            regError.setText("ФИО: только буквы, пробелы и дефис");
            return;
        }
        if (!password.equals(confirmPwd)) {
            regError.setText("Пароли не совпадают");
            return;
        }
        if (!ValidateUtil.isValidPassword(password)) {
            regError.setText("Пароль: 7–20 символов, заглавная, строчная, цифра, спецсимвол");
            return;
        }

        // Отправляем на сервер
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in  = new ObjectInputStream(sock.getInputStream())) {

            // Конструируем запрос, все не нужные поля передаём как null/0
            RegisterUserRequest req = new RegisterUserRequest(
                    username,
                    password,
                    email,
                    null,   // phone
                    fullName,
                    null,   // cardNumber
                    null,   // cardExpiry
                    null    // cvv
            );
            out.writeObject(req);
            out.flush();

            Object respObj = in.readObject();
            if (respObj instanceof RegisterUserResponse) {
                RegisterUserResponse resp = (RegisterUserResponse) respObj;
                if (!resp.isSuccess()) {
                    regError.setText(resp.getMessage());
                } else {
                    // После успешной регистрации переключаемся на форму входа
                    showLogin();
                }
            } else {
                regError.setText("Неверный ответ от сервера");
            }

        } catch (Exception e) {
            regError.setText("Ошибка соединения с сервером");
            e.printStackTrace();
        }
    }

    @FXML
    private void onClose() {
        // Сохраняем состояние окна перед закрытием
        Stage stage = (Stage) root.getScene().getWindow();
        WindowState.saveState(stage);
        
        Platform.exit();
    }
}

