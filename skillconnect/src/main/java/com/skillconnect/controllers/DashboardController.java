package com.skillconnect.controllers;

import com.skillconnect.models.User;
import com.skillconnect.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

public abstract class DashboardController {
    protected User currentUser;

    @FXML protected Label welcomeLabel;
    @FXML protected Label userNameLabel;
    @FXML protected Label userRoleLabel;
    @FXML protected VBox mainContent;

    public void setUser(User user) {
        this.currentUser = user;
        // Update session manager whenever user is set
        SessionManager.getInstance().setCurrentUser(user);
        updateUserInfo();
    }

    protected void updateUserInfo() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
        if (userNameLabel != null) {
            userNameLabel.setText(currentUser.getUsername());
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(currentUser.getRole());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            // Clear session on logout
            SessionManager.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
        }
    }

    protected void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}