package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.skillconnect.models.Project;
import com.skillconnect.models.User;
import com.skillconnect.utils.DatabaseConnection;
import com.skillconnect.utils.AlertUtils;
import com.skillconnect.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class BrowseProjectsController implements Initializable {
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> titleColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, String> skillsColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, Void> actionColumn;
    @FXML private TextField searchField;

    private User currentUser;
    private ObservableList<Project> projectsList = FXCollections.observableArrayList();
    private FilteredList<Project> filteredProjects;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadProjects();
        setupSearch();
    }

    public void setUser(User user) {
        this.currentUser = user;
        // Reload projects after setting user to ensure proper button visibility
        loadProjects();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        skillsColumn.setCellValueFactory(new PropertyValueFactory<>("requiredSkills"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox actionButtons = new HBox(5);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Project project = getTableView().getItems().get(getIndex());
                actionButtons.getChildren().clear();

                if (currentUser != null) {
                    if (currentUser.getRole().equals("ADMIN")) {
                        // Admin sees Open/Close buttons
                        if (project.getStatus().equals("OPEN")) {
                            JFXButton closeButton = new JFXButton("Close");
                            closeButton.getStyleClass().add("button-warning");
                            closeButton.setOnAction(e -> updateProjectStatus(project, "CLOSED"));
                            actionButtons.getChildren().add(closeButton);
                        } else {
                            JFXButton openButton = new JFXButton("Open");
                            openButton.getStyleClass().add("button-success");
                            openButton.setOnAction(e -> updateProjectStatus(project, "OPEN"));
                            actionButtons.getChildren().add(openButton);
                        }
                    } else {
                        // Volunteer sees Apply button
                        JFXButton applyButton = new JFXButton("Apply");
                        applyButton.getStyleClass().add("button-primary");
                        applyButton.setDisable(!project.getStatus().equals("OPEN"));
                        applyButton.setOnAction(e -> handleApplyAction(project));
                        actionButtons.getChildren().add(applyButton);
                    }
                }

                setGraphic(actionButtons);
            }
        });
    }

    private void handleApplyAction(Project project) {
        try {
            if (!project.hasVolunteerApplied(currentUser.getId())) {
                showApplyDialog(project);
            } else {
                AlertUtils.showInformationAlert(
                    "Already Applied",
                    null,
                    "You have already applied for this project!"
                );
            }
        } catch (SQLException ex) {
            AlertUtils.showErrorAlert(
                "Application Error",
                null,
                "Failed to apply for project: " + ex.getMessage()
            );
        }
    }

    private void showApplyDialog(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ApplyProjectDialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Apply for Project");
            dialog.setScene(new Scene(loader.load()));

            ApplyProjectDialogController controller = loader.getController();
            controller.initialize(project, currentUser);

            dialog.showAndWait();
        } catch (Exception e) {
            AlertUtils.showErrorAlert(
                "Dialog Error",
                null,
                "Failed to open application dialog: " + e.getMessage()
            );
        }
    }

    private void updateProjectStatus(Project project, String newStatus) {
        try {
            project.updateStatus(newStatus);
            AlertUtils.showInformationAlert(
                "Success",
                null,
                "Project status updated successfully!"
            );
            loadProjects(); // Reload to refresh the view
        } catch (SQLException e) {
            AlertUtils.showErrorAlert(
                "Update Error",
                null,
                "Failed to update project status: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        try {
            if (currentUser != null && currentUser.getRole().equals("ADMIN")) {
                projectsList.setAll(Project.getProjectsByAdmin(currentUser.getId()));
            } else {
                projectsList.setAll(Project.getAllProjects());
            }
            projectsTable.setItems(projectsList);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert(
                "Load Error",
                null,
                "Failed to load projects: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        filteredProjects = new FilteredList<>(projectsList, p -> true);
        projectsTable.setItems(filteredProjects);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProjects.setPredicate(project -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchText = newValue.toLowerCase();
                return project.getTitle().toLowerCase().contains(searchText) ||
                       project.getDescription().toLowerCase().contains(searchText) ||
                       project.getRequiredSkills().toLowerCase().contains(searchText);
            });
        });
    }
}