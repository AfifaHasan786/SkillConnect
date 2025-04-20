package com.skillconnect.controllers;

import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import com.skillconnect.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class VolunteerDashboardController extends DashboardController {
    @FXML private VBox mainContent;
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    @Override
    public void setUser(User user) {
        super.setUser(user);
        welcomeLabel.setText("Welcome, " + user.getUsername());
        userNameLabel.setText(user.getUsername());
        userRoleLabel.setText("Volunteer");
        showProjects(); // Show projects by default
    }

    @FXML
    private void showProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BrowseProjects.fxml"));
            VBox projectsView = loader.load();
            BrowseProjectsController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(projectsView);
        } catch (IOException e) {
            showError("Failed to load projects view: " + e.getMessage());
        }
    }

    @FXML
    private void showApplications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MyApplications.fxml"));
            VBox applicationsView = loader.load();
            MyApplicationsController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(applicationsView);
        } catch (IOException e) {
            showError("Failed to load applications view: " + e.getMessage());
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VolunteerProfile.fxml"));
            ScrollPane profileView = loader.load();
            VolunteerProfileController controller = loader.getController();
            controller.setUser(currentUser);
            mainContent.getChildren().setAll(profileView);
        } catch (IOException e) {
            showError("Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void showMessages() {
        try {
            // Ensure we have a valid user in the session
            if (currentUser == null) {
                AlertUtils.showErrorAlert("Error", "Not Logged In", "Please log in to use the chat feature.");
                return;
            }

            // Update session with current user if needed
            SessionManager.getInstance().setCurrentUser(currentUser);
            System.out.println("Ensuring session has current user before loading chat: " + currentUser.getUsername());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            Parent chatView = loader.load();
            ChatController chatController = loader.getController();

            // Set the user in the controller
            chatController.setUser(currentUser);

            // Replace the content in the main content area
            mainContent.getChildren().clear();
            mainContent.getChildren().add(chatView);

            // Make sure the chat view fills the space
            AnchorPane.setTopAnchor(chatView, 0.0);
            AnchorPane.setBottomAnchor(chatView, 0.0);
            AnchorPane.setLeftAnchor(chatView, 0.0);
            AnchorPane.setRightAnchor(chatView, 0.0);

            System.out.println("Chat view loaded and user set: " + currentUser.getUsername());
        } catch (IOException e) {
            AlertUtils.showErrorAlert("Error", "Load Error", "Failed to load chat view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    @Override
    public void handleLogout() {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get current stage and set new scene
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
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