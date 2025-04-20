package com.skillconnect.controllers;

import com.jfoenix.controls.JFXButton;
import com.skillconnect.models.Project;
import com.skillconnect.models.User;
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

public class ViewProjectsController {
    @FXML
    private FlowPane projectsContainer;
    @FXML
    private VBox emptyState;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        loadProjects();
    }

    @FXML
    private void initialize() {
        // Add any initialization code here
    }

    private void loadProjects() {
        try {
            List<Project> projects = Project.getProjectsByAdmin(currentUser.getId());
            if (projects.isEmpty()) {
                emptyState.setVisible(true);
                projectsContainer.setVisible(false);
            } else {
                emptyState.setVisible(false);
                projectsContainer.setVisible(true);
                displayProjects(projects);
            }
        } catch (SQLException e) {
            showError("Failed to load projects");
            e.printStackTrace();
        }
    }

    private void displayProjects(List<Project> projects) {
        projectsContainer.getChildren().clear();
        for (Project project : projects) {
            VBox projectCard = createProjectCard(project);
            projectsContainer.getChildren().add(projectCard);
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");

        Text title = new Text(project.getTitle());
        title.getStyleClass().add("project-title");

        Text description = new Text(project.getDescription());
        description.getStyleClass().add("project-description");
        description.setWrappingWidth(270);

        FlowPane skillsPane = new FlowPane();
        skillsPane.setHgap(5);
        skillsPane.setVgap(5);

        // Split the required skills string and create tags
        String[] skills = project.getRequiredSkills().split(",");
        for (String skill : skills) {
            Text skillTag = new Text(skill.trim());
            skillTag.getStyleClass().add("skill-tag");
            skillsPane.getChildren().add(skillTag);
        }

        HBox actions = new HBox(10);
        JFXButton viewRequests = new JFXButton("View Requests");
        viewRequests.getStyleClass().add("action-button");
        viewRequests.setOnAction(e -> handleViewRequests(project));

        JFXButton chat = new JFXButton("Chat");
        chat.getStyleClass().add("action-button");
        chat.setOnAction(e -> handleChat(project));

        actions.getChildren().addAll(viewRequests, chat);

        card.getChildren().addAll(title, description, skillsPane, actions);
        return card;
    }

    private void handleViewRequests(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewRequests.fxml"));
            VBox requestsView = loader.load();
            ViewRequestsController controller = loader.getController();
            controller.setProject(project);
            controller.setUser(currentUser);

            // Replace current view with requests view
            VBox parent = (VBox) projectsContainer.getParent();
            parent.getChildren().set(1, requestsView);
        } catch (IOException e) {
            showError("Failed to load requests view");
            e.printStackTrace();
        }
    }

    private void handleChat(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            VBox chatView = loader.load();
            ChatController controller = loader.getController();

            // Replace current view with chat view
            VBox parent = (VBox) projectsContainer.getParent();
            parent.getChildren().set(1, chatView);
        } catch (IOException e) {
            showError("Failed to load chat view");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProject.fxml"));
            VBox createProjectView = loader.load();
            CreateProjectController controller = loader.getController();
            controller.setUser(currentUser);

            // Replace current view with create project view
            VBox parent = (VBox) projectsContainer.getParent();
            parent.getChildren().set(1, createProjectView);
        } catch (IOException e) {
            showError("Failed to load create project view");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}