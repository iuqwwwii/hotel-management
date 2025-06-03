package com.hotel.client.controller;

import com.hotel.common.FetchRoomsRequest;
import com.hotel.common.FetchRoomsResponse;
import com.hotel.common.RoomDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.TilePane;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ResourceBundle;

public class GuestRoomCardController {
    @FXML private TilePane tilePane;

    @FXML
    public void initialize() {
        loadRooms();
    }

    private void loadRooms() {
        try (Socket sock = new Socket("localhost", 5555);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in  = new ObjectInputStream(sock.getInputStream())) {

            out.writeObject(new FetchRoomsRequest());
            out.flush();
            FetchRoomsResponse resp = (FetchRoomsResponse) in.readObject();
            if (resp.isSuccess()) {
                List<RoomDTO> rooms = resp.getRooms();
                for (RoomDTO room : rooms) {
                    // каждый компонент карточки — отдельный FXML
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/RoomCard.fxml"),
                            ResourceBundle.getBundle("i18n.Bundle_ru")
                    );
                    Parent card = loader.load();
                    RoomCardController ctrl = loader.getController();
                    ctrl.bind(room);
                    tilePane.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
