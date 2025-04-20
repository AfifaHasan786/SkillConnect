package com.skillconnect.services;

import com.skillconnect.models.Skill;
import com.skillconnect.models.User;
import com.skillconnect.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService {

    public boolean submitApplication(int projectId, User applicant, List<Skill> selectedSkills, String coverLetter) {
        String sql = "INSERT INTO applications (project_id, applicant_id, status, cover_letter, application_date) " +
                    "VALUES (?, ?, 'PENDING', ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, projectId);
            pstmt.setInt(2, applicant.getId());
            pstmt.setString(3, coverLetter);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int applicationId = generatedKeys.getInt(1);
                    return insertApplicationSkills(applicationId, selectedSkills);
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertApplicationSkills(int applicationId, List<Skill> skills) {
        String sql = "INSERT INTO application_skills (application_id, skill_id) VALUES (?, ?)";

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