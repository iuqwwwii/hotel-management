package com.hotel.client.controller;

import com.hotel.common.LoginRequest;
import com.hotel.common.LoginResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ResourceBundle;

public class LoginController {
    @FXML private TextField loginField;
    @FXML private PasswordField passField;
    @FXML private Label errorLabel;

    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle_ru");

    @FXML
    public void onLogin() {
        errorLabel.setText("");
        String user = loginField.getText(), pass = passField.getText();
        if (user.isBlank()||pass.isBlank()) { errorLabel.setText(bundle.getString("login.error.empty")); return; }
        try (Socket sock=new Socket("localhost",5555);
             ObjectOutputStream out=new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in=new ObjectInputStream(sock.getInputStream())) {

            out.writeObject(new LoginRequest(user, pass)); out.flush();
            LoginResponse resp=(LoginResponse)in.readObject();
            if (!resp.isSuccess()) { errorLabel.setText(bundle.getString("login.error.invalid"));return; }

            String fxml = "/fxml/" + resp.getRoleName().toLowerCase() + "main.fxml";
            FXMLLoader loader=new FXMLLoader(getClass().getResource(fxml), bundle);
            Parent root=loader.load();
            Stage st=(Stage)loginField.getScene().getWindow();
            st.setTitle(bundle.getString("title."+resp.getRoleName().toLowerCase()));
            st.setScene(new Scene(root)); st.show();
        } catch (Exception e) {
            errorLabel.setText(bundle.getString("login.error.connection")); e.printStackTrace();
        }
    }

    @FXML
    public void openRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RegistrationForm.fxml"),
                    bundle);
            Parent root = loader.load();
            Stage regStage = new Stage();
            regStage.setTitle("Регистрация");
            regStage.initOwner(loginField.getScene().getWindow());
            regStage.initModality(Modality.WINDOW_MODAL);
            regStage.setScene(new Scene(root));
            regStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

