package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTextArea;
import com.skillconnect.models.User;
import com.skillconnect.models.VolunteerProfile;
import com.skillconnect.utils.AlertHelper;
import com.skillconnect.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VolunteerProfileController implements Initializable {

    @FXML private JFXTextField fullNameField;
    @FXML private JFXTextField emailField;
    @FXML private JFXTextField phoneField;
    @FXML private JFXTextArea bioField;
    @FXML private JFXComboBox<String> skillsComboBox;
    @FXML private JFXTextField customSkillField;
    @FXML private JFXButton addCustomSkillButton;
    @FXML private FlowPane skillsContainer;
    @FXML private JFXButton saveButton;

    private User currentUser;
    private VolunteerProfile profile;
    private StringBuilder skillsBuilder = new StringBuilder();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAvailableSkills();
        setupSkillsComboBox();
        setupCustomSkillButton();
        setupSaveButton();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadProfile();
    }

    private void loadAvailableSkills() {
        try {
            List<String> skills = VolunteerProfile.getAllSkills();
            skillsComboBox.getItems().setAll(skills);
        } catch (SQLException e) {
            AlertHelper.showErrorAlert("Error", "Failed to load skills", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSkillsComboBox() {
        skillsComboBox.setOnAction(e -> {
            String selectedSkill = skillsComboBox.getValue();
            if (selectedSkill != null && !selectedSkill.isEmpty()) {
                addSkillToContainer(selectedSkill);
                skillsComboBox.setValue(null);
            }
        });
    }

    private void setupCustomSkillButton() {
        addCustomSkillButton.setOnAction(e -> {
            String customSkill = customSkillField.getText().trim();
            if (!customSkill.isEmpty()) {
                try {
                    ensureSkillExists(customSkill);
                    addSkillToContainer(customSkill);
                    customSkillField.clear();
                } catch (SQLException ex) {
                    AlertHelper.showErrorAlert("Error", "Failed to add custom skill", ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void ensureSkillExists(String skillName) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM skills WHERE name = ?";
        String insertQuery = "INSERT INTO skills (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // First check if skill exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, skillName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Skill doesn't exist, insert it
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, skillName);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private void addSkillToContainer(String skill) {
        // Check if skill already exists
        for (var node : skillsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox skillBox = (HBox) node;
                Label skillLabel = (Label) skillBox.getChildren().get(0);
                if (skillLabel.getText().equals(skill)) {
                    return; // Skill already exists
                }
            }
        }

        if (skillsBuilder.length() > 0) {
            skillsBuilder.append(",");
        }
        skillsBuilder.append(skill);

        HBox skillBox = new HBox(5);
        skillBox.getStyleClass().add("skill-box");

        Label skillLabel = new Label(skill);
        FontAwesomeIconView removeIcon = new FontAwesomeIconView();
        removeIcon.setGlyphName("TIMES");
        removeIcon.setStyleClass("remove-icon");

        JFXButton removeButton = new JFXButton("", removeIcon);
        removeButton.setOnAction(e -> {
            skillsContainer.getChildren().remove(skillBox);
            updateSkillsBuilder();
        });

        skillBox.getChildren().addAll(skillLabel, removeButton);
        skillsContainer.getChildren().add(skillBox);
    }

    private void updateSkillsBuilder() {
        skillsBuilder.setLength(0);
        skillsContainer.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox skillBox = (HBox) node;
                Label skillLabel = (Label) skillBox.getChildren().get(0);
                if (skillsBuilder.length() > 0) {
                    skillsBuilder.append(",");
                }
                skillsBuilder.append(skillLabel.getText());
            }
        });
    }

    private void loadProfile() {
        try {
            profile = VolunteerProfile.getByUserId(currentUser.getId());
            if (profile != null) {
                fullNameField.setText(profile.getFullName());
                emailField.setText(profile.getEmail());
                phoneField.setText(profile.getPhone());
                bioField.setText(profile.getBio());

                // Load skills
                String skills = profile.getSkills();
                if (skills != null && !skills.isEmpty()) {
                    skillsContainer.getChildren().clear();
                    Arrays.stream(skills.split(","))
                          .map(String::trim)
                          .forEach(this::addSkillToContainer);
                }
            } else {
                profile = new VolunteerProfile();
                profile.setUserId(currentUser.getId());
            }
        } catch (SQLException e) {
            AlertHelper.showErrorAlert("Error", "Failed to load profile", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSaveButton() {
        saveButton.setOnAction(e -> saveProfile());
    }

    private void saveProfile() {
        try {
            profile.setFullName(fullNameField.getText());
            profile.setEmail(emailField.getText());
            profile.setPhone(phoneField.getText());
            profile.setBio(bioField.getText());
            profile.setSkills(skillsBuilder.toString());

            profile.save();
            AlertHelper.showInformationAlert("Success", "Profile saved successfully", "Your profile has been updated.");
        } catch (SQLException ex) {
            AlertHelper.showErrorAlert("Error", "Failed to save profile", ex.getMessage());
            ex.printStackTrace();
        }
    }
}