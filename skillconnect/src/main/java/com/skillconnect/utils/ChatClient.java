package com.skillconnect.utils;

import com.skillconnect.controllers.ChatController;
import com.skillconnect.models.Message;
import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class ChatClient implements Runnable {
    private Socket socket;
    private ChatController controller;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatClient(Socket socket, ChatController controller) {
        this.socket = socket;
        this.controller = controller;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert(
                "Connection Error",
                null,
                "Failed to establish chat connection: " + e.getMessage()
            );
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                String[] parts = message.split("\\|");
                if (parts.length >= 5) {
                    Long id = Long.parseLong(parts[0]);
                    int senderId = Integer.parseInt(parts[1]);
                    String senderName = parts[2];
                    int receiverId = Integer.parseInt(parts[3]);
                    String content = parts[4];

                    // Create User objects for sender and receiver
                    User sender = new User(senderId, senderName, "volunteer");
                    User receiver = new User(receiverId, "Recipient", "volunteer");

                    Message chatMessage = new Message(
                        id,
                        content,
                        sender,
                        receiver,
                        LocalDateTime.now()
                    );

                    // Trigger a conversation reload to show the new message
                    Platform.runLater(() -> controller.loadConversation());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() ->
                AlertUtils.showErrorAlert(
                    "Connection Error",
                    null,
                    "Lost connection to chat server: " + e.getMessage()
                )
            );
        }
    }

    public void sendMessage(Message message) {
        String messageString = String.format("%d|%d|%s|%d|%s",
            message.getId(),
            message.getSender().getId(),
            message.getSender().getUsername(),
            message.getReceiver().getId(),
            message.getContent()
        );
        writer.println(messageString);
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert(
                "Connection Error",
                null,
                "Error closing chat connection: " + e.getMessage()
            );
        }
    }
}