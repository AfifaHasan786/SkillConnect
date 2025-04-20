package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import com.skillconnect.models.ProjectApplication;
import com.skillconnect.models.User;
import com.skillconnect.models.VolunteerProfile;
import com.skillconnect.models.Skill;
import com.skillconnect.services.ApplicationService;
import com.skillconnect.utils.AlertUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ApplyProjectDialogController {
    @FXML private Text projectTitle;
    @FXML private Text projectDescription;
    @FXML private FlowPane requiredSkillsContainer;
    @FXML private ComboBox<Skill> skillComboBox;
    @FXML private FlowPane selectedSkillsContainer;
    @FXML private JFXButton addSkillButton;
    @FXML private VBox root;
    @FXML private Button applyButton;
    @FXML private Button cancelButton;

    private Project project;
    private User currentUser;
    private Set<Skill> selectedSkills;
    private ApplicationService applicationService;
    private ObservableList<Skill> availableSkills = FXCollections.observableArrayList();
    private List<Skill> requiredSkillsList = new ArrayList<>();

    public void initialize() {
        selectedSkills = new HashSet<>();
        applicationService = new ApplicationService();

        setupEventHandlers();

        // Load custom stylesheet
        root.getStylesheets().add(getClass().getResource("/styles/apply-dialog.css").toExternalForm());

        // Set project details if project is not null
        if (project != null) {
            projectTitle.setText(project.getTitle());
            projectDescription.setText(project.getDescription());

            // Load required skills
            if (project.getRequiredSkills() != null) {
                requiredSkillsList = Arrays.stream(project.getRequiredSkills().split(","))
                                      .map(String::trim)
                                      .map(Skill::new)
                                      .collect(Collectors.toList());

                for (Skill skill : requiredSkillsList) {
                    addSkillTag(skill, requiredSkillsContainer);
                }
            }
        }

        // Initialize ComboBox with empty list first
        skillComboBox.setItems(FXCollections.observableArrayList());

        // Load volunteer's skills
        try {
            if (currentUser != null) {
                VolunteerProfile volunteer = VolunteerProfile.getByUserId(currentUser.getId());
                if (volunteer != null && volunteer.getSkills() != null) {
                    // Convert comma-separated skills string to Skill objects
                    List<Skill> skills = Arrays.stream(volunteer.getSkills().split(","))
                                             .map(String::trim)
                                             .filter(s -> !s.isEmpty())
                                             .map(Skill::new)
                                             .collect(Collectors.toList());
                    availableSkills.addAll(skills);
                    skillComboBox.setItems(availableSkills);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load skills: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Prevent null selection
        skillComboBox.setOnShowing(e -> {
            if (skillComboBox.getValue() == null && !availableSkills.isEmpty()) {
                skillComboBox.setValue(availableSkills.get(0));
            }
        });

        // Setup skill selection with null check
        addSkillButton.setOnAction(e -> {
            if (skillComboBox.getValue() != null) {
                handleAddSkill();
            }
        });

        skillComboBox.setOnAction(e -> {
            Skill selectedSkill = skillComboBox.getValue();
            if (selectedSkill != null && !selectedSkills.contains(selectedSkill)) {
                handleAddSkill();
            }
        });

        // Add style classes to buttons
        addSkillButton.getStyleClass().add("button-primary");
    }

    private void setupEventHandlers() {
        skillComboBox.setOnAction(event -> {
            Skill selectedSkill = skillComboBox.getValue();
            if (selectedSkill != null && !selectedSkills.contains(selectedSkill)) {
                selectedSkills.add(selectedSkill);
                updateSelectedSkillsDisplay();
                skillComboBox.setValue(null);
            }
        });

        applyButton.setOnAction(event -> handleApply());
        cancelButton.setOnAction(event -> closeDialog());
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null && projectTitle != null && projectDescription != null) {
            projectTitle.setText(project.getTitle());
            projectDescription.setText(project.getDescription());
            displayRequiredSkills();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        populateUserSkills();
    }

    private void displayRequiredSkills() {
        requiredSkillsContainer.getChildren().clear();
        for (Skill skill : requiredSkillsList) {
            Label skillChip = createSkillChip(skill);
            requiredSkillsContainer.getChildren().add(skillChip);
        }
    }

    private void populateUserSkills() {
        skillComboBox.getItems().clear();
        skillComboBox.getItems().addAll(availableSkills);
    }

    private void updateSelectedSkillsDisplay() {
        selectedSkillsContainer.getChildren().clear();
        for (Skill skill : selectedSkills) {
            Label skillChip = createSkillChip(skill);
            selectedSkillsContainer.getChildren().add(skillChip);
        }
    }

    private Label createSkillChip(Skill skill) {
        Label chip = new Label(skill.getName());
        chip.getStyleClass().add("skill-chip");
        return chip;
    }

    private void handleAddSkill() {
        Skill selectedSkill = skillComboBox.getValue();
        if (selectedSkill != null && !selectedSkills.contains(selectedSkill)) {
            selectedSkills.add(selectedSkill);
            addSelectedSkillChip(selectedSkill);
            skillComboBox.setValue(null);

            // Animate the new skill chip
            HBox lastChip = (HBox) selectedSkillsContainer.getChildren().get(selectedSkillsContainer.getChildren().size() - 1);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lastChip);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void addSkillTag(Skill skill, FlowPane container) {
        Label skillLabel = new Label(skill.getName());
        skillLabel.getStyleClass().add("skill-tag");
        container.getChildren().add(skillLabel);
    }

    private void addSelectedSkillChip(Skill skill) {
        HBox chip = new HBox();
        chip.getStyleClass().add("skill-chip");
        chip.setOpacity(0.0); // Start invisible for animation

        Label skillLabel = new Label(skill.getName());

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
            showAlert("Error", "Please select at least one skill", Alert.AlertType.ERROR);
            return;
        }

        try {
            boolean success = applicationService.submitApplication(project.getId(), currentUser, new ArrayList<>(selectedSkills), "");
            if (success) {
                showAlert("Success", "Application submitted successfully", Alert.AlertType.INFORMATION);
                closeDialog();
            } else {
                showAlert("Error", "Failed to submit application", Alert.AlertType.ERROR);
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}