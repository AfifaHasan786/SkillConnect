package com.skillconnect.models;

import javafx.beans.property.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import com.skillconnect.utils.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ProjectApplication {
    private final IntegerProperty projectId;
    private final IntegerProperty volunteerId;
    private final StringProperty status;
    private final ObjectProperty<Timestamp> createdAt;
    private final ObjectProperty<Timestamp> updatedAt;
    private final StringProperty selectedSkills;
    private Project project;
    private VolunteerProfile volunteer;

    // Additional properties for UI
    private final StringProperty projectTitle;
    private final StringProperty projectDescription;
    private final StringProperty volunteerName;
    private final StringProperty volunteerEmail;
    private final StringProperty volunteerPhone;
    private final StringProperty volunteerBio;
    private final StringProperty volunteerSkills;
    private final StringProperty volunteerUsername;
    private final StringProperty applicationDate;

    public ProjectApplication(int projectId, int volunteerId, String status, Timestamp createdAt,
                            Timestamp updatedAt, String selectedSkills) {
        this.projectId = new SimpleIntegerProperty(projectId);
        this.volunteerId = new SimpleIntegerProperty(volunteerId);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
        this.selectedSkills = new SimpleStringProperty(selectedSkills);

        // Initialize UI properties
        this.projectTitle = new SimpleStringProperty("");
        this.projectDescription = new SimpleStringProperty("");
        this.volunteerName = new SimpleStringProperty("");
        this.volunteerEmail = new SimpleStringProperty("");
        this.volunteerPhone = new SimpleStringProperty("");
        this.volunteerBio = new SimpleStringProperty("");
        this.volunteerSkills = new SimpleStringProperty("");
        this.volunteerUsername = new SimpleStringProperty("");
        this.applicationDate = new SimpleStringProperty("");
    }

    // Getters
    public int getProjectId() { return projectId.get(); }
    public int getVolunteerId() { return volunteerId.get(); }
    public String getStatus() { return status.get(); }
    public Timestamp getCreatedAt() { return createdAt.get(); }
    public Timestamp getUpdatedAt() { return updatedAt.get(); }
    public String getSelectedSkills() { return selectedSkills.get(); }

    // Property getters
    public IntegerProperty projectIdProperty() { return projectId; }
    public IntegerProperty volunteerIdProperty() { return volunteerId; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<Timestamp> createdAtProperty() { return createdAt; }
    public ObjectProperty<Timestamp> updatedAtProperty() { return updatedAt; }
    public StringProperty selectedSkillsProperty() { return selectedSkills; }

    // UI property getters
    public String getProjectTitle() { return projectTitle.get(); }
    public StringProperty projectTitleProperty() { return projectTitle; }
    public String getProjectDescription() { return projectDescription.get(); }
    public StringProperty projectDescriptionProperty() { return projectDescription; }
    public String getVolunteerName() { return volunteerName.get(); }
    public StringProperty volunteerNameProperty() { return volunteerName; }
    public String getVolunteerEmail() { return volunteerEmail.get(); }
    public StringProperty volunteerEmailProperty() { return volunteerEmail; }
    public String getVolunteerPhone() { return volunteerPhone.get(); }
    public StringProperty volunteerPhoneProperty() { return volunteerPhone; }
    public String getVolunteerBio() { return volunteerBio.get(); }
    public StringProperty volunteerBioProperty() { return volunteerBio; }
    public String getVolunteerSkills() { return volunteerSkills.get(); }
    public StringProperty volunteerSkillsProperty() { return volunteerSkills; }
    public String getVolunteerUsername() { return volunteerUsername.get(); }
    public StringProperty volunteerUsernameProperty() { return volunteerUsername; }
    public String getApplicationDate() { return applicationDate.get(); }
    public StringProperty applicationDateProperty() { return applicationDate; }

    // Setters for UI properties
    public void setProjectTitle(String title) { projectTitle.set(title); }
    public void setProjectDescription(String description) { projectDescription.set(description); }
    public void setVolunteerName(String name) { volunteerName.set(name); }
    public void setVolunteerEmail(String email) { volunteerEmail.set(email); }
    public void setVolunteerPhone(String phone) { volunteerPhone.set(phone); }
    public void setVolunteerBio(String bio) { volunteerBio.set(bio); }
    public void setVolunteerSkills(String skills) {
        volunteerSkills.set(skills);
    }
    public void setVolunteerSkills(List<String> skills) {
        volunteerSkills.set(String.join(",", skills));
    }
    public void setVolunteerUsername(String username) { volunteerUsername.set(username); }
    public void setVolunteerFullName(String fullName) { volunteerName.set(fullName); }
    public void setApplicationDate(String date) { applicationDate.set(date); }

    public List<String> getSelectedSkillsList() {
        String skills = selectedSkills.get();
        if (skills != null && !skills.isEmpty()) {
            return Arrays.asList(skills.split(","));
        }
        return new ArrayList<>();
    }

    public void setSelectedSkills(String skills) {
        selectedSkills.set(skills);
    }

    public void setSelectedSkills(List<String> skills) {
        if (skills != null) {
            selectedSkills.set(String.join(",", skills));
        } else {
            selectedSkills.set("");
        }
    }

    public static void apply(int projectId, int volunteerId, List<String> selectedSkills) throws SQLException {
        String appSql = "INSERT INTO project_applications (project_id, volunteer_id, status, selected_skills) " +
                       "VALUES (?, ?, 'PENDING', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(appSql)) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, volunteerId);
            stmt.setString(3, String.join(",", selectedSkills));
            stmt.executeUpdate();
        }
    }

    public static List<ProjectApplication> getVolunteerApplications(int volunteerId) throws SQLException {
        List<ProjectApplication> applications = new ArrayList<>();
        String sql = """
            SELECT pa.*, p.title as project_title, p.description as project_description
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            WHERE pa.volunteer_id = ?
            ORDER BY pa.applied_at DESC""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, volunteerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProjectApplication app = new ProjectApplication(
                    rs.getInt("project_id"),
                    rs.getInt("volunteer_id"),
                    rs.getString("status"),
                    rs.getTimestamp("applied_at"),
                    rs.getTimestamp("applied_at"),
                    rs.getString("selected_skills")
                );

                app.setProjectTitle(rs.getString("project_title"));
                app.setProjectDescription(rs.getString("project_description"));

                // Format application date
                if (app.getCreatedAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                    app.setApplicationDate(app.getCreatedAt().toLocalDateTime().format(formatter));
                }

                applications.add(app);
            }
        }

        return applications;
    }

    public void updateStatus(String newStatus) throws SQLException {
        String appSql = "UPDATE project_applications SET status = ? " +
                       "WHERE project_id = ? AND volunteer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(appSql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, getProjectId());
            stmt.setInt(3, getVolunteerId());
            stmt.executeUpdate();
            status.set(newStatus);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            setProjectTitle(project.getTitle());
            setProjectDescription(project.getDescription());
        }
    }

    public VolunteerProfile getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(VolunteerProfile volunteer) {
        this.volunteer = volunteer;
        if (volunteer != null) {
            setVolunteerFullName(volunteer.getFullName());
            setVolunteerBio(volunteer.getBio());
            setVolunteerSkills(volunteer.getSkills());
        }
    }

    public User getVolunteerUser() throws SQLException {
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

    public static List<ProjectApplication> getAdminApplications(int adminId) throws SQLException {
        List<ProjectApplication> applications = new ArrayList<>();
        String sql = """
            SELECT pa.*, p.title as project_title, p.description as project_description,
                   u.username as volunteer_name, vp.skills as volunteer_skills
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            JOIN users u ON pa.volunteer_id = u.id
            LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id
            WHERE p.admin_id = ?
            ORDER BY pa.applied_at DESC""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProjectApplication app = new ProjectApplication(
                    rs.getInt("project_id"),
                    rs.getInt("volunteer_id"),
                    rs.getString("status"),
                    rs.getTimestamp("applied_at"),
                    rs.getTimestamp("applied_at"),
                    rs.getString("selected_skills")
                );

                app.setProjectTitle(rs.getString("project_title"));
                app.setProjectDescription(rs.getString("project_description"));
                app.setVolunteerUsername(rs.getString("volunteer_name"));
                app.setVolunteerSkills(rs.getString("volunteer_skills"));

                // Format application date
                if (app.getCreatedAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                    app.setApplicationDate(app.getCreatedAt().toLocalDateTime().format(formatter));
                }

                applications.add(app);
            }
        }

        return applications;
    }
}