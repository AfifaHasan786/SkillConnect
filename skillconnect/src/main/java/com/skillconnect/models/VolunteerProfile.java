package com.skillconnect.models;

import com.skillconnect.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VolunteerProfile {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private String skills;

    public VolunteerProfile() {
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public static List<String> getAllSkills() throws SQLException {
        List<String> skills = new ArrayList<>();
        String query = "SELECT name FROM skills ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(rs.getString("name"));
            }
        }
        return skills;
    }

    public static VolunteerProfile getByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM volunteer_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                VolunteerProfile profile = new VolunteerProfile();
                profile.setUserId(userId);
                profile.setFullName(rs.getString("full_name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhone(rs.getString("phone"));
                profile.setBio(rs.getString("bio"));
                profile.setSkills(rs.getString("skills"));
                return profile;
            }
        }
        return null;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO volunteer_profiles (user_id, full_name, email, phone, bio, skills) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "full_name = VALUES(full_name), " +
                    "email = VALUES(email), " +
                    "phone = VALUES(phone), " +
                    "bio = VALUES(bio), " +
                    "skills = VALUES(skills)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, bio);
            stmt.setString(6, skills);

            stmt.executeUpdate();
        }
    }
}