package com.skillconnect.controllers;

import com.skillconnect.models.Project;
import com.skillconnect.models.User;
import com.skillconnect.utils.SessionManager;
import com.skillconnect.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.List;

public class ActiveProjectsController {
    @FXML private TableView<Project> activeProjectsTable;
    @FXML private TableColumn<Project, String> projectColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> deadlineColumn;
    @FXML private TableColumn<Project, String> progressColumn;
    @FXML private TableColumn<Project, String> actionColumn;

    private User currentUser;
    private ObservableList<Project> projectsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        loadProjects();
    }

    private void setupTable() {
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));

        activeProjectsTable.setItems(projectsList);
    }

    private void loadProjects() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                List<Project> projects = Project.getActiveProjects(currentUser.getId());
                activeProjectsTable.getItems().setAll(projects);
            }
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Error loading projects", "Failed to load active projects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadProjects();
    }
}