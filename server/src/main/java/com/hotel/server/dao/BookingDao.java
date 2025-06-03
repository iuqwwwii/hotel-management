// File: server/src/main/java/com/hotel/server/dao/BookingDao.java
package com.hotel.server.dao;

import com.hotel.server.model.Booking;
import com.hotel.server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao {
    public List<Booking> findAll() {
        String sql = "SELECT booking_id, user_id, room_id, start_date, end_date, " +
                "total_cost, status, payment_method, notes, " +
                "card_number, card_expiry, cvv, card_holder " +
                "FROM booking";
        List<Booking> list = new ArrayList<>();
        System.out.println("BookingDao: Выполняем запрос для получения всех бронирований");
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking();
                b.setBookingId(rs.getInt("booking_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setRoomId(rs.getInt("room_id"));
                b.setStartDate(rs.getDate("start_date").toLocalDate());
                b.setEndDate(rs.getDate("end_date").toLocalDate());
                b.setTotalCost(rs.getDouble("total_cost"));
                b.setStatus(rs.getString("status"));
                
                // Добавляем новые поля
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setNotes(rs.getString("notes"));
                b.setCardNumber(rs.getString("card_number"));
                b.setCardExpiry(rs.getString("card_expiry"));
                b.setCardCvv(rs.getString("cvv"));
                b.setCardHolder(rs.getString("card_holder"));
                
                list.add(b);
                System.out.println("BookingDao: Загружено бронирование ID=" + b.getBookingId() + 
                                  ", UserID=" + b.getUserId() + ", Status=" + b.getStatus());
            }
            System.out.println("BookingDao: Всего загружено " + list.size() + " бронирований");
        } catch (SQLException e) {
            System.err.println("BookingDao: Ошибка при загрузке бронирований: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DB error in findAll bookings", e);
        }
        return list;
    }
    
    public Optional<Booking> findById(int bookingId) {
        String sql = "SELECT booking_id, user_id, room_id, start_date, end_date, " +
                "total_cost, status, payment_method, notes, " +
                "card_number, card_expiry, cvv, card_holder " +
                "FROM booking WHERE booking_id = ?";
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            
            p.setInt(1, bookingId);
            
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setUserId(rs.getInt("user_id"));
                    b.setRoomId(rs.getInt("room_id"));
                    b.setStartDate(rs.getDate("start_date").toLocalDate());
                    b.setEndDate(rs.getDate("end_date").toLocalDate());
                    b.setTotalCost(rs.getDouble("total_cost"));
                    b.setStatus(rs.getString("status"));
                    
                    // Добавляем новые поля
                    b.setPaymentMethod(rs.getString("payment_method"));
                    b.setNotes(rs.getString("notes"));
                    b.setCardNumber(rs.getString("card_number"));
                    b.setCardExpiry(rs.getString("card_expiry"));
                    b.setCardCvv(rs.getString("cvv"));
                    b.setCardHolder(rs.getString("card_holder"));
                    
                    return Optional.of(b);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById booking", e);
        }
        
        return Optional.empty();
    }

    public boolean create(Booking b) {
        String sql = "INSERT INTO booking (user_id, room_id, start_date, end_date, " +
                "total_cost, status, payment_method, notes, " +
                "card_number, card_expiry, cvv, card_holder) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, b.getUserId());
            p.setInt(2, b.getRoomId());
            p.setDate(3, Date.valueOf(b.getStartDate()));
            p.setDate(4, Date.valueOf(b.getEndDate()));
            p.setDouble(5, b.getTotalCost());
            p.setString(6, b.getStatus());
            
            // Устанавливаем новые параметры (могут быть null)
            p.setString(7, b.getPaymentMethod());
            p.setString(8, b.getNotes());
            p.setString(9, b.getCardNumber());
            p.setString(10, b.getCardExpiry());
            p.setString(11, b.getCardCvv());
            p.setString(12, b.getCardHolder());
            
            return p.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create booking", e);
        }
    }
    
    public boolean update(Booking b) {
        String sql = "UPDATE booking SET user_id = ?, room_id = ?, start_date = ?, " +
                "end_date = ?, total_cost = ?, status = ?, payment_method = ?, " +
                "notes = ?, card_number = ?, card_expiry = ?, cvv = ?, card_holder = ? " +
                "WHERE booking_id = ?";
        
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, b.getUserId());
            p.setInt(2, b.getRoomId());
            p.setDate(3, Date.valueOf(b.getStartDate()));
            p.setDate(4, Date.valueOf(b.getEndDate()));
            p.setDouble(5, b.getTotalCost());
            p.setString(6, b.getStatus());
            p.setString(7, b.getPaymentMethod());
            p.setString(8, b.getNotes());
            p.setString(9, b.getCardNumber());
            p.setString(10, b.getCardExpiry());
            p.setString(11, b.getCardCvv());
            p.setString(12, b.getCardHolder());
            p.setInt(13, b.getBookingId());
            
            return p.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in update booking", e);
        }
    }
}

