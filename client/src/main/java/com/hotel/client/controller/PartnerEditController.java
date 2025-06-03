package com.hotel.client.controller;

import com.hotel.common.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Контроллер для добавления/редактирования партнера
 */
public class PartnerEditController {
    @FXML private TextField nameField;
    @FXML private TextArea contactInfoArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    
    private PartnerDTO partnerToEdit;
    private boolean isNewPartner = true;
    private Stage dialogStage;
    private Consumer<PartnerDTO> onPartnerSavedCallback;
    
    @FXML
    public void initialize() {
        // Очищаем поля сообщений об ошибках
        hideError();
    }
    
    /**
     * Устанавливает партнера для редактирования
     */
    public void setPartner(PartnerDTO partner) {
        this.partnerToEdit = partner;
        this.isNewPartner = false;
        
        // Заполняем поля данными
        nameField.setText(partner.getName());
        contactInfoArea.setText(partner.getContactInfo());
    }
    
    /**
     * Устанавливает стадию диалога
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Устанавливает обработчик события сохранения партнера
     */
    public void setOnPartnerSaved(Consumer<PartnerDTO> callback) {
        this.onPartnerSavedCallback = callback;
    }
    
    /**
     * Обработчик нажатия на кнопку "Сохранить"
     */
    @FXML
    private void onSave() {
        if (!validateForm()) {
            return;
        }
        
        if (isNewPartner) {
            createPartner();
        } else {
            updatePartner();
        }
    }
    
    /**
     * Создает нового партнера
     */
    private void createPartner() {
        // Создаем DTO запроса
        CreatePartnerRequest request = new CreatePartnerRequest(
            nameField.getText().trim(),
            contactInfoArea.getText().trim()
        );
        
        // Отправляем запрос в отдельном потоке
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                
                if (response instanceof CreatePartnerResponse) {
                    CreatePartnerResponse createResponse = (CreatePartnerResponse) response;
                    
                    Platform.runLater(() -> {
                        if (createResponse.isSuccess() && createResponse.getPartner() != null) {
                            // Вызываем обработчик события сохранения
                            if (onPartnerSavedCallback != null) {
                                onPartnerSavedCallback.accept(createResponse.getPartner());
                            }
                            // Закрываем диалог
                            closeDialog();
                        } else {
                            showError("Ошибка: " + createResponse.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Ошибка: неожиданный ответ от сервера"));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка соединения: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Обновляет существующего партнера
     */
    private void updatePartner() {
        // Создаем DTO запроса
        UpdatePartnerRequest request = new UpdatePartnerRequest(
            partnerToEdit.getPartnerId(),
            nameField.getText().trim(),
            contactInfoArea.getText().trim()
        );
        
        // Отправляем запрос в отдельном потоке
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(request);
                out.flush();
                
                Object response = in.readObject();
                
                if (response instanceof UpdatePartnerResponse) {
                    UpdatePartnerResponse updateResponse = (UpdatePartnerResponse) response;
                    
                    Platform.runLater(() -> {
                        if (updateResponse.isSuccess()) {
                            // Вызываем обработчик события сохранения
                            if (onPartnerSavedCallback != null) {
                                // Если в ответе есть обновленный партнер, используем его
                                PartnerDTO updatedPartner = updateResponse.getPartner();
                                if (updatedPartner == null) {
                                    // Иначе обновляем существующий объект
                                    partnerToEdit.setName(nameField.getText().trim());
                                    partnerToEdit.setContactInfo(contactInfoArea.getText().trim());
                                    updatedPartner = partnerToEdit;
                                }
                                onPartnerSavedCallback.accept(updatedPartner);
                            }
                            // Закрываем диалог
                            closeDialog();
                        } else {
                            showError("Ошибка: " + updateResponse.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("Ошибка: неожиданный ответ от сервера"));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка соединения: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Проверяет правильность заполнения формы
     */
    private boolean validateForm() {
        String name = nameField.getText().trim();
        String contactInfo = contactInfoArea.getText().trim();
        
        if (name.isEmpty()) {
            showError("Название партнера не может быть пустым");
            return false;
        }
        
        if (contactInfo.isEmpty()) {
            showError("Контактная информация не может быть пустой");
            return false;
        }
        
        hideError();
        return true;
    }
    
    /**
     * Обработчик нажатия на кнопку "Отмена"
     */
    @FXML
    private void onCancel() {
        closeDialog();
    }
    
    /**
     * Закрывает диалог
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Скрывает сообщение об ошибке
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }
} 