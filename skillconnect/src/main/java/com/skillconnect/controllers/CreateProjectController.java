package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTextArea;
import com.skillconnect.models.Project;
import com.skillconnect.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.ArrayList;
import java.util.List;

public class CreateProjectController {
    @FXML private JFXTextField titleField;
    @FXML private JFXTextArea descriptionField;
    @FXML private JFXTextField skillField;
    @FXML private FlowPane skillsContainer;

    private User currentUser;
    private List<String> selectedSkills = new ArrayList<>();

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleAddSkill() {
        String skill = skillField.getText().trim();
        if (!skill.isEmpty() && !selectedSkills.contains(skill)) {
            selectedSkills.add(skill);
            addSkillChip(skill);
            skillField.clear();
        }
    }

    private void addSkillChip(String skill) {
        HBox chip = new HBox();
        chip.getStyleClass().add("skill-chip");

        Label skillLabel = new Label(skill);

        FontAwesomeIconView closeIcon = new FontAwesomeIconView();
        closeIcon.setGlyphName("TIMES");
        closeIcon.setSize("14");
        closeIcon.getStyleClass().add("close-icon");

        JFXButton removeButton = new JFXButton();
        removeButton.setGraphic(closeIcon);
        removeButton.setOnAction(e -> {
            selectedSkills.remove(skill);
            skillsContainer.getChildren().remove(chip);
        });

        chip.getChildren().addAll(skillLabel, removeButton);
        skillsContainer.getChildren().add(chip);
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String skills = String.join(", ", selectedSkills);

        if (title.isEmpty() || description.isEmpty() || selectedSkills.isEmpty()) {
            showError("Please fill in all fields and add at least one skill.");
            return;
        }

        try {
            Project.createProject(title, description, skills, currentUser.getId());
            clearForm();
            showSuccess("Project created successfully!");
        } catch (Exception e) {
            showError("Failed to create project: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        skillField.clear();
        selectedSkills.clear();
        skillsContainer.getChildren().clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
