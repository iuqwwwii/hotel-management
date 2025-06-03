package com.hotel.client.controller;

import com.hotel.common.FetchReviewsRequest;
import com.hotel.common.FetchReviewsResponse;
import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.ReviewDTO;
import com.hotel.common.RoomDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReviewsViewController {
    @FXML private ComboBox<RoomFilterItem> roomFilterComboBox;
    @FXML private ComboBox<Integer> ratingFilterComboBox;
    @FXML private VBox reviewsContainer;
    @FXML private Label statusLabel;
    @FXML private Button addReviewButton;
    @FXML private Label reviewsCountLabel;
    @FXML private StackPane loadingPane;
    
    private int currentUserId = 1; // ID текущего пользователя, по умолчанию 1
    private boolean isAdmin = false; // Флаг, является ли пользователь администратором
    private boolean isDirector = false; // Флаг, является ли пользователь директором
    private Integer selectedRoomId = null; // ID выбранной комнаты для фильтрации
    private Integer selectedRating = null; // Выбранный рейтинг для фильтрации
    private List<ReviewDTO> allReviews = new ArrayList<>();
    private List<RoomDTO> allRooms = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Настройка выпадающего списка оценок
        List<Integer> ratings = IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList());
        ratingFilterComboBox.setItems(FXCollections.observableArrayList(ratings));
        
        // Получаем ID пользователя из пользовательских данных сцены (если доступно)
        try {
            Scene scene = statusLabel.getScene();
            if (scene != null && scene.getWindow() != null) {
                // Пробуем получить контроллер из пользовательских данных
                Object userData = scene.getUserData();
                if (userData != null && userData instanceof GuestController) {
                    // Установим ID пользователя позже в методе setCurrentUserId
                    System.out.println("ReviewsViewController: GuestController найден в userData");
                } else {
                    System.out.println("ReviewsViewController: GuestController не найден в userData, используем ID по умолчанию: " + currentUserId);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении ID пользователя: " + e.getMessage());
        }
        
        // Загружаем комнаты для фильтра
        loadRooms();
        
        // Загружаем все отзывы
        loadReviews();
    }
    
    // Метод для установки ID пользователя извне
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        updateUserRole();
        // Перезагружаем отзывы при изменении ID пользователя
        loadReviews();
    }
    
    // Метод для установки роли пользователя
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        updateUserRole();
    }
    
    // Обновление интерфейса в зависимости от роли пользователя
    private void updateUserRole() {
        // Кнопка добавления отзыва видна только для гостей
        addReviewButton.setVisible(!isAdmin);
        addReviewButton.setManaged(!isAdmin);
    }
    
    // Загрузка комнат для фильтра
    private void loadRooms() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            out.writeObject(new FetchRoomsRequest());
            out.flush();
            
            Object response = in.readObject();
            
            if (response instanceof FetchRoomsResponse) {
                FetchRoomsResponse roomsResponse = (FetchRoomsResponse) response;
                
                if (roomsResponse.isSuccess()) {
                    allRooms = roomsResponse.getRooms();
                    
                    // Создаем список элементов для фильтра
                    List<RoomFilterItem> filterItems = new ArrayList<>();
                    filterItems.add(new RoomFilterItem(null, "Все номера"));
                    
                    // Добавляем все комнаты
                    for (RoomDTO room : allRooms) {
                        filterItems.add(new RoomFilterItem(room.getRoomId(), "Номер " + room.getNumber()));
                    }
                    
                    roomFilterComboBox.setItems(FXCollections.observableArrayList(filterItems));
                    roomFilterComboBox.getSelectionModel().selectFirst(); // Выбираем "Все номера"
                }
            }
            
        } catch (Exception e) {
            showError("Ошибка при загрузке комнат: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Загружает отзывы из базы данных
     */
    private void loadReviews() {
        reviewsContainer.getChildren().clear();
        showLoading(true);
        
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
            
            FetchReviewsRequest request;
            
            // Определяем, какой запрос отправить в зависимости от выбранных фильтров
            if (isAdmin || isDirector) {
                request = new FetchReviewsRequest(true); // Администратор видит все отзывы
            } else {
                if (selectedRoomId == null) {
                    // Гость без фильтра по комнате - отправляем запрос на получение всех отзывов
                    request = new FetchReviewsRequest(true);
                } else {
                    // Гость с выбранной комнатой - запрашиваем отзывы для конкретной комнаты
                    request = new FetchReviewsRequest();
                    request.setRoomId(selectedRoomId);
                }
            }
            
            System.out.println("ReviewsViewController: Отправляем запрос FetchReviewsRequest");
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            System.out.println("ReviewsViewController: Получен ответ " + response.getClass().getSimpleName());
            
            if (response instanceof FetchReviewsResponse) {
                FetchReviewsResponse resp = (FetchReviewsResponse) response;
                if (resp.isSuccess()) {
                    List<ReviewDTO> reviews = resp.getReviews();
                    System.out.println("ReviewsViewController: Получено " + reviews.size() + " отзывов");
                    
                    // Применяем фильтр по рейтингу, если он выбран
                    if (selectedRating != null) {
                        reviews = reviews.stream()
                                .filter(r -> r.getRating() == selectedRating)
                                .collect(Collectors.toList());
                        System.out.println("ReviewsViewController: После фильтрации по рейтингу " + 
                                         selectedRating + " осталось " + reviews.size() + " отзывов");
                    }
                    
                    // Обновляем счетчик отзывов
                    if (reviewsCountLabel != null) {
                        reviewsCountLabel.setText("Всего отзывов: " + reviews.size());
                    }
                    
                    if (reviews.isEmpty()) {
                        showEmptyState();
                    } else {
                        displayReviews(reviews);
                    }
                } else {
                    showError("Ошибка при загрузке отзывов: " + resp.getMessage());
                }
            } else {
                showError("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при загрузке отзывов: " + e.getMessage());
        } finally {
            showLoading(false);
        }
    }
    
    // Применение фильтров к отзывам
    private void applyFilters() {
        // Получаем выбранные значения фильтров
        RoomFilterItem selectedRoom = roomFilterComboBox.getValue();
        selectedRating = ratingFilterComboBox.getValue();
        
        // Обновляем выбранный ID комнаты для следующей загрузки
        if (selectedRoom != null) {
            selectedRoomId = selectedRoom.getRoomId();
        } else {
            selectedRoomId = null;
        }
        
        // Перезагружаем отзывы с новыми фильтрами
        loadReviews();
    }
    
    /**
     * Отображает список отзывов
     */
    private void displayReviews(List<ReviewDTO> reviews) {
        reviewsContainer.getChildren().clear();
        
        // Сортируем отзывы по дате (сначала новые)
        reviews.sort((r1, r2) -> r2.getReviewDate().compareTo(r1.getReviewDate()));
        
        for (ReviewDTO review : reviews) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/ReviewItemCard.fxml"));
                Parent reviewCard = loader.load();
                
                // Применяем стили
                reviewCard.getStylesheets().add(getClass().getResource("/css/reviews.css").toExternalForm());
                
                ReviewItemCardController controller = loader.getController();
                controller.setReviewData(review);
                
                // Настраиваем кнопки действий в зависимости от прав
                boolean canDelete = isAdmin || isDirector || 
                                   (review.getUserId() == currentUserId);
                boolean canReply = isAdmin || isDirector;
                
                controller.setActionButtonsVisibility(canDelete, canReply);
                
                // Устанавливаем коллбэк для удаления отзыва
                controller.setOnDeleteAction(reviewId -> {
                    handleDeleteReview(reviewId);
                });
                
                reviewsContainer.getChildren().add(reviewCard);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Ошибка при создании карточки отзыва: " + e.getMessage());
            }
        }
    }
    
    /**
     * Показывает состояние загрузки
     */
    private void showLoading(boolean isLoading) {
        if (loadingPane != null) {
            loadingPane.setVisible(isLoading);
            loadingPane.setManaged(isLoading);
        }
    }
    
    /**
     * Показывает пустое состояние при отсутствии отзывов
     */
    private void showEmptyState() {
        Label emptyLabel = new Label("Отзывов не найдено");
        emptyLabel.getStyleClass().add("empty-state-label");
        
        // Создаем иконку
        Region emptyIcon = new Region();
        emptyIcon.getStyleClass().add("icon-empty");
        emptyIcon.setPrefSize(64, 64);
        
        VBox emptyStateBox = new VBox(20, emptyIcon, emptyLabel);
        emptyStateBox.setAlignment(Pos.CENTER);
        emptyStateBox.setPadding(new Insets(50));
        emptyStateBox.getStyleClass().add("empty-state-box");
        
        reviewsContainer.getChildren().add(emptyStateBox);
    }
    
    /**
     * Обработчик удаления отзыва
     */
    private void handleDeleteReview(int reviewId) {
        // Здесь должен быть код для удаления отзыва
        // После успешного удаления перезагрузить список отзывов
        showSuccess("Отзыв успешно удален");
        loadReviews();
    }
    
    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success");
        statusLabel.getStyleClass().add("error");
        statusLabel.setVisible(true);
        
        // Автоматически скрываем сообщение через 5 секунд
        new Timeline(new KeyFrame(Duration.seconds(5), e -> statusLabel.setVisible(false)))
            .play();
    }
    
    /**
     * Показывает сообщение об успешном действии
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error");
        statusLabel.getStyleClass().add("success");
        statusLabel.setVisible(true);
        
        // Автоматически скрываем сообщение через 3 секунды
        new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setVisible(false)))
            .play();
    }
    
    @FXML
    public void onApplyFilter() {
        applyFilters();
    }
    
    @FXML
    public void onResetFilter() {
        // Сбрасываем фильтры
        roomFilterComboBox.getSelectionModel().selectFirst();
        ratingFilterComboBox.setValue(null);
        selectedRating = null;
        selectedRoomId = null;
        
        // Применяем сброшенные фильтры
        loadReviews();
    }
    
    @FXML
    public void onAddReview() {
        try {
            // Выводим ID пользователя для отладки
            System.out.println("ReviewsViewController: Opening add review form for user ID: " + currentUserId);
            
            if (currentUserId <= 0) {
                showError("Необходимо авторизоваться для добавления отзыва");
                return;
            }
            
            // Загружаем форму добавления отзыва
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/GuestAddReview.fxml"));
            Parent root = loader.load();
            
            // Настраиваем контроллер - явно передаем ID текущего пользователя
            GuestAddReviewController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            
            // Создаем новый Stage для формы
            Stage reviewStage = new Stage();
            reviewStage.initModality(Modality.APPLICATION_MODAL); // Блокирует взаимодействие с другими окнами
            reviewStage.initOwner(addReviewButton.getScene().getWindow());
            reviewStage.setTitle("Добавление отзыва");
            
            Scene scene = new Scene(root);
            reviewStage.setScene(scene);
            
            // Обновляем список отзывов после закрытия формы
            reviewStage.setOnHidden(event -> loadReviews());
            
            reviewStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Ошибка при открытии формы добавления отзыва: " + e.getMessage());
            e.printStackTrace();
            showError("Ошибка при открытии формы добавления отзыва: " + e.getMessage());
        }
    }
    
    /**
     * Вспомогательный класс для элементов фильтра по комнатам
     */
    public static class RoomFilterItem {
        private final Integer roomId;
        private final String displayName;
        
        public RoomFilterItem(Integer roomId, String displayName) {
            this.roomId = roomId;
            this.displayName = displayName;
        }
        
        public Integer getRoomId() {
            return roomId;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
} 

