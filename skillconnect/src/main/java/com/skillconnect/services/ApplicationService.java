package com.skillconnect.services;

import com.skillconnect.models.Skill;
import com.skillconnect.models.User;
import com.skillconnect.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationService {

    public boolean submitApplication(int projectId, User applicant, List<Skill> selectedSkills, String coverLetter) {
        String sql = "INSERT INTO project_applications (project_id, volunteer_id, status, selected_skills, applied_at) " +
                    "VALUES (?, ?, 'PENDING', ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, applicant.getId());

            // Convert skills list to comma-separated string
            String skillsString = selectedSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(","));
            pstmt.setString(3, skillsString);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertApplicationSkills(int applicationId, List<Skill> skills) {
        String sql = "INSERT INTO project_application_skills (application_id, skill_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Skill skill : skills) {
                pstmt.setInt(1, applicationId);
                pstmt.setInt(2, skill.getId());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            return results.length == skills.size();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}