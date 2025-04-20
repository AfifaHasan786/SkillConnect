package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import com.skillconnect.models.Request;
import com.skillconnect.models.User;
import com.skillconnect.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ViewRequestsController {
    @FXML
    private VBox requestsContainer;
    @FXML
    private VBox emptyState;

    private Project currentProject;
    private User currentUser;

    public void setProject(Project project) {
        this.currentProject = project;
        loadRequests();
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        // Add any initialization code here
    }

    private void loadRequests() {
        try {
            List<Request> requests = Request.getRequestsByProject(currentProject.getId());
            if (requests.isEmpty()) {
                emptyState.setVisible(true);
                requestsContainer.setVisible(false);
            } else {
                emptyState.setVisible(false);
                requestsContainer.setVisible(true);
                displayRequests(requests);
            }
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load requests", e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayRequests(List<Request> requests) {
        requestsContainer.getChildren().clear();
        for (Request request : requests) {
            VBox requestCard = createRequestCard(request);
            requestsContainer.getChildren().add(requestCard);
        }
    }

    private VBox createRequestCard(Request request) {
        VBox card = new VBox(10);
        card.getStyleClass().add("request-card");

        Text volunteerName = new Text(request.getVolunteerName());
        volunteerName.getStyleClass().add("volunteer-name");

        Text status = new Text("Status: " + request.getStatus());
        status.getStyleClass().add("request-status");

        Text date = new Text("Date: " + request.getFormattedDate());
        date.getStyleClass().add("request-date");

        FlowPane skillsPane = new FlowPane();
        skillsPane.setHgap(5);
        skillsPane.setVgap(5);

        List<String> skillsList = request.getVolunteerSkills();
        if (skillsList != null && !skillsList.isEmpty()) {
            for (String skill : skillsList) {
                Text skillTag = new Text(skill.trim());
                skillTag.getStyleClass().add("skill-tag");
                skillsPane.getChildren().add(skillTag);
            }
        }

        HBox actions = new HBox(10);
        if ("PENDING".equals(request.getStatus())) {
            JFXButton approveButton = new JFXButton("Approve");
            approveButton.getStyleClass().add("approve-button");
            approveButton.setOnAction(e -> handleRequestAction(request, "APPROVED"));

            JFXButton rejectButton = new JFXButton("Reject");
            rejectButton.getStyleClass().add("reject-button");
            rejectButton.setOnAction(e -> handleRequestAction(request, "REJECTED"));

            actions.getChildren().addAll(approveButton, rejectButton);
        }

        JFXButton chatButton = new JFXButton("Chat");
        chatButton.getStyleClass().add("chat-button");
        chatButton.setOnAction(e -> handleChat(request));

        actions.getChildren().add(chatButton);

        card.getChildren().addAll(volunteerName, status, date, skillsPane, actions);
        return card;
    }

    private void handleRequestAction(Request request, String newStatus) {
        try {
            request.updateStatus(newStatus);
            loadRequests(); // Refresh the view
            AlertUtils.showInformationAlert("Success", "Request Updated",
                "The request status has been updated to " + newStatus);
        } catch (SQLException e) {
            AlertUtils.showErrorAlert("Error", "Failed to update request", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleChat(Request request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            VBox chatView = loader.load();
            ChatController controller = loader.getController();
            controller.setUser(currentUser);
            try {
                User volunteer = request.getVolunteer();
                if (volunteer != null) {
                    controller.selectUser(volunteer);
                } else {
                    AlertUtils.showErrorAlert("Error", "User Not Found", "Could not find the volunteer user.");
                    return;
                }
            } catch (SQLException e) {
                AlertUtils.showErrorAlert("Error", "Database Error", "Could not load volunteer information: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Replace current view with chat view
            VBox parent = (VBox) requestsContainer.getParent();
            parent.getChildren().set(1, chatView);
        } catch (IOException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load chat view", e.getMessage());
            e.printStackTrace();
        }
    }
}