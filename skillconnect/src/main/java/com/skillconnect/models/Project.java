package com.skillconnect.models;

import com.skillconnect.utils.DatabaseConnection;
import javafx.beans.property.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty requiredSkills;
    private final StringProperty status;
    private final IntegerProperty adminId;
    private final ObjectProperty<Timestamp> createdAt;
    private final StringProperty adminName;

    public Project(int id, String title, String description, String requiredSkills,
                  String status, int adminId, Timestamp createdAt) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.requiredSkills = new SimpleStringProperty(requiredSkills);
        this.status = new SimpleStringProperty(status);
        this.adminId = new SimpleIntegerProperty(adminId);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.adminName = new SimpleStringProperty();
    }

    // Getters
    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getRequiredSkills() { return requiredSkills.get(); }
    public String getStatus() { return status.get(); }
    public int getAdminId() { return adminId.get(); }
    public Timestamp getCreatedAt() { return createdAt.get(); }
    public String getAdminName() { return adminName.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty requiredSkillsProperty() { return requiredSkills; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty adminIdProperty() { return adminId; }
    public ObjectProperty<Timestamp> createdAtProperty() { return createdAt; }
    public StringProperty adminNameProperty() { return adminName; }

    public static Project getById(int projectId) {
        String query = "SELECT p.*, u.username as admin_name FROM projects p " +
                      "LEFT JOIN users u ON p.admin_id = u.id " +
                      "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Project project = new Project(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("required_skills"),
                    rs.getString("status"),
                    rs.getInt("admin_id"),
                    rs.getTimestamp("created_at")
                );
                project.adminNameProperty().set(rs.getString("admin_name"));
                return project;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> getAllProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM projects ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projects.add(new Project(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("required_skills"),
                    rs.getString("status"),
                    rs.getInt("admin_id"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return projects;
    }

    public static List<Project> getProjectsByAdmin(int adminId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM projects WHERE admin_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(new Project(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("required_skills"),
                        rs.getString("status"),
                        rs.getInt("admin_id"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return projects;
    }

    public static void createProject(String title, String description, String requiredSkills, int adminId) throws SQLException {
        String query = "INSERT INTO projects (title, description, required_skills, status, admin_id) VALUES (?, ?, ?, 'OPEN', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, requiredSkills);
            stmt.setInt(4, adminId);
            stmt.executeUpdate();
        }
    }

    public boolean hasVolunteerApplied(int volunteerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM project_applications WHERE project_id = ? AND volunteer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, getId());
            stmt.setInt(2, volunteerId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void applyForProject(int volunteerId) throws SQLException {
        // Create application
        ProjectApplication.apply(getId(), volunteerId, new ArrayList<>());
    }

    public List<ProjectApplication> getApplications() {
        String query = """
            SELECT pa.project_id, pa.volunteer_id, pa.status, pa.applied_at,
                   u.username, vp.full_name, vp.email, vp.phone, vp.bio, vp.skills
            FROM project_applications pa
            JOIN users u ON pa.volunteer_id = u.id
            LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id
            WHERE pa.project_id = ?
            ORDER BY pa.applied_at DESC
        """;

        List<ProjectApplication> applications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProjectApplication app = new ProjectApplication(
                    rs.getInt("project_id"),
                    rs.getInt("volunteer_id"),
                    rs.getString("status"),
                    rs.getTimestamp("applied_at"),
                    rs.getTimestamp("applied_at"),
                    "" // selectedSkills is empty since we're getting it from the skills table
                );
                app.setVolunteerUsername(rs.getString("username"));
                app.setVolunteerFullName(rs.getString("full_name"));
                app.volunteerEmailProperty().set(rs.getString("email"));
                app.volunteerPhoneProperty().set(rs.getString("phone"));
                app.volunteerBioProperty().set(rs.getString("bio"));
                app.volunteerSkillsProperty().set(rs.getString("skills"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public void updateStatus(String newStatus) throws SQLException {
        String query = "UPDATE projects SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, getId());

            if (stmt.executeUpdate() > 0) {
                this.status.set(newStatus);
            }
        }
    }

    public static List<Project> getActiveProjects(int userId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String query = """
            SELECT p.*, u.username as admin_name
            FROM projects p
            LEFT JOIN users u ON p.admin_id = u.id
            WHERE p.status = 'ACTIVE' AND (p.admin_id = ? OR EXISTS (
                SELECT 1 FROM project_applications pa
                WHERE pa.project_id = p.id
                AND pa.volunteer_id = ?
                AND pa.status = 'ACCEPTED'
            ))
            ORDER BY p.created_at DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("required_skills"),
                        rs.getString("status"),
                        rs.getInt("admin_id"),
                        rs.getTimestamp("created_at")
                    );
                    project.adminNameProperty().set(rs.getString("admin_name"));
                    projects.add(project);
                }
            }
        }
        return projects;
    }
}


