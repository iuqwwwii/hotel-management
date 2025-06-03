package com.hotel.server.dao;

import com.hotel.server.model.Partner;
import com.hotel.server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с партнерами
 */
public class PartnerDao {
    
    /**
     * Получает список всех партнеров
     */
    public List<Partner> findAll() {
        String sql = "SELECT partner_id, name, contact_info FROM partner ORDER BY name";
        List<Partner> partners = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Partner partner = new Partner();
                partner.setPartnerId(rs.getInt("partner_id"));
                partner.setName(rs.getString("name"));
                partner.setContactInfo(rs.getString("contact_info"));
                partners.add(partner);
            }
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при получении списка партнеров: " + e.getMessage());
            e.printStackTrace();
        }
        
        return partners;
    }
    
    /**
     * Получает партнера по ID
     */
    public Optional<Partner> findById(int partnerId) {
        String sql = "SELECT partner_id, name, contact_info FROM partner WHERE partner_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, partnerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Partner partner = new Partner();
                    partner.setPartnerId(rs.getInt("partner_id"));
                    partner.setName(rs.getString("name"));
                    partner.setContactInfo(rs.getString("contact_info"));
                    return Optional.of(partner);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при получении партнера: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Создает нового партнера
     */
    public Partner create(Partner partner) {
        String sql = "INSERT INTO partner (name, contact_info) VALUES (?, ?) RETURNING partner_id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, partner.getName());
            ps.setString(2, partner.getContactInfo());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    partner.setPartnerId(rs.getInt("partner_id"));
                    return partner;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при создании партнера: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Обновляет данные партнера
     */
    public boolean update(Partner partner) {
        String sql = "UPDATE partner SET name = ?, contact_info = ? WHERE partner_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, partner.getName());
            ps.setString(2, partner.getContactInfo());
            ps.setInt(3, partner.getPartnerId());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при обновлении партнера: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Удаляет партнера
     */
    public boolean delete(int partnerId) {
        String sql = "DELETE FROM partner WHERE partner_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, partnerId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при удалении партнера: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Получает статистику по партнеру: количество бронирований и общая сумма
     */
    public Object[] getPartnerStatistics(int partnerId) {
        String sql = "SELECT COUNT(b.booking_id) as bookings_count, " +
                     "COALESCE(SUM(b.total_cost), 0) as total_revenue " +
                     "FROM booking b " +
                     "WHERE b.partner_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, partnerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int bookingsCount = rs.getInt("bookings_count");
                    double totalRevenue = rs.getDouble("total_revenue");
                    return new Object[]{bookingsCount, totalRevenue};
                }
            }
            
        } catch (SQLException e) {
            System.err.println("PartnerDao: Ошибка при получении статистики: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new Object[]{0, 0.0};
    }
} 