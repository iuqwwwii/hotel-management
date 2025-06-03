package com.hotel.client.strategy;

import com.hotel.client.util.ExportHelper;
import com.hotel.common.*;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Стратегия экспорта статистических данных
 */
public class StatisticsExportStrategy extends AbstractExportStrategy {
    
    /**
     * Создает новую стратегию экспорта статистических данных
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public StatisticsExportStrategy(String format, boolean includeHeaders) {
        super(format, includeHeaders);
    }
    
    @Override
    public boolean export(File file) throws Exception {
        updateStatus("Подготовка к экспорту статистических данных");
        
        try {
            // Собираем общую статистику
            Map<String, Object> statistics = new HashMap<>();
            
            // Загружаем данные о комнатах
            loadRoomStatistics(statistics);
            
            // Загружаем данные о бронированиях
            loadBookingStatistics(statistics);
            
            // Загружаем данные о партнерах
            loadPartnerStatistics(statistics);
            
            // Экспортируем данные
            updateStatus("Экспорт статистики");
            
            if (isCsvFormat()) {
                return ExportHelper.exportStatisticsToCSV(statistics, file);
            } else {
                return ExportHelper.exportStatisticsToExcel(statistics, file);
            }
            
        } catch (Exception e) {
            handleError("Ошибка при экспорте статистики", e);
            throw e;
        }
    }
    
    /**
     * Загружает статистику по комнатам
     */
    private void loadRoomStatistics(Map<String, Object> statistics) throws Exception {
        // Добавляем информацию о комнатах
        int totalRooms = 0;
        int availableRooms = 0;
        int occupiedRooms = 0;
        double avgRoomPrice = 0.0;
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            updateStatus("Загрузка данных о комнатах");
            
            FetchRoomsRequest roomsRequest = new FetchRoomsRequest();
            out.writeObject(roomsRequest);
            out.flush();
            
            Object roomsResponse = in.readObject();
            if (roomsResponse instanceof FetchRoomsResponse) {
                FetchRoomsResponse resp = (FetchRoomsResponse) roomsResponse;
                if (resp.isSuccess() && resp.getRooms() != null) {
                    List<RoomDTO> rooms = resp.getRooms();
                    totalRooms = rooms.size();
                    
                    for (RoomDTO room : rooms) {
                        if ("AVAILABLE".equals(room.getStatus())) {
                            availableRooms++;
                        } else if ("OCCUPIED".equals(room.getStatus())) {
                            occupiedRooms++;
                        }
                        avgRoomPrice += room.getBasePrice();
                    }
                    
                    if (totalRooms > 0) {
                        avgRoomPrice /= totalRooms;
                    }
                }
            }
        }
        
        // Добавляем информацию в общую статистику
        statistics.put("Общее количество номеров", totalRooms);
        statistics.put("Доступные номера", availableRooms);
        statistics.put("Занятые номера", occupiedRooms);
        statistics.put("Средняя цена номера", avgRoomPrice);
    }
    
    /**
     * Загружает статистику по бронированиям
     */
    private void loadBookingStatistics(Map<String, Object> statistics) throws Exception {
        // Добавляем информацию о бронированиях
        int totalBookings = 0;
        int newBookings = 0;
        int confirmedBookings = 0;
        int checkedInBookings = 0;
        int checkedOutBookings = 0;
        int cancelledBookings = 0;
        double totalRevenue = 0.0;
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            updateStatus("Загрузка данных о бронированиях");
            
            GetAllBookingsRequest bookingsRequest = new GetAllBookingsRequest();
            out.writeObject(bookingsRequest);
            out.flush();
            
            Object bookingsResponse = in.readObject();
            if (bookingsResponse instanceof GetAllBookingsResponse) {
                GetAllBookingsResponse resp = (GetAllBookingsResponse) bookingsResponse;
                List<BookingDTO> bookings = resp.getBookings();
                totalBookings = bookings.size();
                
                for (BookingDTO booking : bookings) {
                    switch (booking.getStatus()) {
                        case "NEW": newBookings++; break;
                        case "CONFIRMED": confirmedBookings++; break;
                        case "CHECKED_IN": checkedInBookings++; break;
                        case "CHECKED_OUT": checkedOutBookings++; break;
                        case "CANCELLED": cancelledBookings++; break;
                    }
                    
                    // Учитываем выручку только для не отмененных бронирований
                    if (!"CANCELLED".equals(booking.getStatus())) {
                        totalRevenue += booking.getTotalCost();
                    }
                }
            }
        }
        
        // Добавляем информацию в общую статистику
        statistics.put("Всего бронирований", totalBookings);
        statistics.put("Новые бронирования", newBookings);
        statistics.put("Подтвержденные бронирования", confirmedBookings);
        statistics.put("Заселенные", checkedInBookings);
        statistics.put("Выселенные", checkedOutBookings);
        statistics.put("Отмененные", cancelledBookings);
        statistics.put("Общая выручка", totalRevenue);
    }
    
    /**
     * Загружает статистику по партнерам
     */
    private void loadPartnerStatistics(Map<String, Object> statistics) throws Exception {
        // Добавляем информацию о партнерах
        int totalPartners = 0;
        int activePartners = 0;
        double partnerRevenue = 0.0;
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            updateStatus("Загрузка данных о партнерах");
            
            FetchPartnersRequest partnersRequest = new FetchPartnersRequest();
            out.writeObject(partnersRequest);
            out.flush();
            
            Object partnersResponse = in.readObject();
            if (partnersResponse instanceof FetchPartnersResponse) {
                FetchPartnersResponse resp = (FetchPartnersResponse) partnersResponse;
                if (resp.isSuccess() && resp.getPartners() != null) {
                    List<PartnerDTO> partners = resp.getPartners();
                    totalPartners = partners.size();
                    
                    for (PartnerDTO partner : partners) {
                        if (partner.getBookingsCount() > 0) {
                            activePartners++;
                        }
                        partnerRevenue += partner.getTotalRevenue();
                    }
                }
            }
        }
        
        // Добавляем информацию в общую статистику
        double totalRevenue = (Double) statistics.getOrDefault("Общая выручка", 0.0);
        
        statistics.put("Всего партнеров", totalPartners);
        statistics.put("Активные партнеры", activePartners);
        statistics.put("Выручка от партнеров", partnerRevenue);
        statistics.put("Процент выручки от партнеров", 
                      totalRevenue > 0 ? (partnerRevenue / totalRevenue) * 100 : 0);
    }
    
    @Override
    public String getDataType() {
        return "statistics";
    }
} 