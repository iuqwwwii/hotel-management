package com.hotel.server.service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Random;

import com.hotel.common.ReviewDTO;
import com.hotel.server.dao.ReviewDao;
import com.hotel.server.dao.RoomDao;
import com.hotel.server.dao.UserDao;
import com.hotel.server.model.Room;
import com.hotel.server.model.User;

public class ReviewService {
    private final ReviewDao reviewDao = new ReviewDao();
    private final RoomDao roomDao = new RoomDao();
    private final UserDao userDao = new UserDao();
    
    public boolean createReview(int userId, int roomId, Integer bookingId, int rating, String comment) {
        try {
            // Проверяем, существует ли комната
            if (!roomDao.existsById(roomId)) {
                return false;
            }
            
            // Добавляем отзыв в базу данных
            ReviewDTO review = reviewDao.addReview(roomId, userId, rating, comment);
            return review != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<ReviewDTO> getAllReviews() {
        try {
            List<ReviewDTO> reviews = reviewDao.getAllReviews();
            
            // Дополняем информацию о номерах комнат
            for (ReviewDTO review : reviews) {
                try {
                    Room room = roomDao.findById(review.getRoomId());
                    if (room != null) {
                        // Преобразуем строку номера комнаты в Integer, если возможно
                        try {
                            Integer roomNumber = Integer.parseInt(room.getNumber());
                            review.setRoomNumber(roomNumber);
                        } catch (NumberFormatException e) {
                            // Если номер комнаты не числовой, оставляем как есть
                            System.err.println("Номер комнаты не является числом: " + room.getNumber());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при получении информации о комнате: " + e.getMessage());
                }
            }
            
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<ReviewDTO> getReviewsByUserId(Integer userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            // Получаем все отзывы и фильтруем по userId
            return getAllReviews().stream()
                .filter(review -> review.getUserId() == userId)
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<ReviewDTO> getReviewsByRoomId(Integer roomId) {
        if (roomId == null) {
            return new ArrayList<>();
        }
        
        try {
            return reviewDao.getReviewsByRoomId(roomId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public boolean deleteReview(int reviewId, int userId, boolean isAdmin) {
        try {
            // Проверяем, существует ли отзыв
            List<ReviewDTO> allReviews = getAllReviews();
            ReviewDTO reviewToDelete = null;
            
            for (ReviewDTO review : allReviews) {
                if (review.getId() == reviewId) {
                    reviewToDelete = review;
                    break;
                }
            }
            
            if (reviewToDelete == null) {
                return false;
            }
            
            // Проверяем права на удаление (администратор или автор отзыва)
            if (!isAdmin && reviewToDelete.getUserId() != userId) {
                return false;
            }
            
            // Удаляем отзыв
            return reviewDao.deleteReview(reviewId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<ReviewDTO> createTestReviews(int count) {
        List<ReviewDTO> createdReviews = new ArrayList<>();
        Random random = new Random();
        
        try {
            // Получаем список комнат и пользователей для создания тестовых отзывов
            List<Room> rooms = roomDao.findAll();
            List<User> users = new ArrayList<>();
            
            // Если нет комнат, не можем создать отзывы
            if (rooms.isEmpty()) {
                return createdReviews;
            }
            
            // Тестовые комментарии
            String[] comments = {
                "Отличный номер! Всё понравилось.",
                "Хороший сервис, но немного шумно.",
                "Прекрасный вид из окна, рекомендую!",
                "Чисто, уютно, комфортно.",
                "Соотношение цена-качество на высоте.",
                "Неплохо, но можно лучше.",
                "Очень понравилось обслуживание.",
                "Буду рекомендовать друзьям.",
                "Немного разочарован, ожидал большего.",
                "Всё отлично, обязательно вернусь!"
            };
            
            // Создаем тестовые отзывы
            for (int i = 0; i < count; i++) {
                // Выбираем случайную комнату и пользователя
                Room room = rooms.get(random.nextInt(rooms.size()));
                int userId = random.nextInt(5) + 1; // Предполагаем, что у нас есть пользователи с ID 1-5
                
                // Генерируем случайный рейтинг от 1 до 5
                int rating = random.nextInt(5) + 1;
                
                // Выбираем случайный комментарий
                String comment = comments[random.nextInt(comments.length)];
                
                // Создаем отзыв
                ReviewDTO review = reviewDao.addReview(room.getRoomId(), userId, rating, comment);
                
                if (review != null) {
                    // Преобразуем строку номера комнаты в Integer, если возможно
                    try {
                        Integer roomNumber = Integer.parseInt(room.getNumber());
                        review.setRoomNumber(roomNumber);
                    } catch (NumberFormatException e) {
                        // Если номер комнаты не числовой, пропускаем
                        System.err.println("Номер комнаты не является числом: " + room.getNumber());
                    }
                    
                    createdReviews.add(review);
                }
            }
            
            return createdReviews;
        } catch (Exception e) {
            e.printStackTrace();
            return createdReviews;
        }
    }
} 