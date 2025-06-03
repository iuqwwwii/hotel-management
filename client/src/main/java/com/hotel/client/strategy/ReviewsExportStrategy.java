package com.hotel.client.strategy;

import com.hotel.client.util.ExportHelper;
import com.hotel.common.FetchReviewsRequest;
import com.hotel.common.FetchReviewsResponse;
import com.hotel.common.ReviewDTO;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Стратегия экспорта данных об отзывах
 */
public class ReviewsExportStrategy extends AbstractExportStrategy {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String filter;
    
    /**
     * Создает новую стратегию экспорта данных об отзывах
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param startDate Начальная дата для фильтрации
     * @param endDate Конечная дата для фильтрации
     * @param filter Строка фильтрации
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public ReviewsExportStrategy(String format, LocalDate startDate, LocalDate endDate, String filter, boolean includeHeaders) {
        super(format, includeHeaders);
        this.startDate = startDate;
        this.endDate = endDate;
        this.filter = filter;
    }
    
    @Override
    public boolean export(File file) throws Exception {
        updateStatus("Подготовка к экспорту данных об отзывах");
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Отправляем запрос на получение списка отзывов
            FetchReviewsRequest request = new FetchReviewsRequest(true);
            out.writeObject(request);
            out.flush();
            
            // Получаем ответ
            Object response = in.readObject();
            
            if (response instanceof FetchReviewsResponse) {
                FetchReviewsResponse reviewsResponse = (FetchReviewsResponse) response;
                
                if (reviewsResponse.isSuccess() && reviewsResponse.getReviews() != null) {
                    List<ReviewDTO> reviews = reviewsResponse.getReviews();
                    
                    // Фильтруем по датам
                    if (startDate != null) {
                        final LocalDate finalStartDate = startDate;
                        reviews = reviews.stream()
                            .filter(r -> {
                                LocalDateTime reviewDate = r.getReviewDate();
                                return reviewDate != null && 
                                      !reviewDate.toLocalDate().isBefore(finalStartDate);
                            })
                            .collect(Collectors.toList());
                    }
                    
                    if (endDate != null) {
                        final LocalDate finalEndDate = endDate;
                        reviews = reviews.stream()
                            .filter(r -> {
                                LocalDateTime reviewDate = r.getReviewDate();
                                return reviewDate != null && 
                                      !reviewDate.toLocalDate().isAfter(finalEndDate);
                            })
                            .collect(Collectors.toList());
                    }
                    
                    // Фильтруем по строке фильтра, если она задана
                    if (filter != null && !filter.isEmpty()) {
                        final String filterLower = filter.toLowerCase();
                        reviews = reviews.stream()
                            .filter(r -> 
                                (r.getRoomNumber() != null && r.getRoomNumber().toString().contains(filterLower)) || 
                                (r.getComment() != null && r.getComment().toLowerCase().contains(filterLower)) ||
                                (r.getUserFullName() != null && r.getUserFullName().toLowerCase().contains(filterLower)) ||
                                (r.getUsername() != null && r.getUsername().toLowerCase().contains(filterLower)) ||
                                (String.valueOf(r.getRating()).contains(filterLower))
                            )
                            .collect(Collectors.toList());
                    }
                    
                    // Экспортируем данные
                    updateStatus("Экспорт " + reviews.size() + " отзывов");
                    
                    if (isCsvFormat()) {
                        return ExportHelper.exportReviewsToCSV(reviews, file);
                    } else {
                        return ExportHelper.exportReviewsToExcel(reviews, file);
                    }
                } else {
                    throw new Exception("Ошибка загрузки: " + 
                                       (reviewsResponse.getMessage() != null ? 
                                       reviewsResponse.getMessage() : "Неизвестная ошибка"));
                }
            } else {
                throw new Exception("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            handleError("Ошибка при экспорте отзывов", e);
            throw e;
        }
    }
    
    @Override
    public String getDataType() {
        return "reviews";
    }
} 