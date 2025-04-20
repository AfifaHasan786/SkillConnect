package com.skillconnect.controllers;

import com.skillconnect.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController extends DashboardController {

    @FXML private VBox mainContent;
    @FXML private Label welcomeLabel;
    private User currentUser;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        showProjects(); // Load default view
    }

    @FXML
    @Override
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Failed to logout");
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(profileView);
        } catch (IOException e) {
            showError("Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void showCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProject.fxml"));
            Parent createProjectView = loader.load();
            CreateProjectController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(createProjectView);
        } catch (IOException e) {
            showError("Failed to load create project view: " + e.getMessage());
        }
    }

    @FXML
    private void showProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BrowseProjects.fxml"));
            Parent projectsView = loader.load();
            BrowseProjectsController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(projectsView);
        } catch (IOException e) {
            showError("Failed to load projects view");
            e.printStackTrace();
        }
    }

    @FXML
    private void showRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReviewApplications.fxml"));
            Parent requestsView = loader.load();
            ReviewApplicationsController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(requestsView);
        } catch (IOException e) {
            showError("Failed to load requests view");
            e.printStackTrace();
        }
    }

    @FXML
    private void showChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            Parent chatView = loader.load();
            ChatController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(chatView);
        } catch (IOException e) {
            showError("Failed to load chat view");
            e.printStackTrace();
        }
    }

    @Override
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}