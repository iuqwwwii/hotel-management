package com.hotel.server;

import com.hotel.server.network.ClientHandler;
import com.hotel.server.util.ConfigManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication extends Application {
    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        logArea = new TextArea();
        logArea.setEditable(false);
        BorderPane root = new BorderPane(logArea);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Hotel Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::startServer).start();
    }

    private void startServer() {
        int port = ConfigManager.getInstance().getServerPort();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Client connected: " + clientSocket.getRemoteSocketAddress());
                new Thread(new ClientHandler(clientSocket, this::log)).start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    private void log(String message) {
        // Append log to UI thread
        javafx.application.Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    public static void main(String[] args) {
        // Load config first
        ConfigManager.init("config.properties");
        launch(args);
    }
}