package com.skillconnect.models;

import com.skillconnect.utils.DatabaseConnection;
import javafx.beans.property.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private final IntegerProperty id;
    private final IntegerProperty senderId;
    private final IntegerProperty receiverId;
    private final StringProperty message;
    private final ObjectProperty<Timestamp> sentAt;
    private final ObjectProperty<Timestamp> readAt;
    private final StringProperty senderName;
    private final StringProperty receiverName;

    public ChatMessage(int id, int senderId, int receiverId, String message,
                      Timestamp sentAt, Timestamp readAt, String senderName, String receiverName) {
        this.id = new SimpleIntegerProperty(id);
        this.senderId = new SimpleIntegerProperty(senderId);
        this.receiverId = new SimpleIntegerProperty(receiverId);
        this.message = new SimpleStringProperty(message);
        this.sentAt = new SimpleObjectProperty<>(sentAt);
        this.readAt = new SimpleObjectProperty<>(readAt);
        this.senderName = new SimpleStringProperty(senderName);
        this.receiverName = new SimpleStringProperty(receiverName);
    }

    // Getters
    public int getId() { return id.get(); }
    public int getSenderId() { return senderId.get(); }
    public int getReceiverId() { return receiverId.get(); }
    public String getMessage() { return message.get(); }
    public Timestamp getSentAt() { return sentAt.get(); }
    public Timestamp getReadAt() { return readAt.get(); }
    public String getSenderName() { return senderName.get(); }
    public String getReceiverName() { return receiverName.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty senderIdProperty() { return senderId; }
    public IntegerProperty receiverIdProperty() { return receiverId; }
    public StringProperty messageProperty() { return message; }
    public ObjectProperty<Timestamp> sentAtProperty() { return sentAt; }
    public ObjectProperty<Timestamp> readAtProperty() { return readAt; }
    public StringProperty senderNameProperty() { return senderName; }
    public StringProperty receiverNameProperty() { return receiverName; }

    // Database operations
    public static List<ChatMessage> getConversation(int userId1, int userId2) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT m.*, " +
                      "s.username as sender_name, " +
                      "r.username as receiver_name " +
                      "FROM chat_messages m " +
                      "JOIN users s ON m.sender_id = s.id " +
                      "JOIN users r ON m.receiver_id = r.id " +
                      "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                      "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                      "ORDER BY m.sent_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                    rs.getInt("id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("message"),
                    rs.getTimestamp("sent_at"),
                    rs.getTimestamp("read_at"),
                    rs.getString("sender_name"),
                    rs.getString("receiver_name")
                ));
            }
        }
        return messages;
    }

    public static void sendMessage(int senderId, int receiverId, String message) throws SQLException {
        String query = "INSERT INTO chat_messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, message);
            stmt.executeUpdate();
        }
    }

    public void markAsRead() throws SQLException {
        String query = "UPDATE chat_messages SET read_at = CURRENT_TIMESTAMP WHERE id = ? AND read_at IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, getId());
            if (stmt.executeUpdate() > 0) {
                readAt.set(new Timestamp(System.currentTimeMillis()));
            }
        }
    }

    public static List<User> getChatPartners(int userId) throws SQLException {
        List<User> partners = new ArrayList<>();
        String query = "SELECT DISTINCT u.* FROM users u " +
                      "JOIN chat_messages m ON (m.sender_id = u.id OR m.receiver_id = u.id) " +
                      "WHERE (m.sender_id = ? OR m.receiver_id = ?) " +
                      "AND u.id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                partners.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                ));
            }
        }
        return partners;
    }

    public static List<User> getAdminChatPartners(int volunteerId) throws SQLException {
        List<User> admins = new ArrayList<>();
        String query = "SELECT DISTINCT u.* FROM users u " +
                      "JOIN projects p ON u.id = p.admin_id " +
                      "JOIN project_applications a ON p.id = a.project_id " +
                      "WHERE a.volunteer_id = ? AND u.role = 'ADMIN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, volunteerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                admins.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                ));
            }
        }
        return admins;
    }

    public static List<User> getVolunteerChatPartners(int adminId) throws SQLException {
        List<User> volunteers = new ArrayList<>();
        String query = "SELECT DISTINCT u.* FROM users u " +
                      "JOIN project_applications a ON u.id = a.volunteer_id " +
                      "JOIN projects p ON a.project_id = p.id " +
                      "WHERE p.admin_id = ? AND u.role = 'VOLUNTEER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                volunteers.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                ));
            }
        }
        return volunteers;
    }
}