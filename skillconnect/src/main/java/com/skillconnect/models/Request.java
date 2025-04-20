package com.skillconnect.models;

import javafx.beans.property.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import com.skillconnect.utils.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Request {
    private final IntegerProperty id;
    private final IntegerProperty projectId;
    private final IntegerProperty volunteerId;
    private final StringProperty status;
    private final ObjectProperty<Timestamp> requestDate;

    // Additional properties for UI
    private final StringProperty projectTitle;
    private final StringProperty projectDescription;
    private final StringProperty volunteerName;
    private final StringProperty formattedDate;
    private final StringProperty volunteerSkills;

    public Request(int id, int projectId, int volunteerId, String status, Timestamp requestDate) {
        this.id = new SimpleIntegerProperty(id);
        this.projectId = new SimpleIntegerProperty(projectId);
        this.volunteerId = new SimpleIntegerProperty(volunteerId);
        this.status = new SimpleStringProperty(status);
        this.requestDate = new SimpleObjectProperty<>(requestDate);

        // Initialize UI properties
        this.projectTitle = new SimpleStringProperty("");
        this.projectDescription = new SimpleStringProperty("");
        this.volunteerName = new SimpleStringProperty("");
        this.formattedDate = new SimpleStringProperty("");
        this.volunteerSkills = new SimpleStringProperty("");

        // Format the date if available
        if (requestDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            this.formattedDate.set(requestDate.toLocalDateTime().format(formatter));
        }
    }

    // Getters
    public int getId() { return id.get(); }
    public int getProjectId() { return projectId.get(); }
    public int getVolunteerId() { return volunteerId.get(); }
    public String getStatus() { return status.get(); }
    public Timestamp getRequestDate() { return requestDate.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty projectIdProperty() { return projectId; }
    public IntegerProperty volunteerIdProperty() { return volunteerId; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<Timestamp> requestDateProperty() { return requestDate; }

    // UI property getters and setters
    public String getProjectTitle() { return projectTitle.get(); }
    public StringProperty projectTitleProperty() { return projectTitle; }
    public void setProjectTitle(String title) { projectTitle.set(title); }

    public String getProjectDescription() { return projectDescription.get(); }
    public StringProperty projectDescriptionProperty() { return projectDescription; }
    public void setProjectDescription(String description) { projectDescription.set(description); }

    public String getVolunteerName() { return volunteerName.get(); }
    public StringProperty volunteerNameProperty() { return volunteerName; }
    public void setVolunteerName(String name) { volunteerName.set(name); }

    public String getFormattedDate() { return formattedDate.get(); }
    public StringProperty formattedDateProperty() { return formattedDate; }

    public List<String> getVolunteerSkills() {
        String skills = volunteerSkills.get();
        if (skills != null && !skills.isEmpty()) {
            return Arrays.asList(skills.split(","));
        }
        return new ArrayList<>();
    }

    public void setVolunteerSkills(String skills) {
        this.volunteerSkills.set(skills);
    }

    public static List<Request> getRequestsByProject(int projectId) throws SQLException {
        List<Request> requests = new ArrayList<>();
        String sql = """
            SELECT r.*, p.title, p.description, u.username as volunteer_name,
                   vp.skills as volunteer_skills
            FROM requests r
            JOIN projects p ON r.project_id = p.id
            JOIN users u ON r.volunteer_id = u.id
            LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id
            WHERE r.project_id = ?
            ORDER BY r.request_date DESC""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Request request = new Request(
                    rs.getInt("id"),
                    rs.getInt("project_id"),
                    rs.getInt("volunteer_id"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date")
                );

                request.setProjectTitle(rs.getString("title"));
                request.setProjectDescription(rs.getString("description"));
                request.setVolunteerName(rs.getString("volunteer_name"));
                request.setVolunteerSkills(rs.getString("volunteer_skills"));

                requests.add(request);
            }
        }

        return requests;
    }

    public static List<Request> getRequestsByAdmin(int adminId) throws SQLException {
        List<Request> requests = new ArrayList<>();
        String sql = """
            SELECT r.*, p.title, p.description, u.username as volunteer_name,
                   vp.skills as volunteer_skills
            FROM requests r
            JOIN projects p ON r.project_id = p.id
            JOIN users u ON r.volunteer_id = u.id
            LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id
            WHERE p.admin_id = ?
            ORDER BY r.request_date DESC""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Request request = new Request(
                    rs.getInt("id"),
                    rs.getInt("project_id"),
                    rs.getInt("volunteer_id"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date")
                );

                request.setProjectTitle(rs.getString("title"));
                request.setProjectDescription(rs.getString("description"));
                request.setVolunteerName(rs.getString("volunteer_name"));
                request.setVolunteerSkills(rs.getString("volunteer_skills"));

                requests.add(request);
            }
        }

        return requests;
    }

    public static boolean updateRequestStatus(int requestId, String newStatus) throws SQLException {
        String sql = "UPDATE requests SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void updateStatus(String newStatus) throws SQLException {
        if (updateRequestStatus(getId(), newStatus)) {
            status.set(newStatus);
        }
    }

    public User getVolunteer() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getVolunteerId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
            }
        }
        return null;
    }
}