package com.hotel.server.dao;

import com.hotel.common.ReviewDTO;
import com.hotel.server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с отзывами
 */
public class ReviewDao {

    /**
     * Получает все отзывы из базы данных
     * @return список отзывов
     */
    public List<ReviewDTO> getAllReviews() {
        String sql = "SELECT r.review_id, r.room_id, r.user_id, r.rating, r.comment, r.review_date, " +
                "u.username, u.full_name " +
                "FROM review r JOIN \"user\" u ON r.user_id = u.user_id " +
                "ORDER BY r.review_date DESC";
        
        List<ReviewDTO> reviews = new ArrayList<>();
        
        try (Connection c = DatabaseUtil.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ReviewDTO review = new ReviewDTO();
                review.setId(rs.getInt("review_id"));
                review.setRoomId(rs.getInt("room_id"));
                review.setUserId(rs.getInt("user_id"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setCreatedAt(rs.getTimestamp("review_date").toLocalDateTime());
                review.setUsername(rs.getString("username"));
                reviews.add(review);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getAllReviews", e);
        }
        
        return reviews;
    }
    
    /**
     * Получает отзывы для конкретной комнаты
     * @param roomId ID комнаты
     * @return список отзывов для комнаты
     */
    public List<ReviewDTO> getReviewsByRoomId(int roomId) {
        String sql = "SELECT r.review_id, r.room_id, r.user_id, r.rating, r.comment, r.review_date, " +
                "u.username, u.full_name " +
                "FROM review r JOIN \"user\" u ON r.user_id = u.user_id " +
                "WHERE r.room_id = ? " +
                "ORDER BY r.review_date DESC";
        
        List<ReviewDTO> reviews = new ArrayList<>();
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setInt(1, roomId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReviewDTO review = new ReviewDTO();
                    review.setId(rs.getInt("review_id"));
                    review.setRoomId(rs.getInt("room_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setCreatedAt(rs.getTimestamp("review_date").toLocalDateTime());
                    review.setUsername(rs.getString("username"));
                    reviews.add(review);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getReviewsByRoomId", e);
        }
        
        return reviews;
    }
    
    /**
     * Добавляет новый отзыв в базу данных
     * @param roomId ID комнаты
     * @param userId ID пользователя
     * @param rating оценка (от 1 до 5)
     * @param comment комментарий
     * @return созданный отзыв
     */
    public ReviewDTO addReview(int roomId, int userId, int rating, String comment) {
        String sql = "INSERT INTO review (room_id, user_id, rating, comment, review_date) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING review_id, review_date";
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int reviewId = rs.getInt("review_id");
                    Timestamp reviewDate = rs.getTimestamp("review_date");
                    
                    // Получаем информацию о пользователе
                    String userSql = "SELECT username FROM \"user\" WHERE user_id = ?";
                    try (PreparedStatement userPs = c.prepareStatement(userSql)) {
                        userPs.setInt(1, userId);
                        try (ResultSet userRs = userPs.executeQuery()) {
                            if (userRs.next()) {
                                ReviewDTO review = new ReviewDTO();
                                review.setId(reviewId);
                                review.setRoomId(roomId);
                                review.setUserId(userId);
                                review.setRating(rating);
                                review.setComment(comment);
                                review.setCreatedAt(reviewDate.toLocalDateTime());
                                review.setUsername(userRs.getString("username"));
                                return review;
                            }
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in addReview", e);
        }
        
        throw new RuntimeException("Failed to add review");
    }
    
    /**
     * Удаляет отзыв из базы данных
     * @param reviewId ID отзыва
     * @return true если удаление успешно, иначе false
     */
    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM review WHERE review_id = ?";
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setInt(1, reviewId);
            int rowsAffected = ps.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("DB error in deleteReview", e);
        }
    }
} 