// server/src/main/java/com/hotel/server/service/RoomService.java
package com.hotel.server.service;

import com.hotel.common.RoomDTO;
import com.hotel.common.UpdateRoomRequest;
import com.hotel.server.dao.RoomDao;
import com.hotel.server.model.Room;

import java.util.List;
import java.util.stream.Collectors;

public class RoomService {
    private final RoomDao roomDao = new RoomDao();


    public List<RoomDTO> getAllRooms() {
        List<Room> rooms = roomDao.findAll();
        return rooms.stream()
                .map(r -> new RoomDTO(
                        r.getRoomId(),
                        r.getNumber(),
                        r.getStatus(),
                        r.getTypeName(),
                        r.getBasePrice(),
                        r.getDescription()))
                .collect(Collectors.toList());
    }
    
    public boolean deleteRoom(int roomId) {
        try {
            // Проверяем, существует ли номер с указанным id
            Room room = roomDao.findById(roomId);
            if (room == null) {
                return false;
            }
            
            // Удаляем номер
            return roomDao.delete(roomId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public RoomDTO updateRoom(UpdateRoomRequest request) {
        try {
            if (request.isNewRoom()) {
                // Создание нового номера
                int newRoomId = roomDao.create(
                    request.getNumber(),
                    request.getTypeName(),
                    request.getStatus(),
                    request.getBasePrice(),
                    request.getDescription()
                );
                
                if (newRoomId > 0) {
                    return new RoomDTO(
                        newRoomId,
                        request.getNumber(),
                        request.getStatus(),
                        request.getTypeName(),
                        request.getBasePrice(),
                        request.getDescription()
                    );
                }
            } else {
                // Обновление существующего номера
                boolean updated = roomDao.update(
                    request.getRoomId(),
                    request.getNumber(),
                    request.getTypeName(),
                    request.getStatus(),
                    request.getBasePrice(),
                    request.getDescription()
                );
                
                if (updated) {
                    return new RoomDTO(
                        request.getRoomId(),
                        request.getNumber(),
                        request.getStatus(),
                        request.getTypeName(),
                        request.getBasePrice(),
                        request.getDescription()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

