package com.skillconnect.models;

import com.skillconnect.utils.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.beans.property.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty role;
    private final StringProperty email;
    private final StringProperty phone;
    private List<String> skills;

    public User(int id, String username, String password, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.skills = new ArrayList<>();
        this.email = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
    }

    // Constructor for creating User objects without password (for display purposes)
    public User(int id, String username, String role) {
        this(id, username, "", role);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getRole() { return role.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public List<String> getSkills() throws SQLException {
        if (skills.isEmpty()) {
            loadSkills();
        }
        return skills;
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty roleProperty() { return role; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }

    // Database operations
    public static User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                    user.loadSkills();
                    return user;
                }
            }
        }
        return null;
    }

    public static User authenticate(String username, String password, String role) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                    user.loadProfile();
                    return user;
                }
            }
        }
        return null;
    }

    public static void register(String username, String password, String role) throws SQLException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            stmt.executeUpdate();
        }
    }

    public static boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void loadSkills() throws SQLException {
        String query = "SELECT s.name FROM skills s " +
                      "JOIN user_skills us ON s.id = us.skill_id " +
                      "WHERE us.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, this.id.get());

            try (ResultSet rs = stmt.executeQuery()) {
                skills.clear();
                while (rs.next()) {
                    skills.add(rs.getString("name"));
                }
            }
        }
    }

    private void loadProfile() throws SQLException {
        String query = "SELECT email, phone FROM volunteer_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, this.id.get());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.email.set(rs.getString("email"));
                    this.phone.set(rs.getString("phone"));
                } else {
                    // Create empty profile if it doesn't exist
                    String insertQuery = "INSERT INTO volunteer_profiles (user_id) VALUES (?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, this.id.get());
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
        loadSkills();
    }

    public boolean addSkill(String skillName) throws SQLException {
        String query = "INSERT INTO user_skills (user_id, skill_id) " +
                      "SELECT ?, id FROM skills WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, this.id.get());
            stmt.setString(2, skillName);

            return stmt.executeUpdate() > 0;
        }
    }

    public void updateProfile(String email, String phone, ObservableList<String> skills) throws SQLException {
        // Update profile information
        String profileSql = "INSERT INTO volunteer_profiles (user_id, email, phone) VALUES (?, ?, ?) " +
                          "ON DUPLICATE KEY UPDATE email = VALUES(email), phone = VALUES(phone)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(profileSql)) {
            stmt.setInt(1, id.get());
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.executeUpdate();
        }

        // Update skills
        String deleteSql = "DELETE FROM user_skills WHERE user_id = ?";
        String insertSql = "INSERT INTO user_skills (user_id, skill_id) " +
                         "SELECT ?, id FROM skills WHERE name = ? " +
                         "ON DUPLICATE KEY UPDATE skill_id = VALUES(skill_id)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Delete existing skills
            deleteStmt.setInt(1, id.get());
            deleteStmt.executeUpdate();

            // Insert new skills
            for (String skill : skills) {
                // First ensure the skill exists in the skills table
                ensureSkillExists(skill);

                // Then add it to user_skills
                insertStmt.setInt(1, id.get());
                insertStmt.setString(2, skill);
                insertStmt.executeUpdate();
            }
        }

        // Update local data
        this.email.set(email);
        this.phone.set(phone);
        this.skills.clear();
        this.skills.addAll(skills);
    }

    private void ensureSkillExists(String skillName) throws SQLException {
        String sql = "INSERT IGNORE INTO skills (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, skillName);
            stmt.executeUpdate();
        }
    }

    public static List<User> searchVolunteers(String searchTerm) throws SQLException {
        List<User> volunteers = new ArrayList<>();
        String query = """
            SELECT id, username, role
            FROM users
            WHERE role = 'VOLUNTEER'
            AND username LIKE ?
            ORDER BY username
            LIMIT 10
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User volunteer = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                    volunteers.add(volunteer);
                }
            }
        }
        return volunteers;
    }

    public static List<User> searchAdmins(String searchTerm) throws SQLException {
        List<User> admins = new ArrayList<>();
        String query = """
            SELECT id, username, role
            FROM users
            WHERE role = 'ADMIN'
            AND username LIKE ?
            ORDER BY username
            LIMIT 10
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User admin = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                    admins.add(admin);
                }
            }
        }
        return admins;
    }
}