// File: server/src/main/java/com/hotel/server/service/BookingService.java
package com.hotel.server.service;

import com.hotel.common.BookingDTO;
import com.hotel.common.CreateBookingRequest;
import com.hotel.server.dao.BookingDao;
import com.hotel.server.dao.RoomDao;
import com.hotel.server.model.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingDao dao = new BookingDao();

    public List<BookingDTO> getAllBookings() {
        List<Booking> list = dao.findAll();
        return list.stream()
                .map(b -> {
                    BookingDTO dto = new BookingDTO(
                            b.getBookingId(),
                            b.getUserId(),
                            b.getRoomId(),
                            b.getStartDate(),
                            b.getEndDate(),
                            b.getTotalCost(),
                            b.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Получает бронирования конкретного пользователя с возможностью фильтрации
     * 
     * @param userId ID пользователя
     * @param statusFilter фильтр по статусу (может быть null)
     * @param startDateFilter минимальная дата начала бронирования (может быть null)
     * @return список бронирований пользователя, соответствующих фильтрам
     */
    public List<BookingDTO> getBookingsForUser(int userId, String statusFilter, LocalDate startDateFilter) {
        List<Booking> allBookings = dao.findAll();
        RoomDao roomDao = new RoomDao();
        
        return allBookings.stream()
                .filter(b -> b.getUserId() == userId)
                .filter(b -> statusFilter == null || statusFilter.isEmpty() || 
                             b.getStatus().equals(statusFilter))
                .filter(b -> startDateFilter == null || 
                             !b.getStartDate().isBefore(startDateFilter))
                .map(b -> {
                    String roomNumber = "";
                    String roomType = "";
                    try {
                        com.hotel.server.model.Room room = roomDao.findById(b.getRoomId());
                        if (room != null) {
                            roomNumber = room.getNumber();
                            roomType = room.getTypeName();
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при получении данных о номере: " + e.getMessage());
                    }
                    
                    return new BookingDTO(
                            b.getBookingId(),
                            b.getUserId(),
                            b.getRoomId(),
                            roomNumber,
                            roomType,
                            b.getStartDate(),
                            b.getEndDate(),
                            b.getTotalCost(),
                            b.getStatus());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Отменяет бронирование
     * 
     * @param bookingId ID бронирования
     * @param userId ID пользователя (для проверки прав)
     * @param reason причина отмены (может быть null)
     * @return обновленное бронирование или пустой Optional, если бронирование не найдено или нет прав
     */
    public Optional<BookingDTO> cancelBooking(int bookingId, int userId, String reason) {
        Optional<Booking> bookingOpt = dao.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Booking booking = bookingOpt.get();

        if (booking.getUserId() != userId) {
            return Optional.empty();
        }

        String status = booking.getStatus();
        if (!"NEW".equals(status) && !"CONFIRMED".equals(status)) {
            return Optional.empty();
        }

        booking.setStatus("CANCELLED");

        if (reason != null && !reason.isEmpty()) {
            String notes = booking.getNotes();
            if (notes == null || notes.isEmpty()) {
                booking.setNotes("Причина отмены: " + reason);
            } else {
                booking.setNotes(notes + "\nПричина отмены: " + reason);
            }
        }
        
        boolean updated = dao.update(booking);
        
        if (!updated) {
            return Optional.empty();
        }

        BookingDTO dto = new BookingDTO(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalCost(),
                booking.getStatus()
        );
        
        return Optional.of(dto);
    }

    public boolean createBooking(BookingDTO dto) {
        return createBookingFromDTO(dto, null, null, null, null, null, null);
    }
    
    public boolean createBookingFromRequest(CreateBookingRequest req) {
        BookingDTO dto = new BookingDTO(
            0,
            req.getUserId(),
            req.getRoomId(),
            req.getStartDate(),
            req.getEndDate(),
            req.getTotalCost(),
            req.getStatus()
        );
        
        return createBookingFromDTO(dto, 
            req.getPaymentMethod(),
            req.getNotes(),
            req.getCardNumber(),
            req.getCardExpiry(),
            req.getCardCvv(),
            req.getCardHolder());
    }
    
    private boolean createBookingFromDTO(BookingDTO dto, 
                                        String paymentMethod,
                                        String notes,
                                        String cardNumber,
                                        String cardExpiry,
                                        String cardCvv,
                                        String cardHolder) {
        Booking b = new Booking(
                0,
                dto.getUserId(),
                dto.getRoomId(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getTotalCost(),
                dto.getStatus(),
                paymentMethod,
                notes,
                cardNumber,
                cardExpiry, 
                cardCvv,
                cardHolder
        );
        
        return dao.create(b);
    }
    
    /**
     * Обновляет статус бронирования
     * 
     * @param bookingId ID бронирования
     * @param newStatus новый статус
     * @return обновленное бронирование или пустой Optional, если бронирование не найдено
     */
    public Optional<BookingDTO> updateBookingStatus(int bookingId, String newStatus) {
        Optional<Booking> bookingOpt = dao.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(newStatus);
        
        boolean updated = dao.update(booking);
        
        if (!updated) {
            return Optional.empty();
        }

        BookingDTO dto = new BookingDTO(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalCost(),
                booking.getStatus()
        );
        
        return Optional.of(dto);
    }
}

