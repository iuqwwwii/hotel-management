package com.hotel.client.util;

import com.hotel.common.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Утилитарный класс для экспорта данных в CSV и Excel
 */
public class ExportHelper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String CSV_SEPARATOR = ";";
    
    /**
     * Экспортирует список партнеров в CSV файл
     * @param partners список партнеров
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportPartnersToCSV(List<PartnerDTO> partners, File file) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            // Заголовок
            writer.println("ID;Имя;Контактная информация;Количество бронирований;Выручка");
            
            // Данные
            for (PartnerDTO partner : partners) {
                writer.println(
                    partner.getPartnerId() + CSV_SEPARATOR +
                    escapeCSV(partner.getName()) + CSV_SEPARATOR +
                    escapeCSV(partner.getContactInfo()) + CSV_SEPARATOR +
                    partner.getBookingsCount() + CSV_SEPARATOR +
                    formatDouble(partner.getTotalRevenue())
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте партнеров в CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список партнеров в Excel файл
     * @param partners список партнеров
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportPartnersToExcel(List<PartnerDTO> partners, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем лист
            Sheet sheet = workbook.createSheet("Партнеры");
            
            // Создаем стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook);
            
            // Заголовок
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Имя", "Контактная информация", "Количество бронирований", "Выручка"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 1;
            for (PartnerDTO partner : partners) {
                Row row = sheet.createRow(rowNum++);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(partner.getPartnerId());
                idCell.setCellStyle(numericStyle);
                
                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(partner.getName());
                nameCell.setCellStyle(dataStyle);
                
                Cell contactCell = row.createCell(2);
                contactCell.setCellValue(partner.getContactInfo());
                contactCell.setCellStyle(dataStyle);
                
                Cell bookingsCell = row.createCell(3);
                bookingsCell.setCellValue(partner.getBookingsCount());
                bookingsCell.setCellStyle(numericStyle);
                
                Cell revenueCell = row.createCell(4);
                revenueCell.setCellValue(partner.getTotalRevenue());
                revenueCell.setCellStyle(numericStyle);
            }
            
            // Автоподбор ширины столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Записываем результат
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте партнеров в Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список бронирований в CSV файл
     * @param bookings список бронирований
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportBookingsToCSV(List<BookingDTO> bookings, File file) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            // Заголовок
            writer.println("ID;Номер комнаты;Дата заезда;Дата выезда;Статус;Стоимость");
            
            // Данные
            for (BookingDTO booking : bookings) {
                writer.println(
                    booking.getBookingId() + CSV_SEPARATOR +
                    booking.getRoomNumber() + CSV_SEPARATOR +
                    booking.getStartDate().format(DATE_FORMATTER) + CSV_SEPARATOR +
                    booking.getEndDate().format(DATE_FORMATTER) + CSV_SEPARATOR +
                    escapeCSV(getStatusTranslation(booking.getStatus())) + CSV_SEPARATOR +
                    formatDouble(booking.getTotalCost())
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте бронирований в CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список бронирований в Excel файл
     * @param bookings список бронирований
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportBookingsToExcel(List<BookingDTO> bookings, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем лист
            Sheet sheet = workbook.createSheet("Бронирования");
            
            // Создаем стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Заголовок
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Номер комнаты", "Дата заезда", "Дата выезда", "Статус", "Стоимость"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 1;
            for (BookingDTO booking : bookings) {
                Row row = sheet.createRow(rowNum++);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(booking.getBookingId());
                idCell.setCellStyle(numericStyle);
                
                Cell roomCell = row.createCell(1);
                roomCell.setCellValue(booking.getRoomNumber());
                roomCell.setCellStyle(dataStyle);
                
                Cell startDateCell = row.createCell(2);
                startDateCell.setCellValue(booking.getStartDate().format(DATE_FORMATTER));
                startDateCell.setCellStyle(dateStyle);
                
                Cell endDateCell = row.createCell(3);
                endDateCell.setCellValue(booking.getEndDate().format(DATE_FORMATTER));
                endDateCell.setCellStyle(dateStyle);
                
                Cell statusCell = row.createCell(4);
                statusCell.setCellValue(getStatusTranslation(booking.getStatus()));
                statusCell.setCellStyle(dataStyle);
                
                Cell costCell = row.createCell(5);
                costCell.setCellValue(booking.getTotalCost());
                costCell.setCellStyle(numericStyle);
            }
            
            // Автоподбор ширины столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Записываем результат
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте бронирований в Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список комнат в CSV файл
     * @param rooms список комнат
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportRoomsToCSV(List<RoomDTO> rooms, File file) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            // Заголовок
            writer.println("ID;Номер;Тип;Статус;Базовая цена");
            
            // Данные
            for (RoomDTO room : rooms) {
                writer.println(
                    room.getRoomId() + CSV_SEPARATOR +
                    room.getNumber() + CSV_SEPARATOR +
                    escapeCSV(room.getTypeName()) + CSV_SEPARATOR +
                    escapeCSV(getStatusTranslation(room.getStatus())) + CSV_SEPARATOR +
                    formatDouble(room.getBasePrice())
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте комнат в CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список комнат в Excel файл
     * @param rooms список комнат
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportRoomsToExcel(List<RoomDTO> rooms, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем лист
            Sheet sheet = workbook.createSheet("Номера");
            
            // Создаем стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook);
            
            // Заголовок
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Номер", "Тип", "Статус", "Базовая цена"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 1;
            for (RoomDTO room : rooms) {
                Row row = sheet.createRow(rowNum++);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(room.getRoomId());
                idCell.setCellStyle(numericStyle);
                
                Cell numberCell = row.createCell(1);
                numberCell.setCellValue(room.getNumber());
                numberCell.setCellStyle(dataStyle);
                
                Cell typeCell = row.createCell(2);
                typeCell.setCellValue(room.getTypeName());
                typeCell.setCellStyle(dataStyle);
                
                Cell statusCell = row.createCell(3);
                statusCell.setCellValue(getStatusTranslation(room.getStatus()));
                statusCell.setCellStyle(dataStyle);
                
                Cell priceCell = row.createCell(4);
                priceCell.setCellValue(room.getBasePrice());
                priceCell.setCellStyle(numericStyle);
            }
            
            // Автоподбор ширины столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Записываем результат
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте комнат в Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список отзывов в CSV файл
     * @param reviews список отзывов
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportReviewsToCSV(List<ReviewDTO> reviews, File file) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            // Заголовок
            writer.println("ID;Пользователь ID;Номер комнаты;Оценка;Дата;Комментарий");
            
            // Данные
            for (ReviewDTO review : reviews) {
                writer.println(
                    review.getReviewId() + CSV_SEPARATOR +
                    review.getUserId() + CSV_SEPARATOR +
                    review.getRoomNumber() + CSV_SEPARATOR +
                    review.getRating() + CSV_SEPARATOR +
                    review.getReviewDate().format(DATE_FORMATTER) + CSV_SEPARATOR +
                    escapeCSV(review.getComment())
                );
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте отзывов в CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует список отзывов в Excel файл
     * @param reviews список отзывов
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportReviewsToExcel(List<ReviewDTO> reviews, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем лист
            Sheet sheet = workbook.createSheet("Отзывы");
            
            // Создаем стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Заголовок
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Пользователь ID", "Номер комнаты", "Оценка", "Дата", "Комментарий"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 1;
            for (ReviewDTO review : reviews) {
                Row row = sheet.createRow(rowNum++);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(review.getReviewId());
                idCell.setCellStyle(numericStyle);
                
                Cell userIdCell = row.createCell(1);
                userIdCell.setCellValue(review.getUserId());
                userIdCell.setCellStyle(numericStyle);
                
                Cell roomNumberCell = row.createCell(2);
                roomNumberCell.setCellValue(review.getRoomNumber());
                roomNumberCell.setCellStyle(dataStyle);
                
                Cell ratingCell = row.createCell(3);
                ratingCell.setCellValue(review.getRating());
                ratingCell.setCellStyle(numericStyle);
                
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(review.getReviewDate().format(DATE_FORMATTER));
                dateCell.setCellStyle(dateStyle);
                
                Cell commentCell = row.createCell(5);
                commentCell.setCellValue(review.getComment());
                commentCell.setCellStyle(dataStyle);
            }
            
            // Автоподбор ширины столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Записываем результат
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте отзывов в Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует общую статистику в CSV файл
     * @param stats статистика
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportStatisticsToCSV(Map<String, Object> stats, File file) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            // Данные
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                String value = (entry.getValue() != null) ? entry.getValue().toString() : "Н/Д";
                writer.println(escapeCSV(entry.getKey()) + CSV_SEPARATOR + escapeCSV(value));
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте статистики в CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Экспортирует общую статистику в Excel файл
     * @param stats статистика
     * @param file файл для экспорта
     * @return true если экспорт успешен
     */
    public static boolean exportStatisticsToExcel(Map<String, Object> stats, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Создаем лист
            Sheet sheet = workbook.createSheet("Статистика");
            
            // Создаем стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Заголовок
            Row headerRow = sheet.createRow(0);
            Cell keyCell = headerRow.createCell(0);
            keyCell.setCellValue("Показатель");
            keyCell.setCellStyle(headerStyle);
            
            Cell valueCell = headerRow.createCell(1);
            valueCell.setCellValue("Значение");
            valueCell.setCellStyle(headerStyle);
            
            // Данные
            int rowNum = 1;
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(entry.getKey());
                nameCell.setCellStyle(dataStyle);
                
                Cell statValueCell = row.createCell(1);
                String value = (entry.getValue() != null) ? entry.getValue().toString() : "Н/Д";
                statValueCell.setCellValue(value);
                statValueCell.setCellStyle(dataStyle);
            }
            
            // Автоподбор ширины столбцов
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            // Записываем результат
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте статистики в Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Создает стиль для заголовков таблицы Excel
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Создает стиль для текстовых данных таблицы Excel
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Создает стиль для числовых данных таблицы Excel
     */
    private static CellStyle createNumericStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }
    
    /**
     * Создает стиль для дат в таблице Excel
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Переводит статус на русский язык
     */
    private static String getStatusTranslation(String status) {
        if (status == null) return "";
        
        switch (status) {
            case "NEW": return "Новый";
            case "CONFIRMED": return "Подтвержден";
            case "CHECKED_IN": return "Заселен";
            case "CHECKED_OUT": return "Выселен";
            case "CANCELLED": return "Отменен";
            case "AVAILABLE": return "Доступен";
            case "OCCUPIED": return "Занят";
            case "MAINTENANCE": return "На обслуживании";
            default: return status;
        }
    }
    
    /**
     * Экранирует строку для CSV
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        
        // Если строка содержит разделитель, перенос строки или кавычки, заключаем её в кавычки
        if (value.contains(CSV_SEPARATOR) || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Форматирует число с двумя знаками после запятой
     */
    private static String formatDouble(double value) {
        return String.format("%.2f", value).replace(".", ",");
    }
} 