package com.hotel.client.controller;

import com.hotel.common.RegisterUserRequest;
import com.hotel.common.RegisterUserResponse;
import com.hotel.common.util.ValidateUtil;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.stage.Stage;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ResourceBundle;

public class RegistrationController {
    @FXML
    private Pane rootVBox;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField cardField;
    @FXML
    private TextField expiryField;
    @FXML
    private TextField cvvField;
    @FXML
    private Label errorLabel;

    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML
    public void onRegister() {
        errorLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String card = cardField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();

        // Проверки на заполненность
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || fullName.isEmpty() || card.isEmpty() ||
                expiry.isEmpty() || cvv.isEmpty()) {
            errorLabel.setText(bundle.getString("registration.error.empty_fields"));
            return;
        }

        // Validate fields using ValidateUtil
        if (!ValidateUtil.isValidUsername(username)) {
            errorLabel.setText(bundle.getString("registration.error.username"));
            return;
        }
        if (!ValidateUtil.isValidPassword(password)) {
            errorLabel.setText(bundle.getString("registration.error.password"));
            return;
        }
        if (!ValidateUtil.isValidEmail(email)) {
            errorLabel.setText(bundle.getString("registration.error.email"));
            return;
        }
        if (!ValidateUtil.isValidPhone(phone)) {
            errorLabel.setText(bundle.getString("registration.error.phone"));
            return;
        }
        if (!ValidateUtil.isValidFullName(fullName)) {
            errorLabel.setText(bundle.getString("registration.error.fullname"));
            return;
        }
        if (!ValidateUtil.isValidCard(card)) {
            errorLabel.setText(bundle.getString("registration.error.card"));
            return;
        }
        if (!ValidateUtil.isValidExpiry(expiry)) {
            errorLabel.setText(bundle.getString("registration.error.expiry"));
            return;
        }
        if (!ValidateUtil.isValidCVV(cvv)) {
            errorLabel.setText(bundle.getString("registration.error.cvv"));
            return;
        }

        // Отправляем запрос на сервер
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {

            RegisterUserRequest req = new RegisterUserRequest(
                    username, password, email, phone,
                    fullName, card, expiry, cvv);
            out.writeObject(req);
            out.flush();

            Object response = in.readObject();
            if (response instanceof RegisterUserResponse) {
                RegisterUserResponse resp = (RegisterUserResponse) response;
                if (!resp.isSuccess()) {
                    errorLabel.setText(resp.getMessage());
                } else {
                    // Закрыть окно регистрации
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.close();
                }
            } else {
                errorLabel.setText(bundle.getString("registration.error.server"));
            }

        } catch (Exception e) {
            errorLabel.setText(bundle.getString("registration.error.connection"));
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        FadeTransition ft = new FadeTransition(Duration.millis(800), rootVBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}

