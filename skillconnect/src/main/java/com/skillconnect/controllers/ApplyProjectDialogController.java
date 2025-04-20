package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import com.skillconnect.models.ProjectApplication;
import com.skillconnect.models.User;
import com.skillconnect.models.VolunteerProfile;
import com.skillconnect.models.Skill;
import com.skillconnect.services.ApplicationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ApplyProjectDialogController {
    @FXML private Label projectTitle;
    @FXML private Label projectDescription;
    @FXML private FlowPane requiredSkillsContainer;
    @FXML private ComboBox<String> skillsComboBox;
    @FXML private FlowPane selectedSkillsContainer;
    @FXML private JFXButton addSkillButton;
    @FXML private VBox root;
    @FXML private JFXButton cancelButton;
    @FXML private JFXButton applyButton;

    private Project project;
    private User currentUser;
    private Set<String> selectedSkills;
    private ApplicationService applicationService;
    private ObservableList<String> availableSkills = FXCollections.observableArrayList();

    public void initialize() {
        selectedSkills = new HashSet<>();
        applicationService = new ApplicationService();

        // Initialize ComboBox
        skillsComboBox.setPromptText("Select skills that match the requirements");

        // Setup event handlers
        addSkillButton.setOnAction(event -> handleAddSkill());

        // Style buttons
        applyButton.getStyleClass().add("button-primary");
        cancelButton.getStyleClass().add("button-secondary");
        addSkillButton.getStyleClass().add("button-primary");
        addSkillButton.setText("+ Add");

        // Add stylesheet
        root.getStylesheets().add(getClass().getResource("/styles/apply-dialog.css").toExternalForm());
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            projectTitle.setText(project.getTitle());
            projectDescription.setText(project.getDescription());
            displayRequiredSkills();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserSkills();
    }

    private void loadUserSkills() {
        try {
            if (currentUser != null) {
                VolunteerProfile volunteer = VolunteerProfile.getByUserId(currentUser.getId());
                if (volunteer != null && volunteer.getSkills() != null) {
                    availableSkills.clear();
                    List<String> skills = Arrays.stream(volunteer.getSkills().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    availableSkills.addAll(skills);
                    skillsComboBox.setItems(availableSkills);
                }
            }
        } catch (SQLException e) {
            Platform.runLater(() -> showAlert("Error", "Failed to load skills: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void displayRequiredSkills() {
        requiredSkillsContainer.getChildren().clear();
        if (project != null && project.getRequiredSkills() != null) {
            Arrays.stream(project.getRequiredSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(skill -> addSkillTag(skill, requiredSkillsContainer, false));
        }
    }

    private void handleAddSkill() {
        String selectedSkill = skillsComboBox.getValue();
        if (selectedSkill != null && !selectedSkills.contains(selectedSkill)) {
            selectedSkills.add(selectedSkill);
            addSkillTag(selectedSkill, selectedSkillsContainer, true);
            skillsComboBox.setValue(null);
        }
    }

    private void addSkillTag(String skill, FlowPane container, boolean removable) {
        HBox chip = new HBox();
        chip.getStyleClass().add("skill-chip");

        Label skillLabel = new Label(skill);
        chip.getChildren().add(skillLabel);

        if (removable) {
            FontAwesomeIconView closeIcon = new FontAwesomeIconView();
            closeIcon.setGlyphName("TIMES");
            closeIcon.setSize("12");

            JFXButton removeButton = new JFXButton();
            removeButton.getStyleClass().add("close-button");
            removeButton.setGraphic(closeIcon);
            removeButton.setOnAction(e -> {
                selectedSkills.remove(skill);
                container.getChildren().remove(chip);
            });
            chip.getChildren().add(removeButton);
        }

        container.getChildren().add(chip);
    }

    @FXML
    private void handleApply() {
        if (selectedSkills.isEmpty()) {
            showAlert("Warning", "Please select at least one skill", Alert.AlertType.WARNING);
            return;
        }

        if (project == null || currentUser == null) {
            showAlert("Error", "Invalid project or user data", Alert.AlertType.ERROR);
            return;
        }

        try {
            List<Skill> skillsList = selectedSkills.stream()
                    .map(skillName -> {
                        Skill skill = new Skill();
                        skill.setName(skillName);
                        return skill;
                    })
                    .collect(Collectors.toList());

            boolean success = applicationService.submitApplication(project.getId(), currentUser, skillsList, "");
            if (success) {
                showAlert("Success", "Application submitted successfully", Alert.AlertType.INFORMATION);
                closeDialog();
            } else {
                showAlert("Error", "Failed to submit application. Please try again.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to submit application: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            // Style the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/styles/apply-dialog.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-alert");

            alert.showAndWait();
        });
    }
}
