package com.hotel.client.strategy;

import com.hotel.client.util.ExportHelper;
import com.hotel.common.FetchPartnersRequest;
import com.hotel.common.FetchPartnersResponse;
import com.hotel.common.PartnerDTO;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Стратегия экспорта данных о партнерах
 */
public class PartnersExportStrategy extends AbstractExportStrategy {
    
    private final String filter;
    
    /**
     * Создает новую стратегию экспорта данных о партнерах
     * 
     * @param format Формат экспорта (csv, xlsx)
     * @param filter Строка фильтрации
     * @param includeHeaders Включать ли заголовки в экспорт
     */
    public PartnersExportStrategy(String format, String filter, boolean includeHeaders) {
        super(format, includeHeaders);
        this.filter = filter;
    }
    
    @Override
    public boolean export(File file) throws Exception {
        updateStatus("Подготовка к экспорту данных о партнерах");
        
        try (Socket socket = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Отправляем запрос на получение списка партнеров
            FetchPartnersRequest request = new FetchPartnersRequest();
            out.writeObject(request);
            out.flush();
            
            // Получаем ответ
            Object response = in.readObject();
            
            if (response instanceof FetchPartnersResponse) {
                FetchPartnersResponse partnersResponse = (FetchPartnersResponse) response;
                
                if (partnersResponse.isSuccess() && partnersResponse.getPartners() != null) {
                    List<PartnerDTO> partners = partnersResponse.getPartners();
                    
                    // Фильтруем по строке фильтра, если она задана
                    if (filter != null && !filter.isEmpty()) {
                        final String filterLower = filter.toLowerCase();
                        partners.removeIf(p -> !p.getName().toLowerCase().contains(filterLower) && 
                                              !p.getContactInfo().toLowerCase().contains(filterLower));
                    }
                    
                    // Экспортируем данные
                    updateStatus("Экспорт " + partners.size() + " партнеров");
                    
                    if (isCsvFormat()) {
                        return ExportHelper.exportPartnersToCSV(partners, file);
                    } else {
                        return ExportHelper.exportPartnersToExcel(partners, file);
                    }
                } else {
                    throw new Exception("Ошибка загрузки: " + 
                                       (partnersResponse.getMessage() != null ? 
                                       partnersResponse.getMessage() : "Неизвестная ошибка"));
                }
            } else {
                throw new Exception("Неожиданный ответ от сервера");
            }
            
        } catch (Exception e) {
            handleError("Ошибка при экспорте партнеров", e);
            throw e;
        }
    }
    
    @Override
    public String getDataType() {
        return "partners";
    }
} 