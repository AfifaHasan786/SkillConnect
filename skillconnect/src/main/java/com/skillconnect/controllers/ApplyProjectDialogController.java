package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import com.skillconnect.models.ProjectApplication;
import com.skillconnect.models.User;
import com.skillconnect.models.VolunteerProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplyProjectDialogController {
    @FXML private Text projectTitle;
    @FXML private Text projectDescription;
    @FXML private FlowPane requiredSkillsContainer;
    @FXML private ComboBox<String> skillsComboBox;
    @FXML private FlowPane selectedSkillsContainer;
    @FXML private JFXButton addSkillButton;
    @FXML private VBox root;

    private Project project;
    private User currentUser;
    private List<String> selectedSkills = new ArrayList<>();
    private ObservableList<String> availableSkills = FXCollections.observableArrayList();
    private List<String> requiredSkillsList = new ArrayList<>();

    public void initialize(Project project, User user) {
        this.project = project;
        this.currentUser = user;

        // Set project details
        projectTitle.setText(project.getTitle());
        projectDescription.setText(project.getDescription());

        // Load required skills
        requiredSkillsList = Arrays.stream(project.getRequiredSkills().split(","))
                                  .map(String::trim)
                                  .collect(Collectors.toList());

        for (String skill : requiredSkillsList) {
            addSkillTag(skill, requiredSkillsContainer);
        }

        // Initialize ComboBox with empty list first
        skillsComboBox.setItems(FXCollections.observableArrayList());

        // Load volunteer's skills
        try {
            VolunteerProfile volunteer = VolunteerProfile.getByUserId(currentUser.getId());
            if (volunteer != null) {
                availableSkills.addAll(volunteer.getSkills());
                // Update ComboBox items after loading
                skillsComboBox.setItems(availableSkills);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load skills: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Prevent null selection
        skillsComboBox.setOnShowing(e -> {
            if (skillsComboBox.getValue() == null && !availableSkills.isEmpty()) {
                skillsComboBox.setValue(availableSkills.get(0));
            }
        });

        // Setup skill selection with null check
        addSkillButton.setOnAction(e -> {
            if (skillsComboBox.getValue() != null) {
                handleAddSkill();
            }
        });

        skillsComboBox.setOnAction(e -> {
            String selected = skillsComboBox.getValue();
            if (selected != null && !selectedSkills.contains(selected)) {
                handleAddSkill();
            }
        });
    }

    private void handleAddSkill() {
        String selectedSkill = skillsComboBox.getValue();
        if (selectedSkill != null && !selectedSkills.contains(selectedSkill)) {
            selectedSkills.add(selectedSkill);
            addSelectedSkillChip(selectedSkill);
            skillsComboBox.setValue(null);

            // Animate the new skill chip
            HBox lastChip = (HBox) selectedSkillsContainer.getChildren().get(selectedSkillsContainer.getChildren().size() - 1);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lastChip);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void addSkillTag(String skill, FlowPane container) {
        Label skillLabel = new Label(skill);
        skillLabel.getStyleClass().add("skill-tag");
        container.getChildren().add(skillLabel);
    }

    private void addSelectedSkillChip(String skill) {
        HBox chip = new HBox();
        chip.getStyleClass().add("skill-chip");
        chip.setOpacity(0.0); // Start invisible for animation

        Label skillLabel = new Label(skill);

        FontAwesomeIconView closeIcon = new FontAwesomeIconView();
        closeIcon.setGlyphName("TIMES");
        closeIcon.setSize("12");

        JFXButton removeButton = new JFXButton();
        removeButton.getStyleClass().add("close-button");
        removeButton.setGraphic(closeIcon);
        removeButton.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), chip);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                selectedSkills.remove(skill);
                selectedSkillsContainer.getChildren().remove(chip);
            });
            fadeOut.play();
        });

        chip.getChildren().addAll(skillLabel, removeButton);
        selectedSkillsContainer.getChildren().add(chip);
    }

    @FXML
    private void handleApply() {
        if (selectedSkills.isEmpty()) {
            showAlert("Error", "Please select at least one matching skill.", Alert.AlertType.WARNING);
            return;
        }

        // Check if selected skills match required skills
        boolean hasMatchingSkill = selectedSkills.stream()
            .anyMatch(requiredSkillsList::contains);

        if (!hasMatchingSkill) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("None of your selected skills match the required skills for this project. Are you sure you want to apply?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                submitApplication();
            }
        } else {
            submitApplication();
        }
    }

    private void submitApplication() {
        try {
            // Create project application with selected skills
            ProjectApplication.apply(project.getId(), currentUser.getId(), selectedSkills);

            // Show success message
            showAlert("Success", "Your application has been submitted successfully!", Alert.AlertType.INFORMATION);

            // Close the dialog
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert("Error", "Failed to apply for project: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}