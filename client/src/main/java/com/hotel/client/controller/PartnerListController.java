package com.hotel.client.controller;

import com.hotel.common.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Контроллер для отображения и управления списком партнеров
 */
public class PartnerListController {
    @FXML private TableView<PartnerDTO> partnersTable;
    @FXML private TableColumn<PartnerDTO, Integer> idColumn;
    @FXML private TableColumn<PartnerDTO, String> nameColumn;
    @FXML private TableColumn<PartnerDTO, String> contactInfoColumn;
    @FXML private TableColumn<PartnerDTO, Integer> bookingsCountColumn;
    @FXML private TableColumn<PartnerDTO, Double> revenueColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label statusLabel;
    
    private ObservableList<PartnerDTO> partnersList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Настраиваем столбцы таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("partnerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactInfoColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
        bookingsCountColumn.setCellValueFactory(new PropertyValueFactory<>("bookingsCount"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        
        // Форматирование столбца с выручкой
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        revenueColumn.setCellFactory(column -> new TableCell<PartnerDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });
        
        // Связываем таблицу с данными
        partnersTable.setItems(partnersList);
        
        // Активация кнопок редактирования и удаления только при выборе партнера
        partnersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean partnerSelected = newSelection != null;
            editButton.setDisable(!partnerSelected);
            deleteButton.setDisable(!partnerSelected);
        });
        
        // Загружаем данные
        loadPartners();
    }
    
    /**
     * Загружает список партнеров с сервера
     */
    private void loadPartners() {
        // Очищаем статус
        setStatus("Загрузка данных...", false);
        
        // Запускаем загрузку в отдельном потоке
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на получение списка партнеров
                FetchPartnersRequest request = new FetchPartnersRequest();
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                
                if (response instanceof FetchPartnersResponse) {
                    FetchPartnersResponse partnersResponse = (FetchPartnersResponse) response;
                    
                    Platform.runLater(() -> {
                        if (partnersResponse.isSuccess() && partnersResponse.getPartners() != null) {
                            // Обновляем список партнеров
                            partnersList.clear();
                            partnersList.addAll(partnersResponse.getPartners());
                            
                            if (partnersList.isEmpty()) {
                                setStatus("Нет данных о партнерах", false);
                            } else {
                                setStatus("Загружено партнеров: " + partnersList.size(), false);
                            }
                        } else {
                            setStatus("Ошибка загрузки: " + partnersResponse.getMessage(), true);
                        }
                    });
                } else {
                    Platform.runLater(() -> setStatus("Ошибка: неожиданный ответ от сервера", true));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Ошибка соединения: " + e.getMessage(), true));
            }
        }).start();
    }
    
    /**
     * Обработчик нажатия на кнопку "Добавить"
     */
    @FXML
    private void onAddPartner() {
        showPartnerDialog(null);
    }
    
    /**
     * Обработчик нажатия на кнопку "Редактировать"
     */
    @FXML
    private void onEditPartner() {
        PartnerDTO selectedPartner = partnersTable.getSelectionModel().getSelectedItem();
        if (selectedPartner != null) {
            showPartnerDialog(selectedPartner);
        }
    }
    
    /**
     * Обработчик нажатия на кнопку "Удалить"
     */
    @FXML
    private void onDeletePartner() {
        PartnerDTO selectedPartner = partnersTable.getSelectionModel().getSelectedItem();
        if (selectedPartner == null) return;
        
        // Показываем диалог подтверждения
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Подтверждение удаления");
        confirmDialog.setHeaderText("Удаление партнера");
        confirmDialog.setContentText("Вы уверены, что хотите удалить партнера '" + selectedPartner.getName() + "'?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deletePartner(selectedPartner.getPartnerId());
            }
        });
    }
    
    /**
     * Удаляет партнера с указанным ID
     */
    private void deletePartner(int partnerId) {
        setStatus("Удаление партнера...", false);
        
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Отправляем запрос на удаление партнера
                DeletePartnerRequest request = new DeletePartnerRequest(partnerId);
                out.writeObject(request);
                out.flush();
                
                // Получаем ответ
                Object response = in.readObject();
                
                if (response instanceof DeletePartnerResponse) {
                    DeletePartnerResponse deleteResponse = (DeletePartnerResponse) response;
                    
                    Platform.runLater(() -> {
                        if (deleteResponse.isSuccess()) {
                            // Удаляем партнера из списка
                            partnersList.removeIf(p -> p.getPartnerId() == partnerId);
                            setStatus("Партнер успешно удален", false);
                        } else {
                            setStatus("Ошибка удаления: " + deleteResponse.getMessage(), true);
                        }
                    });
                } else {
                    Platform.runLater(() -> setStatus("Ошибка: неожиданный ответ от сервера", true));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Ошибка соединения: " + e.getMessage(), true));
            }
        }).start();
    }
    
    /**
     * Показывает диалог добавления/редактирования партнера
     */
    private void showPartnerDialog(PartnerDTO partner) {
        try {
            // Загружаем FXML для диалога
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PartnerEdit.fxml"));
            Parent root = loader.load();
            
            // Получаем контроллер
            PartnerEditController controller = loader.getController();
            
            // Если редактируем существующего партнера
            if (partner != null) {
                controller.setPartner(partner);
            }
            
            // Создаем и настраиваем новое окно
            Stage dialogStage = new Stage();
            dialogStage.setTitle(partner == null ? "Добавление партнера" : "Редактирование партнера");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(partnersTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Устанавливаем обработчик закрытия диалога
            controller.setDialogStage(dialogStage);
            controller.setOnPartnerSaved(savedPartner -> {
                // Обновляем список партнеров после сохранения
                if (partner == null) {
                    // Добавляем нового партнера
                    partnersList.add(savedPartner);
                } else {
                    // Обновляем существующего партнера
                    int index = partnersList.indexOf(partner);
                    if (index >= 0) {
                        partnersList.set(index, savedPartner);
                    }
                }
            });
            
            // Показываем диалог
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("Ошибка: " + e.getMessage(), true);
        }
    }
    
    /**
     * Устанавливает текст статуса
     */
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "black"));
    }
    
    /**
     * Обработчик нажатия на кнопку "Обновить"
     */
    @FXML
    private void onRefresh() {
        loadPartners();
    }
} 