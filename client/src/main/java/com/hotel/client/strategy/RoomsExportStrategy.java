package com.hotel.client.strategy;

import com.hotel.client.util.ExportHelper;
import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Стратегия экспорта данных о комнатах
 */
public class RoomsExportStrategy extends AbstractExportStrategy {
    
    private final String filter;
    
    /**
     * Создает новую стратегию экспорта данных о комнатах
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param filter Строка фильтрации
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public RoomsExportStrategy(String format, String filter, boolean includeHeaders) {
        super(format, includeHeaders);
        this.filter = filter;
    }
    
    @Override
    public boolean export(File file) throws Exception {
        updateStatus("Подготовка к экспорту данных о комнатах");
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Отправляем запрос на получение списка комнат
            FetchRoomsRequest request = new FetchRoomsRequest();
            out.writeObject(request);
            out.flush();
            
            // Получаем ответ
            Object response = in.readObject();
            
            if (response instanceof FetchRoomsResponse) {
                FetchRoomsResponse roomsResponse = (FetchRoomsResponse) response;
                
                if (roomsResponse.isSuccess() && roomsResponse.getRooms() != null) {
                    List<RoomDTO> rooms = roomsResponse.getRooms();
                    
                    // Фильтруем по строке фильтра, если она задана
                    if (filter != null && !filter.isEmpty()) {
                        final String filterLower = filter.toLowerCase();
                        rooms = rooms.stream()
                            .filter(r -> 
                                (r.getNumber() != null && r.getNumber().toLowerCase().contains(filterLower)) || 
                                (r.getTypeName() != null && r.getTypeName().toLowerCase().contains(filterLower)) ||
                                (r.getStatus() != null && r.getStatus().toLowerCase().contains(filterLower)) ||
                                String.valueOf(r.getBasePrice()).contains(filterLower)
                            )
                            .collect(Collectors.toList());
                    }
                    
                    // Экспортируем данные
                    updateStatus("Экспорт " + rooms.size() + " комнат");
                    
                    if (isCsvFormat()) {
                        return ExportHelper.exportRoomsToCSV(rooms, file);
                    } else {
                        return ExportHelper.exportRoomsToExcel(rooms, file);
                    }
                } else {
                    throw new Exception("Ошибка загрузки: " + 
                                      (roomsResponse.getMessage() != null ? 
                                      roomsResponse.getMessage() : "Неизвестная ошибка"));
                }
            } else {
                throw new Exception("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            handleError("Ошибка при экспорте комнат", e);
            throw e;
        }
    }
    
    @Override
    public String getDataType() {
        return "rooms";
    }
} 