package com.hotel.client.strategy;

import com.hotel.client.util.ExportHelper;
import com.hotel.common.BookingDTO;
import com.hotel.common.GetAllBookingsRequest;
import com.hotel.common.GetAllBookingsResponse;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Стратегия экспорта данных о бронированиях
 */
public class BookingsExportStrategy extends AbstractExportStrategy {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String filter;
    
    /**
     * Создает новую стратегию экспорта данных о бронированиях
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param startDate Начальная дата для фильтрации
     * @param endDate Конечная дата для фильтрации
     * @param filter Строка фильтрации
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public BookingsExportStrategy(String format, LocalDate startDate, LocalDate endDate, String filter, boolean includeHeaders) {
        super(format, includeHeaders);
        this.startDate = startDate;
        this.endDate = endDate;
        this.filter = filter;
    }
    
    @Override
    public boolean export(File file) throws Exception {
        updateStatus("Подготовка к экспорту данных о бронированиях");
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Отправляем запрос на получение списка бронирований
            GetAllBookingsRequest request = new GetAllBookingsRequest();
            out.writeObject(request);
            out.flush();
            
            // Получаем ответ
            Object response = in.readObject();
            
            if (response instanceof GetAllBookingsResponse) {
                GetAllBookingsResponse bookingsResponse = (GetAllBookingsResponse) response;
                List<BookingDTO> bookings = bookingsResponse.getBookings();
                
                // Фильтруем по датам
                if (startDate != null) {
                    final LocalDate finalStartDate = startDate;
                    bookings = bookings.stream()
                        .filter(b -> !b.getStartDate().isBefore(finalStartDate))
                        .collect(Collectors.toList());
                }
                
                if (endDate != null) {
                    final LocalDate finalEndDate = endDate;
                    bookings = bookings.stream()
                        .filter(b -> !b.getStartDate().isAfter(finalEndDate))
                        .collect(Collectors.toList());
                }
                
                // Фильтруем по строке фильтра, если она задана
                if (filter != null && !filter.isEmpty()) {
                    final String filterLower = filter.toLowerCase();
                    bookings = bookings.stream()
                        .filter(b -> 
                            (b.getRoomNumber() != null && b.getRoomNumber().contains(filterLower)) || 
                            (b.getStatus() != null && b.getStatus().toLowerCase().contains(filterLower)) ||
                            (b.getRoomType() != null && b.getRoomType().toLowerCase().contains(filterLower)) ||
                            String.valueOf(b.getUserId()).contains(filterLower) ||
                            String.valueOf(b.getTotalCost()).contains(filterLower)
                        )
                        .collect(Collectors.toList());
                }
                
                // Экспортируем данные
                updateStatus("Экспорт " + bookings.size() + " бронирований");
                
                if (isCsvFormat()) {
                    return ExportHelper.exportBookingsToCSV(bookings, file);
                } else {
                    return ExportHelper.exportBookingsToExcel(bookings, file);
                }
                
            } else {
                throw new Exception("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            handleError("Ошибка при экспорте бронирований", e);
            throw e;
        }
    }
    
    @Override
    public String getDataType() {
        return "bookings";
    }
} 