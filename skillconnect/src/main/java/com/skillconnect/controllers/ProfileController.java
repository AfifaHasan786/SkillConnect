package com.skillconnect.controllers;

import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import com.skillconnect.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.sql.SQLException;

public class ProfileController {
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField skillField;
    @FXML private ListView<String> skillsList;
    @FXML private Button addSkillButton;
    @FXML private Button removeSkillButton;
    @FXML private Button saveButton;

    private ObservableList<String> skills = FXCollections.observableArrayList();
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        try {
            loadUserData(user);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Database Error", "Failed to load user data: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing ProfileController...");

        // Get the current user from the session
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showErrorAlert("Error", "Not Logged In", "Please log in to view your profile.");
            return;
        }

        // Initialize the skills list
        skillsList.setItems(skills);

        setupSkillControls();
    }

    private void loadUserData(User user) throws SQLException {
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        skills.clear();
        skills.addAll(user.getSkills());
    }

    private void setupSkillControls() {
        // Enable/disable remove button based on selection
        skillsList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> removeSkillButton.setDisable(newVal == null)
        );

        // Enable add button only when skill field has text
        skillField.textProperty().addListener(
            (obs, oldVal, newVal) -> addSkillButton.setDisable(newVal == null || newVal.trim().isEmpty())
        );
    }

    @FXML
    private void handleAddSkill() {
        String skill = skillField.getText().trim();
        if (!skill.isEmpty() && !skills.contains(skill)) {
            skills.add(skill);
            skillField.clear();
        }
    }

    @FXML
    private void handleRemoveSkill() {
        String selectedSkill = skillsList.getSelectionModel().getSelectedItem();
        if (selectedSkill != null) {
            skills.remove(selectedSkill);
        }
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) {
            AlertUtils.showErrorAlert("Error", "Not Logged In", "Please log in to save your profile.");
            return;
        }

        try {
            currentUser.updateProfile(
                emailField.getText().trim(),
                phoneField.getText().trim(),
                skills
            );
            AlertUtils.showInformationAlert("Success", null, "Profile updated successfully!");
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to Save", "Could not update profile: " + e.getMessage());
        }
    }
}