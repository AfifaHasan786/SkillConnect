package com.skillconnect.models;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.skillconnect.utils.DatabaseConnection;

public class Message {
    private Long id;
    private String content;
    private User sender;
    private User receiver;
    private LocalDateTime timestamp;

    public Message(Long id, String content, User sender, User receiver, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Additional methods needed by ChatController
    public int getSenderId() {
        return sender.getId();
    }

    public String getSenderName() {
        return sender.getUsername();
    }

    // Database operations
    public static List<Message> getConversation(int userId1, int userId2) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = """
            SELECT m.id, m.content, m.timestamp,
                   s.id as sender_id, s.username as sender_username, s.role as sender_role,
                   r.id as receiver_id, r.username as receiver_username, r.role as receiver_role
            FROM messages m
            JOIN users s ON m.sender_id = s.id
            JOIN users r ON m.receiver_id = r.id
            WHERE (m.sender_id = ? AND m.receiver_id = ?)
               OR (m.sender_id = ? AND m.receiver_id = ?)
            ORDER BY m.timestamp ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User sender = new User(
                        rs.getInt("sender_id"),
                        rs.getString("sender_username"),
                        rs.getString("sender_role")
                    );
                    User receiver = new User(
                        rs.getInt("receiver_id"),
                        rs.getString("receiver_username"),
                        rs.getString("receiver_role")
                    );
                    messages.add(new Message(
                        rs.getLong("id"),
                        rs.getString("content"),
                        sender,
                        receiver,
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    ));
                }
            }
        }
        return messages;
    }

    public static void sendMessage(int senderId, int receiverId, String content) throws SQLException {
        String query = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);
            stmt.executeUpdate();
        }
    }

    public void save() throws SQLException {
        String query = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, sender.getId());
            stmt.setInt(2, receiver.getId());
            stmt.setString(3, content);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        }
    }
}